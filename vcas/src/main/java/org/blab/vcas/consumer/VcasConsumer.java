package org.blab.vcas.consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.blab.vcas.BrokerNotAvailableException;
import org.blab.vcas.MessageFormatException;
import org.blab.vcas.UnknownServerException;
import org.blab.vcas.UnknownTopicException;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VcasConsumer implements Consumer {
  private static final Logger logger = LogManager.getLogger(VcasConsumer.class);

  private AsynchronousSocketChannel socket;
  private final ByteBuffer messageBuffer;
  private final Callback callback;

  private final ExecutorService executor;
  private final CompletionHandler<Void, ConsumerProperties> connectionHandler;
  private final CompletionHandler<Integer, ConsumerProperties> readHandler;
  private final CompletionHandler<Integer, String> writeHandler;

  private final Set<String> subscriptions;
  private final Queue<String> requests;
  private boolean isClosed = false;

  public VcasConsumer(ConsumerProperties properties, Callback callback) {
    if (properties == null || callback == null) throw new NullPointerException();

    try {
      this.executor = Executors.newVirtualThreadPerTaskExecutor();
      this.connectionHandler = new ConnectionHandler();
      this.readHandler = new ReadHandler(properties.maxMessageSize());
      this.writeHandler = new WriteHandler();

      this.callback = callback;
      this.messageBuffer = ByteBuffer.allocate(properties.maxMessageSize() + 1);
      this.socket = AsynchronousSocketChannel.open();

      this.subscriptions = new HashSet<>();
      this.requests = new LinkedList<>();

      this.socket.connect(properties.address(), properties, connectionHandler);
    } catch (IOException e) {
      logger.fatal(e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void subscribe(Set<String> topics) {
    if (isClosed) throw new IllegalStateException();
    topics.stream()
        .filter(t -> !subscriptions.contains(t))
        .forEach(
            t -> {
              subscriptions.add(t);
              subscribe(t);
            });
  }

  private void subscribe(String topic) {
    String request = String.format("name:%s|method:subscr\n", topic);
    request(request);
  }

  @Override
  public void unsubscribe() {
    unsubscribe(subscriptions);
  }

  @Override
  public void unsubscribe(Set<String> topics) {
    if (isClosed) throw new IllegalStateException();
    topics.stream()
        .filter(subscriptions::contains)
        .forEach(
            t -> {
              subscriptions.remove(t);
              unsubscribe(t);
            });
  }

  private void unsubscribe(String topic) {
    String request = String.format("name:%s|method:free\n", topic);
    request(request);
  }

  private void request(String request) {
    if (requests.isEmpty())
      socket.write(ByteBuffer.wrap(request.getBytes()), request, writeHandler);

    requests.add(request);
  }

  @Override
  public void close() {
    if (isClosed) throw new IllegalStateException();

    executor.submit(callback::onConnectionLost);

    try {
      if (!executor.awaitTermination(3, TimeUnit.MINUTES)) executor.shutdown();
      socket.close();
    } catch (Exception e) {
      logger.error(e);
    } finally {
      isClosed = true;
    }
  }

  private class ConnectionHandler implements CompletionHandler<Void, ConsumerProperties> {
    @Override
    public void completed(Void result, ConsumerProperties properties) {
      executor.submit(callback::onConnectionEstablished);
      subscriptions.forEach(VcasConsumer.this::subscribe);
      socket.read(messageBuffer, properties, readHandler);
    }

    @Override
    public void failed(Throwable t, ConsumerProperties properties) {
      logger.error(t);

      if (t instanceof ConnectException)
        executor.submit(
            () -> callback.onError(new BrokerNotAvailableException("Broker not available", t)));
      else executor.submit(() -> callback.onError(t));

      try {
        Thread.sleep(properties.reconnectTimeoutMs());

        if (!socket.isOpen()) socket = AsynchronousSocketChannel.open();
        socket.connect(properties.address(), properties, this);
      } catch (Exception e) {
        failed(e, properties);
      }
    }
  }

  private class ReadHandler implements CompletionHandler<Integer, ConsumerProperties> {
    private final ByteBuffer message;

    ReadHandler(int maxMessageSize) {
      message = ByteBuffer.allocate(maxMessageSize);
    }

    @Override
    public void completed(Integer result, ConsumerProperties properties) {
      for (int i = 0; i < result; ++i)
        if (messageBuffer.get(i) == '\n') submitMessage();
        else {
          if (!message.hasRemaining()) {
            executor.submit(
                () ->
                    callback.onError(
                        new MessageFormatException("Message overflow: " + message.toString())));
            message.clear();
          }

          message.put(messageBuffer.get(i));
        }

      socket.read(messageBuffer.clear(), properties, this);
    }

    private void submitMessage() {
      var msg = new String(Arrays.copyOf(message.array(), message.position()));

      try {
        var event = ConsumerEvent.parse(msg);

        if (event.value().equals("error")) {
          if (event.description().contains("not found"))
            executor.submit(
                () ->
                    callback.onError(
                        new UnknownTopicException("Topic not exists: " + event.topic(), null)));
          else
            executor.submit(
                () -> callback.onError(new UnknownServerException(event.description(), null)));
        } else executor.submit(() -> callback.onEvent(event));

        message.clear();
      } catch (Exception e) {
        logger.error(e);
        executor.submit(() -> callback.onError(e));
      }
    }

    @Override
    public void failed(Throwable t, ConsumerProperties properties) {
      logger.error(t);
      executor.submit(callback::onConnectionLost);
      socket.connect(properties.address(), properties, connectionHandler);
    }
  }

  private class WriteHandler implements CompletionHandler<Integer, String> {
    @Override
    public void completed(Integer integer, String request) {
      logger.debug("Request sent: {}", request);
      next();
    }

    @Override
    public void failed(Throwable t, String request) {
      logger.error("Request failed: {}", request);
      next();
    }

    private void next() {
      requests.poll();
      String next = requests.peek();
      if (next != null) socket.write(ByteBuffer.wrap(next.getBytes()), next, this);
    }
  }
}

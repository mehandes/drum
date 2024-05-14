package org.blab.vcas;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class VcasConsumer implements Consumer {
  private static final Logger logger = LogManager.getLogger(VcasConsumer.class);

  private final InetSocketAddress address;
  private final ByteBuffer buffer;

  private final ExecutorService executorService;
  private final CompletionHandler<Void, Void> connectionHandler;
  private final CompletionHandler<Integer, Void> eventHandler;
  private final Set<String> subscriptions;

  private AsynchronousSocketChannel socket;
  private Callback callback;

  private boolean isClosed;

  public VcasConsumer(
      AsynchronousSocketChannel socket, ByteBuffer buffer, InetSocketAddress address) {
    this.socket = socket;
    this.buffer = buffer;
    this.address = address;

    this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    this.connectionHandler = new ConnectionHandler();
    this.eventHandler = new EventHandler();
    this.subscriptions = new HashSet<>();

    this.socket.connect(address, null, connectionHandler);
  }

  public VcasConsumer withCallback(Callback callback) {
    this.callback = callback;
    return this;
  }

  @Override
  public void subscribe(Set<String> topics) {
    if (isClosed) throw new IllegalStateException();

    topics.removeAll(subscriptions);
    if (subscriptions.addAll(topics)) topics.forEach(this::subscribe);
  }

  private void subscribe(String topic) {
    try {
      socket
          .write(ByteBuffer.wrap(String.format("name:%s|method:subscr\n", topic).getBytes()))
          .get();
    } catch (Exception e) {
      logger.error(e);
      if (callback != null) executorService.submit(() -> callback.onError(e));
    }
  }

  @Override
  public void unsubscribe() {
    if (isClosed) throw new IllegalStateException();

    subscriptions.forEach(
        t -> socket.write(ByteBuffer.wrap(String.format("name:%s|method:free\n", t).getBytes())));
  }

  @Override
  public void close() {
    if (isClosed) throw new IllegalStateException();

    executorService.submit(() -> callback.onDisconnected());

    try {
      if (!executorService.awaitTermination(3, TimeUnit.MINUTES)) executorService.shutdown();
      socket.close();
    } catch (Exception e) {
      logger.error(e);
    } finally {
      isClosed = true;
    }
  }

  private class ConnectionHandler implements CompletionHandler<Void, Void> {
    @Override
    public void completed(Void result, Void attachment) {
      executorService.submit(() -> callback.onConnected());
      subscriptions.forEach(VcasConsumer.this::subscribe);
      socket.read(buffer, attachment, eventHandler);
    }

    @Override
    public void failed(Throwable exc, Void attachment) {
      logger.error(exc);

      if (callback != null) {
        executorService.submit(() -> callback.onDisconnected());
        executorService.submit(() -> callback.onError(exc));
      }

      try {
        if (!socket.isOpen()) socket = AsynchronousSocketChannel.open();
        socket.connect(address, attachment, this);
      } catch (Exception e) {
        logger.fatal(e);
        failed(e, attachment);
      }
    }
  }

  private class EventHandler implements CompletionHandler<Integer, Void> {
    private final ByteBuffer cache;

    EventHandler() {
      this.cache = ByteBuffer.allocate(8192);
    }

    @Override
    public void completed(Integer result, Void attachment) {
      for (int i = 0; i < buffer.position(); ++i)
        if (buffer.get(i) == (byte) '\n' && cache.position() != 0) {
          try {
            Event event = parseMessage(Arrays.copyOf(cache.array(), cache.position()));
            if (callback != null) executorService.submit(() -> callback.onEvent(event));
          } catch (Exception e) {
            logger.error(e);
            if (callback != null) executorService.submit(() -> callback.onError(e));
          }

          cache.clear();
        } else cache.put(buffer.get(i));

      socket.read(buffer.clear(), attachment, this);
    }

    private Event parseMessage(byte[] message) throws ParseException {
      Map<String, String> map =
          Arrays.stream(new String(message).split("\\|"))
              .map(s -> s.split(":"))
              .collect(Collectors.toMap(s -> s[0], s -> s[1]));

      return new Event(map.get("name"), map.get("val"), parseTime(map.get("time")));
    }

    private Long parseTime(String time) throws ParseException {
      return new SimpleDateFormat("dd.MM.yyyy HH_mm_ss.SSS").parse(time).getTime();
    }

    @Override
    public void failed(Throwable exc, Void attachment) {
      logger.error(exc);

      if (callback != null) {
        executorService.submit(() -> callback.onDisconnected());
        executorService.submit(() -> callback.onError(exc));
      }

      try {
        if (!socket.isOpen()) socket = AsynchronousSocketChannel.open();
        socket.connect(address, attachment, connectionHandler);
      } catch (Exception e) {
        logger.fatal(e);
        failed(e, attachment);
      }
    }
  }
}

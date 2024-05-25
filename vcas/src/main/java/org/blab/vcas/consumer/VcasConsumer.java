package org.blab.vcas.consumer;

import org.blab.vcas.UnknownServerException;
import org.blab.vcas.UnknownTopicException;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class VcasConsumer extends ByteChannel implements Consumer {
  private final Callback callback;
  private final ExecutorService executor;
  private final Set<String> subscriptions;
  private final ReentrantLock subscriptionLock;

  private boolean isClosed = false;

  public VcasConsumer(InetSocketAddress address, Callback callback) {
    super();

    this.callback = callback;
    this.subscriptions = new HashSet<>();
    this.executor = Executors.newVirtualThreadPerTaskExecutor();
    this.subscriptionLock = new ReentrantLock();

    start(address);
  }

  @Override
  protected void onMessage(byte[] msg) {
    try {
      var event = ConsumerEvent.parse(new String(msg));

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
    } catch (Exception e) {
      executor.submit(() -> callback.onError(e));
    }
  }

  @Override
  protected void onChannelConnected() {
    subscriptionLock.lock();
    executor.submit(callback::onConnectionEstablished);
    subscriptions.forEach(this::subscribe);
    subscriptionLock.unlock();
  }

  @Override
  protected void onChannelDisconnected() {
    executor.submit(callback::onConnectionLost);
  }

  @Override
  protected void onChannelError(ByteChannelException e) {
    executor.submit(() -> callback.onError(e));
  }

  @Override
  public void subscribe(Set<String> topics) {
    subscriptionLock.lock();
    if (isClosed) throw new IllegalStateException();
    topics.stream()
        .filter(t -> !subscriptions.contains(t))
        .forEach(
            t -> {
              subscriptions.add(t);
              subscribe(t);
            });
    subscriptionLock.unlock();
  }

  @Override
  public void unsubscribe() {
    unsubscribe(subscriptions);
  }

  @Override
  public void unsubscribe(Set<String> topics) {
    subscriptionLock.lock();
    if (isClosed) throw new IllegalStateException();
    topics.stream()
        .filter(subscriptions::contains)
        .forEach(
            t -> {
              subscriptions.remove(t);
              unsubscribe(t);
            });
    subscriptionLock.unlock();
  }

  private void subscribe(String topic) {
    write(String.format("name:%s|method:subscr\n", topic).getBytes());
  }

  private void unsubscribe(String topic) {
    write(String.format("name:%s|method:free\n", topic).getBytes());
  }

  @Override
  public void close() {
    if (isClosed) throw new IllegalStateException();
    isClosed = true;
    callback.onConnectionLost();
    executor.shutdown();
    super.close();
  }
}

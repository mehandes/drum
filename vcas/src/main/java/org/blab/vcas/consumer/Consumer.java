package org.blab.vcas.consumer;

import java.util.Set;

public interface Consumer {
  void subscribe(Set<String> topics);

  void unsubscribe();

  void unsubscribe(Set<String> topics);

  void close();

  interface Callback {
    void onEvent(ConsumerEvent event);

    void onConnectionEstablished();

    void onConnectionLost();

    void onError(Throwable e);
  }
}

package org.blab.vcas;

import java.util.Set;

public interface Consumer {
  void subscribe(Set<String> topics);

  void unsubscribe();

  void close();

  interface Callback {
    void onEvent(Event event);

    void onConnected();

    void onDisconnected();

    void onError(Throwable e);
  }
}

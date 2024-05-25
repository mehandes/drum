package org.blab.drum.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.blab.vcas.consumer.Consumer;
import org.blab.vcas.consumer.VcasConsumer;

import java.net.InetSocketAddress;

public abstract class VcasService implements Consumer.Callback {
  private final ObjectProperty<State> state;
  protected final Consumer eventConsumer;

  public VcasService(InetSocketAddress address) {
    this.state = new SimpleObjectProperty<>(State.DISCONNECTED);
    eventConsumer = new VcasConsumer(address, this);
  }

  public ObjectProperty<State> getVcasState() {
    return state;
  }

  @Override
  public void onConnectionEstablished() {
    state.setValue(State.CONNECTED);
  }

  @Override
  public void onConnectionLost() {
    state.setValue(State.DISCONNECTED);
  }

  public enum State {
    CONNECTED,
    DISCONNECTED
  }
}

package org.blab.drum.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.blab.vcas.consumer.Consumer;
import org.blab.vcas.consumer.VcasConsumer;

import java.net.InetSocketAddress;

public abstract class VcasService implements Consumer.Callback {
  private static final Logger logger = LogManager.getLogger(VcasService.class);

  private final ObjectProperty<State> state;
  protected final Consumer eventConsumer;

  public VcasService(InetSocketAddress address) {
    this.state = new SimpleObjectProperty<>(State.DISCONNECTED);
    eventConsumer = new VcasConsumer(address, this);
  }

  public ObjectProperty<State> getObservableVcasState() {
    return state;
  }

  @Override
  public void onConnectionEstablished() {
    logger.warn("Connection established");
    state.setValue(State.CONNECTED);
  }

  @Override
  public void onConnectionLost() {
    logger.warn("Connection lost");
    state.setValue(State.DISCONNECTED);
  }

  public enum State {
    CONNECTED,
    DISCONNECTED
  }
}

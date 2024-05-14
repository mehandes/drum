package org.blab.drum.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.blab.vcas.Consumer;
import org.blab.vcas.Event;
import org.blab.vcas.VcasConsumer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.HashMap;
import java.util.Map;

public class Datasource implements Consumer.Callback {
  private static final int VCAS_MESSAGE_MAX_SIZE = 2048;

  private static Datasource instance;

  public static Datasource getInstance() {
    return instance;
  }

  public static void createInstance(DatasourceConfiguration configuration) throws IOException {
    instance = new Datasource(configuration);
  }

  private final ObjectProperty<State> state;
  private final Map<String, Channel> channels;

  public Datasource(DatasourceConfiguration configuration) throws IOException {
    this.state = new SimpleObjectProperty<>(State.DISCONNECTED);
    this.channels = new HashMap<>(configuration.channelNames().size() / 2);

    for (String topic : configuration.channelNames())
      channels.put(topic, new Channel(configuration.persistenceRange()));

    new VcasConsumer(
            AsynchronousSocketChannel.open(),
            ByteBuffer.allocate(VCAS_MESSAGE_MAX_SIZE),
            configuration.address())
        .withCallback(this)
        .subscribe(configuration.channelNames());
  }

  public ObjectProperty<State> getStateProperty() {
    return state;
  }

  public Channel getChannel(String name) {
    return channels.get(name);
  }

  @Override
  public void onEvent(Event event) {
    // TODO Process incoming event
  }

  @Override
  public void onConnected() {
    this.state.set(State.CONNECTED);
  }

  @Override
  public void onDisconnected() {
    this.state.set(State.DISCONNECTED);
  }

  @Override
  public void onError(Throwable e) {}

  public enum State {
    CONNECTED,
    IDLE,
    DISCONNECTED
  }
}

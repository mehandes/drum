package org.blab.drum.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.blab.vcas.consumer.Consumer;
import org.blab.vcas.consumer.ConsumerEvent;
import org.blab.vcas.consumer.ConsumerProperties;

import java.util.HashMap;
import java.util.Map;

public class Datasource implements Consumer.Callback {
  private static Datasource instance;

  private final ObjectProperty<State> state;
  private final Map<String, Channel> channels;

  private Datasource(
      DatasourceProperties datasourceProperties, ConsumerProperties consumerProperties) {
    this.state = new SimpleObjectProperty<>(State.DISCONNECTED);
    this.channels = new HashMap<>(datasourceProperties.topicNames().size() / 2);
  }

  public static Datasource getInstance() {
    return instance;
  }

  public static void createInstance(
      DatasourceProperties datasourceProperties, ConsumerProperties consumerProperties) {
    instance = new Datasource(datasourceProperties, consumerProperties);
  }

  public ObjectProperty<State> getStateProperty() {
    return state;
  }

  public Channel getChannel(String name) {
    return channels.get(name);
  }

  @Override
  public void onEvent(ConsumerEvent event) {
    // TODO Process incoming event
  }

  @Override
  public void onConnectionEstablished() {
    state.set(State.CONNECTED);
  }

  @Override
  public void onConnectionLost() {
    state.set(State.DISCONNECTED);
  }

  @Override
  public void onError(Throwable e) {}

  public enum State {
    CONNECTED,
    IDLE,
    DISCONNECTED
  }
}

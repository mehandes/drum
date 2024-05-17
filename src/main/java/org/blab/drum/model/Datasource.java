package org.blab.drum.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.blab.vcas.consumer.*;

import java.util.HashMap;
import java.util.Map;

public class Datasource implements Consumer.Callback {
  private static final Logger logger = LogManager.getLogger(Datasource.class.getName());

  private static Datasource instance;

  private final ObjectProperty<State> state;
  private final Map<String, Channel> channels;
  private final Consumer consumer;

  private Datasource(
      DatasourceProperties datasourceProperties, ConsumerProperties consumerProperties) {
    this.state = new SimpleObjectProperty<>(State.DISCONNECTED);
    this.channels = new HashMap<>(datasourceProperties.topicNames().size() / 2);
    this.consumer = new VcasConsumer(consumerProperties, this);

    consumer.subscribe(datasourceProperties.topicNames());
  }

  public static Datasource getInstance() {
    return instance;
  }

  public static void createInstance(
      DatasourceProperties datasourceProperties, ConsumerProperties consumerProperties) {
    logger.debug("Instantiating...");
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
    logger.debug(event);
  }

  @Override
  public void onConnectionEstablished() {
    logger.info("Connected.");
    state.set(State.CONNECTED);
  }

  @Override
  public void onConnectionLost() {
    logger.warn("Disconnected.");
    state.set(State.DISCONNECTED);
  }

  @Override
  public void onError(Throwable e) {
    logger.error(e);
  }

  public enum State {
    CONNECTED,
    IDLE,
    DISCONNECTED
  }
}

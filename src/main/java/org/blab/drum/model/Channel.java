package org.blab.drum.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Channel {
  private final ObjectProperty<State> state;
  private final ObservableQueue<Double> currents;
  private final ObservableQueue<Double> voltages;

  public Channel(int capacity) {
    this.state = new SimpleObjectProperty<>(State.IDLE_ALL);
    this.currents = new ObservableQueue<>(capacity);
    this.voltages = new ObservableQueue<>(capacity);
  }

  public State getState() {
    return state.get();
  }

  public ObjectProperty<State> stateProperty() {
    return state;
  }

  public ObservableQueue<Double> getCurrents() {
    return currents;
  }

  public ObservableQueue<Double> getVoltages() {
    return voltages;
  }

  public enum State {
    NORMAL,
    IDLE_CURRENT,
    IDLE_VOLTAGE,
    IDLE_ALL,
    CRITICAL_CURRENT,
    CRITICAL_VOLTAGE,
    CRITICAL_ALL
  }
}

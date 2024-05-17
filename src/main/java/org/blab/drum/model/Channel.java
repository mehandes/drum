package org.blab.drum.model;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/** Channel for single VCAS topic which controls state and data. */
public class Channel {
  private final String name;
  private final ObjectProperty<State> state;
  private final ObservableQueue<Double> data;
  private final Range normalRange;

  private boolean isDataUpdated;

  public Channel(
      String name,
      int historySize,
      Range normalRange,
      int updateDelay,
      ScheduledExecutorService pool) {
    this.name = name;
    this.state = new SimpleObjectProperty<>(State.IDLE);
    this.data = new ObservableQueue<>(historySize);
    this.normalRange = normalRange;

    isDataUpdated = false;

    pool.scheduleAtFixedRate(this::updateState, updateDelay, updateDelay, TimeUnit.SECONDS);
  }

  public void addValue(Double v) {
    isDataUpdated = true;
    data.add(v);

    if (!normalRange.contains(v)) state.setValue(State.CRITICAL);
  }

  public String getName() {
    return name;
  }

  public ObservableQueue<Double> getObservableData() {
    return data;
  }

  public ObjectProperty<State> getObservableState() {
    return state;
  }

  private void updateState() {
    if (isDataUpdated) isDataUpdated = false;
    else state.setValue(State.IDLE);
  }

  public enum State {
    NORMAL,
    CRITICAL,
    IDLE;
  }
}

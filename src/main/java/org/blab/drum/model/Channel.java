package org.blab.drum.model;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.chart.XYChart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Channel {
  private static final Logger log = LogManager.getLogger(Channel.class);
  private final String name;
  private final ObjectProperty<State> state;
  private final NumericQueue data;
  private final ScheduledExecutorService pool;
  private final int criticalTimeoutMs;

  private boolean isDataUpdated = false;
  private boolean isCriticalTimeout = false;

  public Channel(
      String name,
      int historySize,
      int stateUpdateDelayMs,
      int criticalTimeoutMs,
      ScheduledExecutorService pool) {
    this.name = name;
    this.state = new SimpleObjectProperty<>(State.IDLE);
    this.data = new NumericQueue(historySize);
    this.pool = pool;
    this.criticalTimeoutMs = criticalTimeoutMs;

    pool.scheduleAtFixedRate(
        () -> {
          if (isDataUpdated) isDataUpdated = false;
          else if (!isCriticalTimeout) updateState(State.IDLE);
        },
        stateUpdateDelayMs,
        stateUpdateDelayMs,
        TimeUnit.MILLISECONDS);
  }

  public void addValue(Double value, String time) {
    if (!isCriticalTimeout)
      if (isCritical(value)) setCritical();
      else updateState(State.NORMAL);

    isDataUpdated = true;
    data.offer(new XYChart.Data<>(time, value));
  }

  private void setCritical() {
    updateState(State.CRITICAL);
    isCriticalTimeout = true;
    pool.schedule(
        () -> {
          isCriticalTimeout = false;
        },
        criticalTimeoutMs,
        TimeUnit.MILLISECONDS);
  }

  private boolean isCritical(double v) {
    return v > data.mean() + data.stdDev() * 3 || v < data.mean() - data.stdDev() * 3;
  }

  private void updateState(State state) {
    if (!this.state.getValue().equals(state)) this.state.setValue(state);
  }

  public String getName() {
    return name;
  }

  public NumericQueue getData() {
    return data;
  }

  public ObjectProperty<State> getState() {
    return state;
  }

  public enum State {
    NORMAL,
    CRITICAL,
    IDLE
  }
}

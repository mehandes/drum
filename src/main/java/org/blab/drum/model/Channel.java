package org.blab.drum.model;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.chart.XYChart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Channel for single VCAS topic which controls state and data. */
public class Channel {
  private static final Logger logger = LogManager.getLogger(Channel.class);

  private final String name;
  private final ObjectProperty<State> state;
  private final NumericQueue data;

  private boolean isDataUpdated;

  public Channel(String name, int historySize, int updateDelay, ScheduledExecutorService pool) {
    this.name = name;
    this.state = new SimpleObjectProperty<>(State.IDLE);
    this.data = new NumericQueue(historySize);
    isDataUpdated = false;

    pool.scheduleAtFixedRate(this::updateState, updateDelay, updateDelay, TimeUnit.SECONDS);
  }

  public void addValue(Double value, String time) {
    if (value > data.mean() + data.stdDev() * 3 || value < data.mean() - data.stdDev() * 3)
      setState(State.CRITICAL);
    else setState(State.NORMAL);

    isDataUpdated = true;
    data.offer(new XYChart.Data<>(time, value));
  }

  private void setState(State state) {
    if (this.state.getValue().equals(state)) return;
    this.state.setValue(state);
  }

  public String getName() {
    return name;
  }

  public NumericQueue getData() {
    return data;
  }

  public ObjectProperty<State> getObservableState() {
    return state;
  }

  private void updateState() {
    if (isDataUpdated) isDataUpdated = false;
    else {
      logger.error("IDLE");
      state.setValue(State.IDLE);
    }
  }

  public enum State {
    NORMAL,
    CRITICAL,
    IDLE;
  }
}

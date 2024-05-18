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
  private final ObservableQueue<XYChart.Data<String, Number>> data;
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

  public void addValue(Double value, String time) {
    isDataUpdated = true;
    data.offer(new XYChart.Data<>(time, value));

    if (!normalRange.contains(value)) state.setValue(State.CRITICAL);
    else state.setValue(State.NORMAL);
  }

  public String getName() {
    return name;
  }

  public ObservableQueue<XYChart.Data<String, Number>> getObservableData() {
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

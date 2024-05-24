package org.blab.drum.model;

import javafx.scene.chart.XYChart;

public class NumericQueue extends ObservableQueue<XYChart.Data<String, Number>> {
  private double min = 0;
  private double max = 0;
  private double sum = 0;
  private double mean = 0;
  private double stdDev = 0;

  public NumericQueue(int capacity) {
    super(capacity);
  }

  @Override
  public boolean offer(XYChart.Data<String, Number> sample) {
    if (isFull()) sum -= getFirst().getYValue().doubleValue();
    boolean r = super.offer(sample);

    sum += sample.getYValue().doubleValue();
    mean = sum / size();

    min = Double.MAX_VALUE;
    max = Double.MIN_VALUE;
    stdDev = 0;

    for (var p : this) {
      var v = p.getYValue().doubleValue();
      if (v < min) min = v;
      if (v > max) max = v;
      stdDev += Math.pow(v - mean, 2);
    }

    stdDev = Math.sqrt(stdDev / size());

    return r;
  }

  public double max() {
    return max;
  }

  public double min() {
    return min;
  }

  public double sum() {
    return sum;
  }

  public double mean() {
    return mean;
  }

  public double stdDev() {
    return stdDev;
  }
}

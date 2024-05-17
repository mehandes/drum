package org.blab.drum.model;

public record Range(double min, double max) {
  public boolean contains(double v) {
    return min <= v && v <= max;
  }

  public static Range of(double min, double max) {
    return new Range(min, max);
  }
}

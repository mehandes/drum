package org.blab.drum.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.*;

/** Collection of related Channels with bounded states. */
public class ChannelGroup {
  private final String name;
  private final ObjectProperty<State> state;
  private final Map<String, Channel> channels;

  private int criticalCount = 0;
  private int idleCount = 0;

  public ChannelGroup(String name) {
    this.name = name;
    this.channels = new HashMap<>();
    this.state = new SimpleObjectProperty<>(State.IDLE);
  }

  public void addChannel(Channel channel) {
    if (channels.containsKey(channel.getName())) return;
    channels.put(channel.getName(), channel);
    channel.getObservableState().addListener((obs, o, n) -> {
      switch (o) {
        case CRITICAL -> criticalCount--;
        case IDLE -> idleCount--;
      }

      switch (n) {
        case CRITICAL -> criticalCount++;
        case IDLE -> idleCount++;
      }

      if (criticalCount == 0 && idleCount == 0) state.setValue(State.NORMAL);
      else if (criticalCount >= idleCount) state.setValue(State.CRITICAL);
      else state.setValue(State.IDLE);
    });
  }

  public String getName() {
    return name;
  }

  public ObjectProperty<State> getObservableState() {
    return state;
  }

  public Map<String, Channel> getChannels() {
    return channels;
  }

  public Channel getChannelByName(String name) {
    return channels.get(name);
  }

  public List<Channel> getCriticalChannels() {
    return channels.values().stream()
        .filter(c -> c.getObservableState().getValue().equals(Channel.State.CRITICAL))
        .toList();
  }

  public List<Channel> getIdleChannels() {
    return channels.values().stream()
        .filter(c -> c.getObservableState().getValue().equals(Channel.State.IDLE))
        .toList();
  }

  public enum State {
    NORMAL,
    CRITICAL,
    IDLE
  }
}

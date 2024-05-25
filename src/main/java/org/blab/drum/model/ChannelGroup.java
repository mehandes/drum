package org.blab.drum.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class ChannelGroup {
  private final String name;
  private final ObjectProperty<State> state;
  private final Map<String, Channel> channels;
  private final ReentrantLock stateLock;

  private int criticalCount = 0;
  private int idleCount = 0;

  public ChannelGroup(String name) {
    this.name = name;
    this.channels = new HashMap<>();
    this.state = new SimpleObjectProperty<>(State.IDLE);
    this.stateLock = new ReentrantLock();
  }

  public void add(Channel channel) {
    if (channels.containsKey(channel.getName())) return;

    channels.put(channel.getName(), channel);
    syncChannelState(channel);
  }

  private void syncChannelState(Channel channel) {
    if (channel.getState().getValue().equals(Channel.State.IDLE)) idleCount++;
    else if (channel.getState().getValue().equals(Channel.State.CRITICAL)) criticalCount++;

    channel
        .getState()
        .addListener(
            (obs, o, n) -> {
              stateLock.lock();

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

              stateLock.unlock();
            });
  }

  public String getName() {
    return name;
  }

  public ObjectProperty<State> getState() {
    return state;
  }

  public Map<String, Channel> getChannels() {
    return channels;
  }

  public Channel getChannel(String name) {
    return channels.get(name);
  }

  public List<Channel> getCriticalChannels() {
    return channels.values().stream()
        .filter(c -> c.getState().getValue().equals(Channel.State.CRITICAL))
        .toList();
  }

  public List<Channel> getIdleChannels() {
    return channels.values().stream()
        .filter(c -> c.getState().getValue().equals(Channel.State.IDLE))
        .toList();
  }

  public enum State {
    NORMAL,
    CRITICAL,
    IDLE
  }
}

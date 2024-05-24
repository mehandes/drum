package org.blab.drum.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/** Collection of related Channels with bounded states. */
public class ChannelGroup {
  private static final Logger logger = LogManager.getLogger(ChannelGroup.class);

  private final String name;
  private final ObjectProperty<State> state;
  private final Map<String, Channel> channels;
  private final ReentrantLock lock;

  private int criticalCount = 0;
  private int idleCount = 0;

  public ChannelGroup(String name) {
    this.name = name;
    this.channels = new HashMap<>();
    this.state = new SimpleObjectProperty<>(State.IDLE);
    this.lock = new ReentrantLock();
  }

  public void addChannel(Channel channel) {
    if (channels.containsKey(channel.getName())) return;
    channels.put(channel.getName(), channel);

    if (channel.getObservableState().getValue().equals(Channel.State.IDLE)) idleCount++;
    else if (channel.getObservableState().getValue().equals(Channel.State.CRITICAL))
      criticalCount++;

    channel
        .getObservableState()
        .addListener(
            (obs, o, n) -> {
              lock.lock();

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

              logger.debug("Group state: {}", state.getValue());

              lock.unlock();
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

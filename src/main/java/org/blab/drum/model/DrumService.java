package org.blab.drum.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.blab.vcas.consumer.ConsumerEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class DrumService extends VcasService {
  private static final Logger logger = LogManager.getLogger(DrumService.class);
  private static DrumService instance;

  public static void init(DrumProperties properties) {
    instance = new DrumService(properties);
  }

  public static DrumService getInstance() {
    if (instance == null) throw new IllegalStateException("Uninitialized DrumService");
    return instance;
  }

  private final Map<String, ChannelGroup> groups;

  private DrumService(DrumProperties properties) {
    super(properties.consumerProperties());

    var pool = Executors.newScheduledThreadPool(1);
    groups = new HashMap<>();

    properties
        .topics()
        .forEach(
            t -> {
              String groupName = getGroupNameFromTopic(t);
              String channelName = getChannelNameFromTopic(t);

              if (!groups.containsKey(groupName))
                groups.put(groupName, new ChannelGroup(groupName));

              if (groups.get(groupName).getChannelByName(channelName) == null)
                groups
                    .get(groupName)
                    .addChannel(
                        new Channel(
                            channelName,
                            properties.channelHistorySize(),
                            properties.valuesNormalRange(),
                            properties.channelStateUpdateDelay(),
                            pool));
            });

    eventConsumer.subscribe(properties.topics());
  }

  public ChannelGroup getGroupByName(String groupName) {
    return groups.get(groupName);
  }

  public Map<String, ChannelGroup> getGroups() {
    return groups;
  }

  @Override
  public void onEvent(ConsumerEvent event) {
    groups
        .get(getGroupNameFromTopic(event.topic()))
        .getChannelByName(getChannelNameFromTopic(event.topic()))
        .addValue(Double.parseDouble(event.value()), parseTimestamp(event.timestamp()));
  }

  private String parseTimestamp(Long timestamp) {
    return new SimpleDateFormat("HH:mm:ss").format(new Date(timestamp));
  }

  private String getGroupNameFromTopic(String topic) {
    return topic.substring(0, topic.lastIndexOf('/'));
  }

  private String getChannelNameFromTopic(String topic) {
    return topic.substring(topic.lastIndexOf('/') + 1);
  }

  @Override
  public void onError(Throwable e) {
    logger.error(e);
  }
}

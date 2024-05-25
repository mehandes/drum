package org.blab.drum.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.blab.drum.DrumProperties;
import org.blab.vcas.consumer.ConsumerEvent;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
    super(properties.bootstrapServer().toAddress());

    groups = new LinkedHashMap<>();

    ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
    Set<String> subscriptions = new HashSet<>();

    properties
        .groups()
        .forEach(
            g -> {
              groups.put(g, new ChannelGroup(g));

              properties
                  .channels()
                  .forEach(
                      c -> {
                        groups
                            .get(g)
                            .add(
                                new Channel(
                                    c,
                                    properties.channelHistorySize(),
                                    properties.channelStateUpdateDelayMs(),
                                    properties.channelCriticalTimeoutMs(),
                                    pool));

                        subscriptions.add(String.format("%s/%s/%s", properties.namespace(), g, c));
                      });
            });

    eventConsumer.subscribe(subscriptions);
  }

  public Map<String, ChannelGroup> getGroups() {
    return groups;
  }

  @Override
  public void onEvent(ConsumerEvent event) {
    groups
        .get(event.topic().split("/")[2])
        .getChannel(event.topic().split("/")[3])
        .addValue(Double.parseDouble(event.value()), parseTimestamp(event.timestamp()));
  }

  private String parseTimestamp(Long timestamp) {
    return new SimpleDateFormat("HH:mm:ss").format(new Date(timestamp));
  }

  @Override
  public void onError(Throwable e) {
    logger.error(e);
  }
}

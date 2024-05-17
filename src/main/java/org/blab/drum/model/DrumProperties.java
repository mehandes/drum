package org.blab.drum.model;

import org.blab.vcas.consumer.ConsumerProperties;

import java.util.Set;

public record DrumProperties(
    Set<String> topics,
    int channelHistorySize,
    Range valuesNormalRange,
    int channelStateUpdateDelay,
    ConsumerProperties consumerProperties) {}

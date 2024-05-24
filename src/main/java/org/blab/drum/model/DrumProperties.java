package org.blab.drum.model;

import java.net.InetSocketAddress;
import java.util.Set;

public record DrumProperties(
    Set<String> topics,
    int channelHistorySize,
    int channelStateUpdateDelay,
    InetSocketAddress address) {}

package org.blab.vcas.consumer;

import java.net.InetSocketAddress;

public record ConsumerProperties(
    InetSocketAddress address, int maxMessageSize, int reconnectTimeoutMs) {}

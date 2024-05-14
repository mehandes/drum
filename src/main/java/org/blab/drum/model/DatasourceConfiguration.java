package org.blab.drum.model;

import java.net.InetSocketAddress;
import java.util.Set;

public record DatasourceConfiguration(
    InetSocketAddress address, Set<String> channelNames, int persistenceRange) {}

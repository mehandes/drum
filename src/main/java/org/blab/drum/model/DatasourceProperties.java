package org.blab.drum.model;

import java.util.Set;

public record DatasourceProperties(Set<String> topicNames, int storageSize) {}

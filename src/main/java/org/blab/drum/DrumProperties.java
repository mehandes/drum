package org.blab.drum;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;

public record DrumProperties(
    String namespace,
    List<String> groups,
    Set<String> channels,
    int gridWidth,
    int channelHistorySize,
    int channelStateUpdateDelayMs,
    int channelCriticalTimeoutMs,
    ServerProperties bootstrapServer) {
  public static DrumProperties load(InputStreamReader inputStream) {
    return new Gson().fromJson(inputStream, DrumProperties.class);
  }

  public record ServerProperties(String hostname, int port) {
    public InetSocketAddress toAddress() {
      return new InetSocketAddress(hostname, port);
    }
  }
}

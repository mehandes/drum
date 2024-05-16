package org.blab.vcas.consumer;

import org.blab.vcas.MessageFormatException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/** Parsed VCAS message. */
public record ConsumerEvent(String topic, String value, String description, Long timestamp) {
  private static final String TIME_FORMAT = "dd.MM.yyyy HH_mm_ss.SSS";

  public static ConsumerEvent parse(String message) {
    Map<String, String> map =
        Arrays.stream(message.split("\\|"))
            .map(s -> s.split(":"))
            .collect(Collectors.toMap(s -> s[0], s -> s[1]));

    if (!map.containsKey("name") || !map.containsKey("val") || !map.containsKey("time"))
      throw new MessageFormatException("Required field missing: " + message);

    return new ConsumerEvent(
        map.get("name"),
        map.get("val"),
        map.getOrDefault("descr", null),
        parseTime(map.get("time")));
  }

  private static Long parseTime(String time) {
    try {
      return new SimpleDateFormat(TIME_FORMAT).parse(time).getTime();
    } catch (ParseException e) {
      throw new MessageFormatException("Invalid time format: " + time);
    }
  }
}

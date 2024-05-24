package org.blab.vcas;

public class UnknownTopicException extends InvalidMetadataException {
  public UnknownTopicException(String message, Throwable cause) {
    super(message, cause);
  }

  public UnknownTopicException(Throwable cause) {
    super(cause);
  }
}

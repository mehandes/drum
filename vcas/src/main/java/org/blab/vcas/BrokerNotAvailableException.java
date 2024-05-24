package org.blab.vcas;

public class BrokerNotAvailableException extends ServerException {
  public BrokerNotAvailableException(Throwable cause) {
    super(cause);
  }

  public BrokerNotAvailableException(String message, Throwable cause) {
    super(message, cause);
  }
}

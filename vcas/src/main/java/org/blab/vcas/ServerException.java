package org.blab.vcas;

public class ServerException extends VcasException {
  public ServerException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServerException(Throwable cause) {
    super(cause);
  }
}

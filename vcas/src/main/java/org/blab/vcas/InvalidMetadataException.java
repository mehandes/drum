package org.blab.vcas;

public class InvalidMetadataException extends VcasException {
  public InvalidMetadataException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidMetadataException(Throwable cause) {
    super(cause);
  }
}

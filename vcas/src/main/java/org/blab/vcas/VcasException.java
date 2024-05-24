package org.blab.vcas;

public class VcasException extends RuntimeException {
  public VcasException(String message, Throwable cause) {
    super(message, cause);
  }

  public VcasException(Throwable cause) {
    super(cause);
  }
}

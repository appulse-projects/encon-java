
package io.appulse.encon.java.config.exception;


import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = false)
public class NoSuchMailboxHandlerException extends RuntimeException {

  private static final long serialVersionUID = 9031562526640890127L;

  String classPath;

  public NoSuchMailboxHandlerException (Throwable cause, String classPath) {
    super(cause);
    this.classPath = classPath;
  }
}

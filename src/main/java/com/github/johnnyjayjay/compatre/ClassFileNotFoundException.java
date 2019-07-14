package com.github.johnnyjayjay.compatre;

/**
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public final class ClassFileNotFoundException extends ClassTransformationException {

  public ClassFileNotFoundException() {
    super();
  }

  public ClassFileNotFoundException(String message) {
    super(message);
  }

  public ClassFileNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public ClassFileNotFoundException(Throwable cause) {
    super(cause);
  }

  protected ClassFileNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}

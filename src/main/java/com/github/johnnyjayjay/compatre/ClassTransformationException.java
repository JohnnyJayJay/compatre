package com.github.johnnyjayjay.compatre;

/**
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public class ClassTransformationException extends RuntimeException {

  public ClassTransformationException() {
    super();
  }

  public ClassTransformationException(String message) {
    super(message);
  }

  public ClassTransformationException(String message, Throwable cause) {
    super(message, cause);
  }

  public ClassTransformationException(Throwable cause) {
    super(cause);
  }

  protected ClassTransformationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
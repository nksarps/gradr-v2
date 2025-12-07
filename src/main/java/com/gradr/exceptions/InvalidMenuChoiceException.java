package com.gradr.exceptions;

public class InvalidMenuChoiceException extends RuntimeException {
  public InvalidMenuChoiceException(String message) {
    super(message);
  }
}

package com.openglobes.core.trader;

public class WrongOrderIdException extends TraderException {

    private static final long serialVersionUID = 2166387843820193L;

    public WrongOrderIdException(String message) {
        super(message);
    }

    public WrongOrderIdException(String message,
                                 Throwable cause) {
        super(message,
              cause);
    }

    public WrongOrderIdException(Throwable cause) {
        super(cause);
    }

    public WrongOrderIdException(String message,
                                 Throwable cause,
                                 boolean enableSuppression,
                                 boolean writableStackTrace) {
        super(message,
              cause,
              enableSuppression,
              writableStackTrace);
    }
}

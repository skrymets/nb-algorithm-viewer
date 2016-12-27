package org.netbeans.module.sandbox.dot;

public class DotBridgeException extends RuntimeException {

    private static final long serialVersionUID = -7973287545256636178L;

    public DotBridgeException() {
    }

    public DotBridgeException(String message) {
        super(message);
    }

    public DotBridgeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DotBridgeException(Throwable cause) {
        super(cause);
    }

    public DotBridgeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}

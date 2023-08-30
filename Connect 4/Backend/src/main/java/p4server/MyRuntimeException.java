package p4server;

public class MyRuntimeException extends RuntimeException {
	private static final long serialVersionUID = -8284438944814871770L;

	public MyRuntimeException() {
		super();
	}

	public MyRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MyRuntimeException(String message) {
		super(message);
	}

	public MyRuntimeException(Throwable cause) {
		super(cause);
	}
}

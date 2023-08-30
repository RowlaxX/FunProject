package p4server.endpoint;

public class EndpointException extends RuntimeException {
	private static final long serialVersionUID = 7613456062302526741L;

	public EndpointException() {
		super();
	}

	public EndpointException(String message, Throwable cause) {
		super(message, cause);
	}

	public EndpointException(String message) {
		super(message);
	}

	public EndpointException(Throwable cause) {
		super(cause);
	}
}

package util;

/**
 * A custom Exception
 * 
 * @author Adam Lechovský
 *
 */
public class NoIntersectionException extends Exception {

	private static final long serialVersionUID = -3899865746568487774L;

	public NoIntersectionException() {
		super();
	}

	public NoIntersectionException(String message) {
		super(message);
	}

	public NoIntersectionException(Throwable cause) {
		super(cause);
	}

	public NoIntersectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoIntersectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

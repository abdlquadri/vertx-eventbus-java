package ng.abdlquadri.eventbus.handlers;

/**
 * The result of an operation. Can be used as 
 * Handler<AsyncResult<Boolean>> or
 * Handler<AsyncResult<String>>
 * instead of all sorts of different handlers.
 * 
 * @author nielsbaloe
 *
 * @param <T>
 */
public class AsyncResult<T> {

	/**
	 * The result. Will be null when succeeded is false.
	 */
	public T result;

	/**
	 * The error. Will be null when succeeded is true.
	 */
	public Throwable cause;

	/**
	 * Whether or not the operation succeeded.
	 */
	public boolean success;

}

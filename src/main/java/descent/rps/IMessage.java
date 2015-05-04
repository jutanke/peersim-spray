package descent.rps;

/**
 * A basic message interface
 */
public interface IMessage {

	/**
	 * getter of the payload of the message, i.e., the information contained
	 * inside the message
	 * 
	 * @return an Object which must be carefully cast afterwards
	 */
	public Object getPayload();
}

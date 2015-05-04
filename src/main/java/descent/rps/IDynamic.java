package descent.rps;

/**
 * Functions describing the state of a peer, i.e., if it is alive, or not.
 */
public interface IDynamic {

	/**
	 * Checks if the peer is alive
	 * 
	 * @return true if the peer is alive, false otherwise
	 */
	public boolean isUp();

	/**
	 * Checks if the peer crashed/left
	 * 
	 * @return true if the peer crashed/left, false otherwise
	 */
	public boolean isDown();

}

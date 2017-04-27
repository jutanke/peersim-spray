package descent.clocks;

/**
 * Logical clock that allow broadcast protocols to order events.
 */
public interface IClock {

	/**
	 * Checks if the 'other' clock is causally ready to be delivered.
	 * 
	 * @param other
	 *            the other clock to check
	 * @return true if it is, false if it must wait.
	 */
	boolean isReady(IClock other);

}

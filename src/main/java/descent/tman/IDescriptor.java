package descent.tman;

public interface IDescriptor {

	/**
	 * Give a score of this descriptor compared to the one in parameter. Using
	 * these score, descriptors can be ranked.
	 * 
	 * @param other
	 *            the other descriptor
	 * @return the score
	 */
	double ranking(IDescriptor other);
}

package descent.slicer;

import descent.tman.IDescriptor;

public class RankDescriptor implements IDescriptor {

	public Integer rank;

	// (TODO) writing frequency (SWAP parameter)

	public double ranking(IDescriptor other) {
		RankDescriptor o = (RankDescriptor) other;
		if (o.rank > this.rank + 1) {
			return 0;
		} else {
			return 1 - Math.abs(o.rank - this.rank + 1);
		}
	}

}

package descent.slicer;

import descent.tman.IDescriptor;

public class RankDescriptor implements IDescriptor {

	public Integer rank;

	// (TODO) writing frequency (SWAP parameter)

	public RankDescriptor() {
		this.rank = Integer.MAX_VALUE;
	}

	public double ranking(IDescriptor other) {
		RankDescriptor o = (RankDescriptor) other;

		if (o.rank > this.rank + 1 || o.rank.equals(Integer.MAX_VALUE) || this.rank.equals(Integer.MAX_VALUE)) {
			return Integer.MAX_VALUE;
		} else {
			// System.out.println("o"  +  o.rank);
			// System.out.println("t" + this.rank);
			return Math.abs(o.rank - (this.rank + 1));
		}
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

}

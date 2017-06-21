package descent.slicer;

import descent.tman.IDescriptor;

public class RankDescriptor implements IDescriptor, Comparable<RankDescriptor> {

	public Integer rank;
	public Double frequency;

	public RankDescriptor() {
		this.rank = Integer.MAX_VALUE;
		this.frequency = 0.;
	}

	public double ranking(IDescriptor other) {
		RankDescriptor o = (RankDescriptor) other;

		if (o.rank > this.rank + 1 || o.rank.equals(Integer.MAX_VALUE) || this.rank.equals(Integer.MAX_VALUE)) {
			return Integer.MAX_VALUE;
		} else {
			return this.distance(o);
		}
	}

	public double distance(RankDescriptor o) {
		return Math.abs(o.rank + 1 - (this.rank));
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public void setFrequency(Double frequency) {
		this.frequency = frequency;
	}

	public int compareTo(RankDescriptor o) {
		if (this.frequency > o.frequency) {
			return 1;
		} else if (this.frequency < o.frequency) {
			return -1;
		} else {
			return 0;
		}
	}
}

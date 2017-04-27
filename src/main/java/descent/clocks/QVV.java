package descent.clocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Quasi-version-vector that provides quasi-causality tracking to set of events,
 * ie events are ordered but some may be wrongly considered as ready due to
 * overlapping entries.
 */
public class QVV extends ArrayList<Integer> implements IClock {

	public HashSet<Integer> entries;

	public QVV(Integer size) {
		// (TODO)
	}

	public QVV(Set<Integer> entries) {
		this.entries = new HashSet<Integer>(entries);
	}

	public boolean isReady(IClock other) {
		// TODO Auto-generated method stub
		return false;
	}

}

package descent.clocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import peersim.core.CommonState;

/**
 * Quasi-version-vector that provides quasi-causality tracking to set of events,
 * ie events are ordered but some may be wrongly considered as ready due to
 * overlapping entries.
 */
public class QVV extends ArrayList<Integer> implements IClock {

	public HashSet<Integer> entries;

	public QVV(Integer size) {
		for (int i = 0; i < size; ++i) {
			this.add(new Integer(0));
		}
		this.entries = this.provideEntries(1);
	}

	public QVV(Integer size, Set<Integer> entries) {
		for (int i = 0; i < size; ++i) {
			this.add(new Integer(0));
		}
		this.entries = new HashSet<Integer>(entries);
	}

	private HashSet<Integer> provideEntries(Integer k) {
		HashSet<Integer> entries = new HashSet<Integer>();
		while (k < entries.size()) {
			entries.add(new Integer(CommonState.r.nextInt() * this.size()));
		}
		return entries;
	}

	public boolean isReady(IClock other) {
		// TODO Auto-generated method stub
		return false;
	}

}

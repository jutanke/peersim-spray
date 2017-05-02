package descent.spray;

import java.util.HashSet;

/**
 * Dendograms are graphs used in cluster differentiation.
 *
 */
public class History<T> {

	// Identifier of the resulting cluster
	HashSet<String> name;

	// Store data
	T data;

	// generators
	History<T> left;
	History<T> right;

	public History(History<T> left, History<T> right) {
		this.name = new HashSet<String>();
		this.name.addAll(left.name);
		this.name.addAll(right.name);

		this.left = left;
		this.right = right;
	}

	public History(String name) {
		this.name = new HashSet<String>();
		this.name.add(name);

		this.left = null;
		this.right = null;
	}

	public History(HashSet<String> name) {
		this.name = new HashSet<String>();
		this.name.addAll(name);

		this.left = null;
		this.right = null;
	}

	public void setData(T data) {
		this.data = data;
	}

	public HashSet<String> getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name.clear();
		this.name.add(name);
	}

	public T getData() {
		return this.data;
	}

	public T getData(HashSet<String> name) {
		if (this.name.equals(name)) {
			return this.data;
		} else {
			T leftData = this.left.getData(name);

			if (leftData != null) {
				return leftData;
			} else {
				return this.right.getData(name);
			}
		}
	}

	public History<T> clone() {
		return new History<T>(this.name);
	}

	public History<T> merge(HashSet<String> name) {
		History<T> right = new History<T>(name);
		History<T> newOne = new History<T>(this, right);
		return newOne;
	}

	public boolean isEqual(History<T> other) {
		return this.name.equals(other.name);
	}

	// other < this
	public boolean isLower(History<T> other) {
		return this.name.containsAll(other.name) && !other.name.containsAll(this.name);
	}

	public boolean isDifferent(History<T> other) {
		boolean found = false;
		for (String id : this.name) {
			if (other.name.contains(id)) {
				found = true;
			}
		}
		for (String id : other.name) {
			if (this.name.contains(id)) {
				found = true;
			}
		}
		return !found;
	}

}

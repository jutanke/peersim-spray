package descent.scamplon;

import java.util.Comparator;

import peersim.core.Node;

// ============================================
// E N T R Y
// ============================================
public final class PartialViewEntry implements Comparable<PartialViewEntry>,
		Comparator<PartialViewEntry> {
	public Node node;
	public int age;
	public boolean isVolatile;

	public PartialViewEntry(Node n) {
		this.node = n;
		this.age = 0;
		this.isVolatile = false;
	}

	public final PartialViewEntry clone() {
		PartialViewEntry result = new PartialViewEntry(this.node);
		result.age = this.age;
		result.isVolatile = false;
		return result;
	}

	@Override
	public String toString() {
		return "{" + node.getID() + "|" + age + "|" + (isVolatile ? "y" : "n")
				+ "}";
	}

	public int compare(PartialViewEntry entry, PartialViewEntry t1) {
		return Integer.compare(entry.age, t1.age);
	}

	public int compareTo(PartialViewEntry entry) {
		return ((Integer) this.age).compareTo(entry.age);
	}
}
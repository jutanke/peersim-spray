package descent.scamp;

import peersim.core.CommonState;
import peersim.core.Node;

public class ViewEntry {
	public final long id;
	public final long birthdate;
	public final Node node;
	public final long leaseTime;

	public ViewEntry(Node n) {
		this.node = n;
		this.id = n.getID();
		this.birthdate = CommonState.getTime();
		this.leaseTime = CommonState.r.nextLong(View.leaseTimeoutMax
				- View.leaseTimeoutMin)
				+ View.leaseTimeoutMin;
	}

	@Override
	public String toString() {
		return "{id:" + this.id + ":lifetime:"
				+ ((this.birthdate + this.leaseTime) - CommonState.getTime())
				+ "}";
	}

	public boolean timeout() {
		return ((this.birthdate + this.leaseTime) < CommonState.getTime());
	}
}
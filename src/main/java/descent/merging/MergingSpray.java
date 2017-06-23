package descent.merging;

import descent.rps.IMessage;
import descent.spray.Spray;
import peersim.core.Node;

/**
 * Add merging behavior to Spray (TODO)
 */
public class MergingSpray extends Spray {

	// #A Configuration from peersim config file
	// #B Local variables
	public MergingRegister register;

	public MergingSpray(String prefix) {
		super(prefix);
		this.register = new MergingRegister();
	}

	public MergingSpray() {
		super();
		this.register = new MergingRegister();
	}

	public void periodicCall() {
		// #1 Check if must merge networks
		// this.onMerge(this.register.isMerge((SprayMessage) received,
		// this.partialView.size()), q);
		// this.onMergeBis(q);

		super.periodicCall();
	}

	public IMessage onPeriodicCall(Node origin, IMessage message) {
		// #0 Check there is a network merging in progress
		if (this.register.shouldMerge()) {
			Double toInject = this.register.getArcNumber(
					new MergingMessage(this.register.getNetworkId(), (double) this.partialView.size(), this.A, this.B));
			this.inject(toInject, 0., this.partialView.getLowestOcc());
		}
		return null;
	}

	public void startMerge(Node contact) {
		// #0 replace an arc of ours with contact (TODO)
		// #1 aggregate value from direct neighbors (TODO)
		Double aggregate = new Double(this.partialView.size());
		// #2 create the merging message to send
		MergingMessage m = new MergingMessage(this.register.getNetworkId(), aggregate, this.A, this.B);
		// #3 send it
		MergingSpray contactSpray = (MergingSpray) contact.getProtocol(MergingSpray.pid);
		contactSpray.onStartMerge(this.node, m);
	}

	private void onStartMerge(Node origin, MergingMessage m) {
		// #1 aggregate on the size of partial views (TODO)
		Double aggregate = new Double(this.partialView.size());
		// #2 create the merging message to send to the counterpart
		MergingMessage r = new MergingMessage(this.register.getNetworkId(), aggregate, this.A, this.B);
		// #3 send it
		MergingSpray originSpray = (MergingSpray) origin.getProtocol(MergingSpray.pid);
		originSpray.onMerge(m);

		this.onMerge(m);
	}

	private void onMerge(MergingMessage m) {
		// #1 integrate the data to our register
		if (this.register.isToMerge(m)) {
			this.register.add(m);
			// #2 forward the message to our neighbors
			for (Node neighbor : this.getAliveNeighbors()) {
				MergingSpray neighborSpray = (MergingSpray) neighbor.getProtocol(MergingSpray.pid);
				neighborSpray.onMerge(m);
			}
		}
	}

}

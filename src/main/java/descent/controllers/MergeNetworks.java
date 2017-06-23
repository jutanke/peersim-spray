package descent.controllers;

import descent.merging.MergingSpray;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Node;

public class MergeNetworks implements Control {

	private static final String PAR_DATE = "date";
	private static final String PAR_FROM = "from";
	private static final String PAR_TO = "to";

	private final int DATE;
	private final int FROM;
	private final int TO;

	public MergeNetworks(String n) {
		this.DATE = Configuration.getInt(n + "." + MergeNetworks.PAR_DATE, Integer.MAX_VALUE);
		this.FROM = Configuration.getInt(n + "." + MergeNetworks.PAR_FROM);
		this.TO = Configuration.getInt(n + "." + MergeNetworks.PAR_TO);
	}

	public boolean execute() {
		if (CommonState.getTime() == this.DATE) {
			// #1 choose a peer from each network
			final Node initiator = DynamicNetwork.networks.get(this.FROM)
					.get(CommonState.r.nextInt(DynamicNetwork.networks.get(this.FROM).size()));
			final Node contact = DynamicNetwork.networks.get(this.TO)
					.get(CommonState.r.nextInt(DynamicNetwork.networks.get(this.TO).size()));
			// #2 initiate the merge
			this.merge(initiator, contact);
		}
		return false;
	}

	/**
	 * Creates a bridge between two networks which will be used to merge them
	 * into one. The rest of the merge is done by the spray protocol itself.
	 * 
	 * @param initiator
	 *            the initiator node of the merge (from)
	 * @param contact
	 *            the contact node (to)
	 */
	private void merge(Node initiator, Node contact) {
		// #1 replace the oldest neighbor of initiator with the contact
		MergingSpray si = (MergingSpray) initiator.getProtocol(MergingSpray.pid);
		si.partialView.partialView.remove((int) 0);
		si.partialView.partialView.add((int) 0, contact);
		si.startMerge(contact);
	}

}

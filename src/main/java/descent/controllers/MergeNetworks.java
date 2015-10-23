package descent.controllers;

import descent.spray.Spray;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Node;

public class MergeNetworks implements Control {

	private static final String PAR_DATE = "date";
	private static final String PAR_PROTOCOL = "protocol";

	public final int DATE;
	protected final int pid;

	public MergeNetworks(String n) {
		this.DATE = Configuration.getInt(n + "." + MergeNetworks.PAR_DATE,
				Integer.MAX_VALUE);
		this.pid = Configuration.lookupPid(Configuration.getString(n + "."
				+ MergeNetworks.PAR_PROTOCOL));
	}

	public boolean execute() {
		if (CommonState.getTime() == this.DATE) {
			// #1 (TODO) choose networks
			final Node initiator = DynamicNetwork.networks.get(0).get(
					CommonState.r
							.nextInt(DynamicNetwork.networks.get(0).size()));
			final Node contact = DynamicNetwork.networks.get(1).get(
					CommonState.r
							.nextInt(DynamicNetwork.networks.get(1).size()));
			MergeNetworks.merge(initiator, contact);
			// #2 replace the oldest neighbor of initiator with the contact
			Spray si = (Spray) initiator.getProtocol(pid);
			si.partialView.partialView
					.remove(0);
			si.partialView.partialView.add(0,contact);
		}
		return false;
	}

	private static void merge(Node initiator, Node contact) {
		// #1 replace or add a bridge between networks

	}

}

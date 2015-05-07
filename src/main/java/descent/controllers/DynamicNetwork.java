package descent.controllers;

import java.util.LinkedList;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import descent.rps.ARandomPeerSamplingProtocol;
import descent.rps.IRandomPeerSampling;

/**
 * Controller that add and/or remove peers from the network over time
 */
public class DynamicNetwork implements Control {

	private static final String PAR_ADD_COUNT = "addingPerStep";
	private static final String PAR_ADD_PERC = "addingPerStepPerc";
	private static final String PAR_REM_COUNT = "removingPerStep";
	private static final String PAR_ADD_START = "startAdd";
	private static final String PAR_REM_START = "startRem";
	private static final String PAR_ADD_END = "endAdd";
	private static final String PAR_REM_END = "endRem";
	private static final String PAR_PROTOCOL = "protocol";

	public final int ADDING_PERCENT;
	public final int ADDING_COUNT;
	public final int REMOVING_COUNT;
	public final long ADDING_START;
	public final long REMOVING_START;
	public final long REMOVING_END;
	public final long ADDING_END;
	public final boolean IS_PERCENTAGE;
	protected static int pid;

	public static LinkedList<Node> graph = new LinkedList<Node>();
	public static LinkedList<Node> availableNodes = new LinkedList<Node>();

	public DynamicNetwork(String n) {
		// #A initialize all the variable from the configuration file
		this.ADDING_COUNT = Configuration.getInt(n + "."
				+ DynamicNetwork.PAR_ADD_COUNT, -1);
		this.ADDING_PERCENT = Configuration.getInt(n + "."
				+ DynamicNetwork.PAR_ADD_PERC, -1);
		this.REMOVING_COUNT = Configuration.getInt(n + "."
				+ DynamicNetwork.PAR_REM_COUNT, 0);
		this.ADDING_START = Configuration.getInt(n + "."
				+ DynamicNetwork.PAR_ADD_START, Integer.MAX_VALUE);
		this.REMOVING_START = Configuration.getInt(n + "."
				+ DynamicNetwork.PAR_REM_START, Integer.MAX_VALUE);
		this.REMOVING_END = Configuration.getInt(n + "."
				+ DynamicNetwork.PAR_REM_END, Integer.MAX_VALUE);
		this.ADDING_END = Configuration.getInt(n + "."
				+ DynamicNetwork.PAR_ADD_END, Integer.MAX_VALUE);
		this.IS_PERCENTAGE = this.ADDING_PERCENT != -1;

		final int nsize = Network.size();
		DynamicNetwork.pid = Configuration.lookupPid(Configuration.getString(n
				+ "." + DynamicNetwork.PAR_PROTOCOL));
		for (int i = 0; i < nsize; i++) {
			final Node node = Network.get(i);
			IRandomPeerSampling d = (IRandomPeerSampling) node.getProtocol(pid);
			d.leave();
			availableNodes.add(node);
			// System.err.println("Churn insert:" + this.ADDING_COUNT +
			// " [" + this.ADDING_START + ".." + this.ADDING_END + "]");
			// System.err.println("Churn remove:" + this.REMOVING_COUNT +
			// " [" + this.REMOVING_START + ".." + this.REMOVING_END + "]");
		}
	}

	public boolean execute() {
		final long currentTimestamp = CommonState.getTime();
		final boolean removingElements = currentTimestamp >= this.REMOVING_START
				&& currentTimestamp <= this.REMOVING_END;
		final boolean addingElements = currentTimestamp >= this.ADDING_START
				&& currentTimestamp <= this.ADDING_END;

		if (removingElements) {
			// REMOVE ELEMENTS
			for (int i = 0; i < this.REMOVING_COUNT
					&& DynamicNetwork.graph.size() > 0; i++) {
				final int pos = CommonState.r.nextInt(DynamicNetwork.graph
						.size());
				final Node rem = DynamicNetwork.graph.get(pos);
				DynamicNetwork.removeNode(rem);
				ARandomPeerSamplingProtocol d = (ARandomPeerSamplingProtocol) rem
						.getProtocol(pid);
				if (d.isUp()) {
					d.leave();
				}
				DynamicNetwork.graph.remove(pos);
				DynamicNetwork.availableNodes.push(rem);
			}
		}

		if (addingElements) {
			// ADD ELEMENTS

			if (this.IS_PERCENTAGE) {

				final double log10 = Math.floor(Math.log10(DynamicNetwork.graph
						.size()));
				final double dev10 = Math.pow(10, log10);
				int count = Math.max(1, (int) dev10 / this.ADDING_PERCENT);
				System.err.println("QQ:" + graph.size() + "," + log10 + ","
						+ dev10 + "," + count);
				for (int i = 0; i < count
						&& DynamicNetwork.availableNodes.size() > 0; i++) {
					insert();
				}

			} else {
				for (int i = 0; i < this.ADDING_COUNT
						&& DynamicNetwork.availableNodes.size() > 0; i++) {
					insert();
				}
			}
		}

		return false;
	}

	private void insert() {
		final Node current = DynamicNetwork.availableNodes.poll();
		if (graph.size() > 0) {
			final Node contact = getNode();
			DynamicNetwork.addNode(current, contact);
		} else {
			DynamicNetwork.addNode(current, null);
		}
		DynamicNetwork.graph.add(current);
	}

	public static Node getNode() {
		return DynamicNetwork.graph.get(CommonState.r
				.nextInt(DynamicNetwork.graph.size()));
	}

	public static void removeNode(Node leaver) {
		ARandomPeerSamplingProtocol leaverProtocol = (ARandomPeerSamplingProtocol) leaver
				.getProtocol(ARandomPeerSamplingProtocol.pid);
		leaverProtocol.leave();
	}

	public static void addNode(Node joiner, Node contact) {
		ARandomPeerSamplingProtocol joinerProtocol = (ARandomPeerSamplingProtocol) joiner
				.getProtocol(ARandomPeerSamplingProtocol.pid);
		joinerProtocol.join(joiner, contact);
	}

}

package descent.controllers;

import java.util.LinkedList;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import descent.rps.ARandomPeerSamplingProtocol;
import descent.rps.IRandomPeerSampling;
import descent.spray.Spray;

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
	private static final String PAR_STEP = "stepDynamic";
	private static final String PAR_SIZE = "size";

	public final int STEP;
	public final int ADDING_PERCENT;
	public final int ADDING_COUNT;
	public final int REMOVING_COUNT;
	public final long ADDING_START;
	public final long REMOVING_START;
	public final long REMOVING_END;
	public final long ADDING_END;
	public final boolean IS_PERCENTAGE;
	public final int SIZE;
	protected final int pid;
	public final int NETWORK_ID;

	public static boolean once = false;
	public static LinkedList<Node> graph = new LinkedList<Node>();
	public static LinkedList<Node> availableNodes = new LinkedList<Node>();
	public LinkedList<Node> localGraph = new LinkedList<Node>();
	public static LinkedList<LinkedList<Node>> networks = new LinkedList<LinkedList<Node>>();

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
		this.SIZE = Configuration.getInt(n + "." + DynamicNetwork.PAR_SIZE,
				Integer.MAX_VALUE);
		this.STEP = Configuration.getInt(n + "." + DynamicNetwork.PAR_STEP, 1);

		this.NETWORK_ID = DynamicNetwork.networks.size();

		final int nsize = Network.size();
		this.pid = Configuration.lookupPid(Configuration.getString(n + "."
				+ DynamicNetwork.PAR_PROTOCOL));
		if (!DynamicNetwork.once) {
			for (int i = 0; i < nsize; i++) {
				final Node node = Network.get(i);
				IRandomPeerSampling d = (IRandomPeerSampling) node
						.getProtocol(pid);
				d.leave();
				DynamicNetwork.availableNodes.add(node);
			}
			DynamicNetwork.once = true;
		}
		DynamicNetwork.networks.add(this.localGraph);
	}

	public boolean execute() {

		final long currentTimestamp = CommonState.getTime();

		final boolean removingElements = currentTimestamp >= this.REMOVING_START
				&& currentTimestamp <= this.REMOVING_END;
		final boolean addingElements = currentTimestamp >= this.ADDING_START
				&& currentTimestamp <= this.ADDING_END;
		if ((((currentTimestamp - this.REMOVING_START) % this.STEP) == 0)
				&& removingElements) {
			// REMOVE ELEMENTS
			for (int i = 0; i < this.REMOVING_COUNT
					&& DynamicNetwork.graph.size() > 0; i++) {

				final int pos = CommonState.r.nextInt(DynamicNetwork.graph
						.size());
				final Node rem = DynamicNetwork.graph.get(pos);
				this.removeNode(rem);
				ARandomPeerSamplingProtocol d = (ARandomPeerSamplingProtocol) rem
						.getProtocol(pid);
				if (d.isUp()) {
					d.leave();
				}
				DynamicNetwork.graph.remove(pos);
				DynamicNetwork.availableNodes.push(rem);
			}
		}

		if ((((currentTimestamp - this.ADDING_START) % this.STEP) == 0)
				&& addingElements) {
			// ADD ELEMENTS
			if (this.IS_PERCENTAGE) {
				final double log10 = Math.floor(Math.log10(graph.size()));
				final double dev10 = Math.pow(10, log10);
				int count = Math.max(1, (int) dev10 / this.ADDING_PERCENT);
				for (int i = 0; i < count && availableNodes.size() > 0; i++) {
					insert();
				}

			} else {
				for (int i = 0; i < this.ADDING_COUNT
						&& availableNodes.size() > 0; i++) {
					insert();
				}
			}
		}

		return false;
	}

	private void insert() {
		if (this.SIZE > this.localGraph.size()) {
			final Node current = DynamicNetwork.availableNodes.poll();
			Spray s = (Spray) current.getProtocol(pid); // only work for spray
			s.register.networks.add(this.NETWORK_ID);
			if (localGraph.size() > 0) {
				final Node contact = getNode();
				this.addNode(current, contact);
			} else {
				this.addNode(current, null);
			}
			this.localGraph.add(current);
			DynamicNetwork.graph.add(current);
		}
	}

	public Node getNode() {
		return this.localGraph
				.get(CommonState.r.nextInt(this.localGraph.size()));
	}

	public void removeNode(Node leaver) {
		ARandomPeerSamplingProtocol leaverProtocol = (ARandomPeerSamplingProtocol) leaver
				.getProtocol(ARandomPeerSamplingProtocol.pid);
		leaverProtocol.leave();
	}

	public void addNode(Node joiner, Node contact) {
		ARandomPeerSamplingProtocol joinerProtocol = (ARandomPeerSamplingProtocol) joiner
				.getProtocol(ARandomPeerSamplingProtocol.pid);
		joinerProtocol.join(joiner, contact);
	}

}

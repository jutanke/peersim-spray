package descent.controllers;

import java.util.LinkedList;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import descent.Dynamic;
import descent.rps.IDynamic;
import descent.rps.IRandomPeerSampling;

/**
 * Created by julian on 3/28/15.
 */
public abstract class ChurnProtocol implements Control {

	public static ChurnProtocol current;

	private static final String PROTOCOL = "o1";
	private static final String PAR_ADD_COUNT = "addingPerStep";
	private static final String PAR_ADD_PERC = "addingPerStepPerc";
	private static final String PARR_REM_COUNT = "removingPerStep";
	private static final String PAR_ADD_START = "startAdd";
	private static final String PAR_REM_START = "startRem";
	private static final String PAR_ADD_END = "endAdd";
	private static final String PAR_REM_END = "endRem";

	public final int ADDING_PERCENT;
	public final int ADDING_COUNT;
	public final int REMOVING_COUNT;
	public final long ADDING_START;
	public final long REMOVING_START;
	public final long REMOVING_END;
	public final long ADDING_END;
	public final boolean IS_PERCENTAGE;
	public final int pid;

	public LinkedList<Node> graph = new LinkedList<Node>();
	public LinkedList<Node> availableNodes = new LinkedList<Node>();

	public ChurnProtocol(String n, String cyclProtocol) {
		this.ADDING_COUNT = Configuration.getInt(n + "." + PAR_ADD_COUNT, -1);
		this.ADDING_PERCENT = Configuration.getInt(n + "." + PAR_ADD_PERC, -1);
		this.IS_PERCENTAGE = this.ADDING_PERCENT != -1;
		System.err.println("IS PERC: " + this.IS_PERCENTAGE);
		this.REMOVING_COUNT = Configuration.getInt(n + "." + PARR_REM_COUNT, 0);
		this.ADDING_START = Configuration.getInt(n + "." + PAR_ADD_START,
				Integer.MAX_VALUE);
		this.REMOVING_START = Configuration.getInt(n + "." + PAR_REM_START,
				Integer.MAX_VALUE);
		this.REMOVING_END = Configuration.getInt(n + "." + PAR_REM_END,
				Integer.MAX_VALUE);
		this.ADDING_END = Configuration.getInt(n + "." + PAR_ADD_END,
				Integer.MAX_VALUE);
		final int nsize = Network.size();
		this.pid = Configuration.lookupPid(cyclProtocol);
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
			for (int i = 0; i < this.REMOVING_COUNT && this.graph.size() > 0; i++) {
				final int pos = CommonState.r.nextInt(this.graph.size());
				final Node rem = this.graph.get(pos);
				this.removeNode(rem);
				Dynamic d = (Dynamic) rem.getProtocol(pid);
				if (d.isUp()) {
					d.down();
				}
				this.graph.remove(pos);
				this.availableNodes.push(rem);
			}
		}

		if (addingElements) {
			// ADD ELEMENTS

			if (this.IS_PERCENTAGE) {

				final double log10 = Math.floor(Math.log10(this.graph.size()));
				final double dev10 = Math.pow(10, log10);
				int count = Math.max(1, (int) dev10 / this.ADDING_PERCENT);
				System.err.println("QQ:" + graph.size() + "," + log10 + ","
						+ dev10 + "," + count);
				for (int i = 0; i < count && this.availableNodes.size() > 0; i++) {
					insert();
				}

			} else {
				for (int i = 0; i < this.ADDING_COUNT
						&& this.availableNodes.size() > 0; i++) {
					insert();
				}
			}
		}

		return false;
	}

	private void insert() {
		final Node current = this.availableNodes.poll();
		if (graph.size() > 0) {
			// final Node contact = getBestNode();
			final Node contact = getNode();
			this.addNode(current, contact);
		}
		this.graph.add(current);
	}

	public Node getNode() {
		return this.graph.get(CommonState.r.nextInt(this.graph.size()));
	}

	public Node getBestNode() {
		Node a = graph.get(CommonState.r.nextInt(this.graph.size()));
		Node b = graph.get(CommonState.r.nextInt(this.graph.size()));
		Dynamic A = (Dynamic) a.getProtocol(pid);
		Dynamic B = (Dynamic) b.getProtocol(pid);

		final Node bigger = (A.degree() > B.degree()) ? a : b;
		final Node smaller = (A.degree() > B.degree()) ? b : a;

		if (CommonState.r.nextInt(2) == 1) {
			return bigger;
		} else {
			return smaller;
		}
	}

	/**
	 * @param node
	 * @return when true then select this node, otherwise do not select it
	 */
	public abstract void removeNode(Node node);

	public abstract void addNode(Node subscriber, Node contact);
}

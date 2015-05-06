package descent.rps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;

public abstract class ARandomPeerSamplingProtocol implements IDynamic,
		Linkable, CDProtocol, IRandomPeerSampling {

	// #A the names of the parameters in the configuration file of peersim
	public static final String PAR_PROT = "rps";
	private static final String PAR_DELTA = "delta";
	private static final String PAR_START = "start";

	// #B the values from the configuration file of peersim
	public static int pid;
	private static int delta;
	private static int start;

	// #C local variables
	protected boolean isUp = true;
	protected Node node = null;

	/**
	 * Constructor of the class
	 * 
	 * @param prefix
	 *            configuration of peersim
	 * @param rps
	 *            the random peer sampling instance (Cyclon or Scamp or Spray)
	 */
	public ARandomPeerSamplingProtocol(String prefix) {
		ARandomPeerSamplingProtocol.pid = Configuration
				.lookupPid(ARandomPeerSamplingProtocol.PAR_PROT);
		ARandomPeerSamplingProtocol.delta = Configuration.getInt(prefix + "."
				+ ARandomPeerSamplingProtocol.PAR_DELTA);
		ARandomPeerSamplingProtocol.start = Configuration.getInt(prefix + "."
				+ ARandomPeerSamplingProtocol.PAR_START);
	}

	public ARandomPeerSamplingProtocol() {
	}

	// must be implemented in the child class
	public abstract IRandomPeerSampling clone();

	public void onKill() {
	}

	public void nextCycle(Node node, int pid) {
		// #1 lazy loading of the reference of the node
		if (this.node == null) {
			this.node = node;
		}
		// #2 call the periodic function of the node every Delta time
		if (CommonState.getTime() >= ARandomPeerSamplingProtocol.start
				&& CommonState.getTime() % ARandomPeerSamplingProtocol.delta == 0) {
			this.periodicCall();
		}
	}

	public abstract boolean addNeighbor(Node peer);

	public boolean contains(Node neighbor) {
		boolean found = false;
		Iterator<Node> iNeighbors = this.getPeers(Integer.MAX_VALUE).iterator();
		while (!found && iNeighbors.hasNext()) {
			if (iNeighbors.next().getID() == neighbor.getID()) {
				found = true;
			}
		}
		return found;
	}

	public int degree() {
		return this.getAliveNeighbors().size();
	}

	public Node getNeighbor(int index) {
		return this.getPeers(Integer.MAX_VALUE).get(index);
	}

	/**
	 * Getter of the list of alive neighbors
	 * 
	 * @return a list of nodes
	 */
	public List<Node> getAliveNeighbors() {
		List<Node> neighbors = this.getPeers(Integer.MAX_VALUE);
		ArrayList<Node> result = new ArrayList<Node>();
		for (Node neighbor : neighbors) {
			if (neighbor.isUp()) {
				result.add(neighbor);
			}
		}
		return result;
	}

	public void pack() {
	}

	public boolean isUp() {
		return this.isUp;
	}

	public boolean isDown() {
		return this.isUp;
	}

}

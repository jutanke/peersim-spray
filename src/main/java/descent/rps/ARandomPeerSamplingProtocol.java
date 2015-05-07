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
	public static final String PAR_PROT = "rps"; // name of the protocol
	private static final String PAR_DELTA = "delta"; // frequency of cyclic call
	private static final String PAR_START = "start"; // start the cyclic call
	private static final String PAR_FAIL = "fail"; // proba of fail of each peer

	// #B the values from the configuration file of peersim
	public static int pid;
	private static int delta;
	private static int start;
	protected static double fail;

	// #C local variables
	protected boolean isUp = false;
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
		ARandomPeerSamplingProtocol.fail = Configuration.getDouble(prefix + "."
				+ ARandomPeerSamplingProtocol.PAR_FAIL, 0.0);
	}

	public ARandomPeerSamplingProtocol() {
	}

	// must be implemented in the child class
	public abstract IRandomPeerSampling clone();

	public abstract boolean addNeighbor(Node peer);

	/**
	 * Compute the probability that the connection establishment fails. A fail
	 * setup means that locally, the peer has the reference to the remote peer
	 * but the arc (or link (or connection)) associated to it does not work. It
	 * depends of the number of hops before reaching the peer to connect to. The
	 * inbetween arcs and peer must remains up for the round-trip, mandatory in
	 * three-way handshake connection context.
	 * 
	 * @param path
	 *            the path traveled by the connection
	 * @return true if the connection establishment fails, false otherwise
	 */
	protected abstract boolean pFail(List<Node> path);

	public void onKill() {
	}

	public void nextCycle(Node node, int pid) {
		// #1 lazy loading of the reference of the node
		if (this.node == null) {
			this.node = node;
		}
		// #2 call the periodic function of the node every Delta time
		if (isUp()
				&& CommonState.getTime() >= ARandomPeerSamplingProtocol.start
				&& CommonState.getTime() % ARandomPeerSamplingProtocol.delta == 0) {
			this.periodicCall();
		}
	}

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

package descent.rps;

import java.util.Iterator;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;

public abstract class ARandomPeerSamplingProtocol implements IDynamic,
		Linkable, CDProtocol, IRandomPeerSampling {

	// #A the names of the parameters in the configuration file of peersim
	private static final String PAR_PROT = "lnk";
	private static final String PAR_TRANSPORT = "transport";
	private static final String PAR_DELTA = "delta";
	private static final String PAR_START = "start";

	// #B the values from the configuration file of peersim
	protected static int tid;
	protected static int pid;
	private static int delta;
	private static int start;

	// #C local variables
	protected boolean isUp = true;

	/**
	 * Constructor of the class
	 * 
	 * @param prefix
	 *            configuration of peersim
	 * @param rps
	 *            the random peer sampling instance (Cyclon or Scamp or Spray)
	 */
	public ARandomPeerSamplingProtocol(String prefix) {
		ARandomPeerSamplingProtocol.tid = Configuration.getPid(prefix + "."
				+ ARandomPeerSamplingProtocol.PAR_TRANSPORT);
		ARandomPeerSamplingProtocol.pid = Configuration
				.lookupPid(ARandomPeerSamplingProtocol.PAR_PROT);
		ARandomPeerSamplingProtocol.delta = Configuration
				.getInt(ARandomPeerSamplingProtocol.PAR_DELTA);
		ARandomPeerSamplingProtocol.start = Configuration
				.getInt(ARandomPeerSamplingProtocol.PAR_START);
	}

	public ARandomPeerSamplingProtocol() {
	}

	// must be implemented in the child class
	public abstract IRandomPeerSampling clone();

	public void onKill() {
	}

	public void nextCycle(Node node, int pid) {
		// (XXX) does not work with intervals yet
		if (CommonState.getTime() >= ARandomPeerSamplingProtocol.start
				&& ARandomPeerSamplingProtocol.delta % CommonState.getTime() == 0) {
			this.periodicCall();
		}
	}

	public boolean addNeighbor(Node peer) {
		return this.addNeighbor(peer);
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
		return this.getPeers(Integer.MAX_VALUE).size();
	}

	public Node getNeighbor(int index) {
		return this.getPeers(Integer.MAX_VALUE).get(index);
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

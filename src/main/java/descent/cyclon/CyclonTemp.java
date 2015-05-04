package descent.cyclon;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import descent.rps.ARandomPeerSamplingProtocol;
import descent.rps.IMessage;
import descent.rps.IRandomPeerSampling;

/**
 * The Cyclon protocol
 */
public class CyclonTemp extends ARandomPeerSamplingProtocol implements
		IRandomPeerSampling {

	// #A the names of the parameters in the configuration file of peersim
	private static final String PAR_C = "c"; // max partial view size
	private static final String PAR_L = "l"; // shuffle size

	// #B the values from the configuration file of peersim
	private static int c;
	private static int l;

	// #C local variables
	private CyclonPartialView partialView;
	private static int RND_WALK = 5;

	/**
	 * Construction of a Cyclon instance
	 * 
	 * @param prefix
	 *            the peersim configuration
	 */
	public CyclonTemp(String prefix) {
		super(prefix);
		CyclonTemp.c = Configuration.getInt(prefix + "." + PAR_C);
		CyclonTemp.l = Configuration.getInt(prefix + "." + PAR_L);
		this.partialView = new CyclonPartialView(CyclonTemp.c, CyclonTemp.l);
	}

	public CyclonTemp() {
		super();
		this.partialView = new CyclonPartialView(CyclonTemp.c, CyclonTemp.l);
	}

	public void periodicCall() {
		if (this.isUp() && this.partialView.getPeers().size() > 0) {
			this.partialView.incrementAge();
			Node q = this.partialView.getOldest();
			CyclonTemp qProtocol = (CyclonTemp) q
					.getProtocol(ARandomPeerSamplingProtocol.pid);
			List<Node> sample = this.partialView.getSample(q);
			sample.add((Node) this); // (XXX) not sure 'bout the cast
			IMessage received = qProtocol.onPeriodicCall(this.node,
					new CyclonTempMessage(sample));
			List<Node> samplePrime = (List<Node>) received.getPayload();
			this.partialView.mergeSample(q, samplePrime, sample);
		}
	}

	public IMessage onPeriodicCall(Node origin, IMessage message) {
		List<Node> samplePrime = this.partialView.getSample(this.node);
		this.partialView.mergeSample(this.node,
				(List<Node>) message.getPayload(), samplePrime);
		return new CyclonTempMessage(samplePrime);
	}

	public void join(Node contact) {
		CyclonTemp contactCyclon = (CyclonTemp) contact;
		this.partialView.clear();
		this.partialView.addNeighbor(contact);
		contactCyclon.onSubscription(this.node);
	}

	public void onSubscription(Node origin) {
		List<Node> aliveNeighbors = this.getAliveNeighbors();
		Collections.shuffle(aliveNeighbors, CommonState.r);
		int nbRndWalk = Math.min(CyclonTemp.c - 1, aliveNeighbors.size());

		for (int i = 0; i < nbRndWalk; ++i) {
			randomWalk(origin, aliveNeighbors.get(i), CyclonTemp.RND_WALK);
		}
	}

	public void leave() {
		this.isUp = false;
		this.partialView.clear();
		// nothing else
	}

	public List<Node> getPeers(int k) {
		return this.partialView.getPeers(k);
	}

	public IRandomPeerSampling clone() {
		try {
			CyclonTemp cyClone = new CyclonTemp();
			cyClone.partialView = (CyclonPartialView) this.partialView.clone();
			return cyClone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Perform a random walk in the network at a depth set by ttl (time-to-live)
	 * 
	 * @param origin
	 *            the subscribing peer
	 * @param current
	 *            the current peer that either accept the subcription or
	 *            forwards it
	 * @param ttl
	 *            the current time-to-live before the subscription gets accepted
	 */
	private static void randomWalk(Node origin, Node current, int ttl) {
		final CyclonTemp originCyclon = (CyclonTemp) origin.getProtocol(pid);
		final CyclonTemp currentCyclon = (CyclonTemp) current.getProtocol(pid);
		List<Node> aliveNeighbors = currentCyclon.getAliveNeighbors();
		ttl -= 1;
		// #A if the receiving peer has neighbors in its partial view
		if (aliveNeighbors.size() > 0) {
			// #A1 if the ttl is greater than 0, continue the random walk
			if (ttl > 0) {
				final Node next = aliveNeighbors.get(CommonState.r
						.nextInt(aliveNeighbors.size()));
				randomWalk(origin, next, ttl);
			} else {
				// #B if the ttl is greater than 0 or the partial view is empty,
				// then
				// accept the subscription and stop forwarding it
				if (origin.getID() != current.getID()) {
					Iterator<Node> iPeers = currentCyclon.getPeers(1)
							.iterator();
					if (iPeers.hasNext()) {
						Node chosen = iPeers.next();
						currentCyclon.partialView.removeNode(chosen);
						originCyclon.partialView.addNeighbor(chosen);
					}
					currentCyclon.addNeighbor(origin);
				}
			}
		}
	}

}

package descent.cyclon;

import java.util.List;

import peersim.config.Configuration;
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
			List<Node> sent = this.partialView.getSample(q);
			sent.add((Node) this); // (XXX) not sure 'bout the cast
			List<Node> received = qProtocol.onPeriodicCall(this,
					new CyclonTempMessage(sent));
			this.partialView.mergeSample(q, received, sent);
		}
	}

	public List<Node> onPeriodicCall(IRandomPeerSampling origin,
			IMessage message) {
		List<Node> sent = this.partialView.getSample((Node) this);
		this.partialView.mergeSample((Node) this,
				(List<Node>) message.getPayload(), sent);
		return sent;
	}

	public void join(IRandomPeerSampling contact) {
		// TODO Auto-generated method stub

	}

	public void onSubscription(IRandomPeerSampling joiner) {
		// TODO Auto-generated method stub

	}

	public void leave() {
		this.isUp = false;
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

}

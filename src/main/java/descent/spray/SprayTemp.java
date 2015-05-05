package descent.spray;

import java.util.List;

import peersim.core.Node;
import descent.rps.ARandomPeerSamplingProtocol;
import descent.rps.IMessage;
import descent.rps.IRandomPeerSampling;

/**
 * The Spray protocol
 */
public class SprayTemp extends ARandomPeerSamplingProtocol implements
		IRandomPeerSampling {

	// #A no configuration needed, everything is adaptive
	// #B no values from the configuration file of peersim
	// #C local variables
	private SprayTempPartialView partialView;

	/**
	 * Constructor of the Spray instance
	 * 
	 * @param prefix
	 *            the peersim configuration
	 */
	public SprayTemp(String prefix) {
		super(prefix);
		this.partialView = new SprayTempPartialView();
	}

	public SprayTemp() {
		this.partialView = new SprayTempPartialView();
	}

	public void periodicCall() {
		if (this.isUp) {
		}
	}

	public IMessage onPeriodicCall(Node origin, IMessage message) {
		// TODO Auto-generated method stub
		return null;
	}

	public void join(Node joiner, Node contact) {
		// TODO Auto-generated method stub

	}

	public void onSubscription(Node origin) {
		// TODO Auto-generated method stub

	}

	public void leave() {
		// TODO Auto-generated method stub

	}

	public List<Node> getPeers(int k) {
		// TODO Auto-generated method stub
		return null;
	}

	public IRandomPeerSampling clone() {
		try {
			SprayTemp sprayClone = new SprayTemp();
			sprayClone.partialView = (SprayTempPartialView) this.partialView
					.clone();
			return sprayClone;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean addNeighbor(Node peer) {
		// TODO Auto-generated method stub
		return false;
	}

}

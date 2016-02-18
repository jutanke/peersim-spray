package descent.closify;

import java.util.ArrayList;
import java.util.List;

import peersim.core.Node;
import descent.rps.ARandomPeerSamplingProtocol;
import descent.rps.IMessage;
import descent.rps.IRandomPeerSampling;
import descent.spray.MergingRegister;
import descent.spray.Spray;
import descent.spray.SprayPartialView;

public class Closify extends Spray {

	public ClosifyView additionnalView;

	public Closify(String prefix) {
		super(prefix);
		this.additionnalView = new ClosifyView();
	}

	public Closify() {
		super();
		this.additionnalView = new ClosifyView();
	}

	@Override
	public List<Node> getPeers(int k) {
		ArrayList<Node> result = (ArrayList<Node>) this.partialView.getPeers();
		result.addAll(this.additionnalView.getPeers());
		return result;
	}

	public void periodicCall() {
		Node oldest = this.partialView.getOldest();
		Closify qClosify = (Closify) oldest
				.getProtocol(ARandomPeerSamplingProtocol.pid);
		super.periodicCall();
		IMessage send = new ClosifyMessage(
				this.additionnalView.getPeers(30 / 100 * this.additionnalView
						.size()));
		ClosifyMessage answer = (ClosifyMessage) qClosify
				.onClosifyPeriodicCall(this.node, send);
		this.additionnalView.updateMax(this.partialView.size());
		this.additionnalView.mergeSample(this.node, qClosify.node,
				(List<Node>) answer.getPayload(),
				(List<Node>) send.getPayload(), true);
		if (this.additionnalView.size() < this.additionnalView.max) {
			this.additionnalView.mergeSample(this.node, qClosify.node,
					this.partialView.getPeers(), new ArrayList<Node>(), true);
		}
		// System.out.println(this.additionnalView.max);
	}

	public IMessage onClosifyPeriodicCall(Node origin, IMessage message) {
		IMessage answer = new ClosifyMessage(
				this.additionnalView.getPeers(30 / 100 * this.additionnalView
						.size()));
		this.additionnalView.mergeSample(this.node, origin,
				(List<Node>) message.getPayload(),
				(List<Node>) answer.getPayload(), false);
		if (this.additionnalView.size() < this.additionnalView.max) {
			this.additionnalView.mergeSample(this.node, origin,
					this.partialView.getPeers(), new ArrayList<Node>(), false);
		}
		return answer;
	}

	@Override
	public IRandomPeerSampling clone() {
		try {
			Closify closifyClone = new Closify();
			closifyClone.partialView = (SprayPartialView) this.partialView
					.clone();
			closifyClone.register = (MergingRegister) this.register.clone();
			closifyClone.additionnalView = (ClosifyView) this.additionnalView
					.clone();
			return closifyClone;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}

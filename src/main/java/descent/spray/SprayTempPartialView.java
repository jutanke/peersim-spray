package descent.spray;

import java.util.List;

import peersim.core.Node;
import descent.rps.IAgingPartialView;

public class SprayTempPartialView implements IAgingPartialView {

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	public void incrementAge() {
		// TODO Auto-generated method stub

	}

	public Node getOldest() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Node> getPeers() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Node> getPeers(int k) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Node> getSample(Node neighbor) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean removeNode(Node peer) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean removeNode(Node peer, Integer age) {
		// TODO Auto-generated method stub
		return false;
	}

	public void mergeSample(Node me, Node other, List<Node> newSample,
			List<Node> oldSample) {
		// TODO Auto-generated method stub

	}

	public boolean addNeighbor(Node peer) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean contains(Node peer) {
		// TODO Auto-generated method stub
		return false;
	}

	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void clear() {
		// TODO Auto-generated method stub

	}

}

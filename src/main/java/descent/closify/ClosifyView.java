package descent.closify;

import java.util.ArrayList;
import java.util.List;

import peersim.core.CommonState;
import peersim.core.Node;
import descent.rps.PartialView;

public class ClosifyView extends PartialView {

	public int max = 0;

	public void updateMax(Integer pvSize) {
		this.max = Math
				.max(0, (int) (2 * Math.sqrt(Math.exp(pvSize)) - pvSize));
	}

	public void mergeSample(Node caller, Node neighbor, List<Node> nSample,
			List<Node> oSample, boolean isInitiator) {
		ArrayList<Node> oldSample = new ArrayList<Node>();
		oldSample.addAll(oSample);
		ArrayList<Node> newSample = new ArrayList<Node>();
		newSample.addAll(nSample);

		// #1 cut the tail to adjust to the maximum length
		while (this.partialView.size() > this.max) {
			if (oldSample.size() > 0) {
				// #A priority to sent sample
				this.partialView.remove(oldSample.get((int) 0));
				oldSample.remove((int) 0);
			} else {
				// #B at random
				this.partialView.remove(CommonState.r.nextInt(this.partialView
						.size()));
			}

		}
		// #2 add new sample to adjust to the maximum length
		while (this.partialView.size() < this.max && newSample.size() > 0) {
			this.addNeighbor(newSample.get((int) 0));
			newSample.remove((int) 0);
		}
		// #3 replace old entries with the new received sample
		while (newSample.size() > 0) {
			Node entry = newSample.get((int) 0);
			newSample.remove((int) 0);
			if (!this.partialView.contains(entry)) {
				if (oldSample.size() > 0) {
					this.partialView.remove(oldSample.get((int) 0));
					oldSample.remove((int) 0);
				} else {
					this.partialView.remove(CommonState.r
							.nextInt(this.partialView.size()));
				}
				this.partialView.add(entry);
			}
		}
	}

	@Override
	public ClosifyView clone() throws CloneNotSupportedException {
		ClosifyView viewClone = new ClosifyView();
		viewClone.max = this.max;
		viewClone.partialView = (ArrayList<Node>) this.partialView.clone();
		return viewClone;
	}
}

package descent.slicer;

import java.util.HashSet;

import descent.rps.IPeerSampling;
import descent.spray.MergingRegister;
import descent.spray.SprayPartialView;
import descent.tman.TMan;
import descent.tman.TManPartialView;
import peersim.core.CommonState;
import peersim.core.Node;

public class Slicer extends TMan {

	private static boolean SWAP = true;
	private boolean once = false;

	public Slicer(String prefix) {
		super(prefix);

		this.descriptor = new RankDescriptor();
	}

	public Slicer() {
		super();
		this.descriptor = new RankDescriptor();
	}

	public void periodicCall() {
		if (CommonState.getTime() == 100) {
			Double rn = Math.abs(CommonState.r.nextGaussian()) * 5.;
			((RankDescriptor) this.descriptor).setFrequency(rn);
		}

		// #1 initialize descriptor based on Spray
		if (!this.rank.equals(Integer.MAX_VALUE) && !this.once) {
			((RankDescriptor) this.descriptor).setRank(this.rank);
			this.once = true;
		}

		// #2 see if a swap of rank is needed
		// (TODO) move this to be more generic, i.e. should not be in slicer	
		if (Slicer.SWAP) {
			Node toSwapNode = null;

			RankDescriptor boundaryDescriptor = (RankDescriptor) this.descriptor;
			// bottom -> top
			for (Node neighbor : this.partialViewTMan) {
				Slicer slicerNeighbor = (Slicer) neighbor.getProtocol(Slicer.pid);
				RankDescriptor slicerDescriptor = (RankDescriptor) slicerNeighbor.descriptor;
				if (boundaryDescriptor.compareTo(slicerDescriptor) > 0
						&& slicerDescriptor.rank < ((RankDescriptor) this.descriptor).rank) {
					boundaryDescriptor = slicerDescriptor;
					toSwapNode = neighbor;
				}
			}

			if (toSwapNode == null) {
				// top -> bottom
				for (Node neighbor : this.partialViewTMan) {
					Slicer slicerNeighbor = (Slicer) neighbor.getProtocol(Slicer.pid);
					RankDescriptor slicerDescriptor = (RankDescriptor) slicerNeighbor.descriptor;
					if (boundaryDescriptor.compareTo(slicerDescriptor) < 0
							&& slicerDescriptor.rank > ((RankDescriptor) this.descriptor).rank) {
						boundaryDescriptor = slicerDescriptor;
						toSwapNode = neighbor;
					}
				}
			}

			// #B swap
			if (toSwapNode != null) {
				Slicer toSwapSlicer = (Slicer) toSwapNode.getProtocol(Slicer.pid);

				Integer r = ((RankDescriptor) toSwapSlicer.descriptor).rank;
				((RankDescriptor) toSwapSlicer.descriptor).setRank(((RankDescriptor) this.descriptor).rank);
				((RankDescriptor) this.descriptor).setRank(r);
			}
		}

		// #3 exchange views
		super.periodicCall();
	}

	@Override
	public IPeerSampling clone() {
		TMan slicerClone = new Slicer();
		try {
			slicerClone.partialView = (SprayPartialView) this.partialView.clone();
			slicerClone.register = (MergingRegister) this.register.clone();
			slicerClone.partialViewTMan = (TManPartialView) this.partialViewTMan.clone();
			slicerClone.descriptor = new RankDescriptor();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return slicerClone;
	}

}

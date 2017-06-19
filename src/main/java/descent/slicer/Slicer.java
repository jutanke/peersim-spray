package descent.slicer;

import descent.rps.IPeerSampling;
import descent.spray.MergingRegister;
import descent.spray.SprayPartialView;
import descent.tman.TMan;
import descent.tman.TManPartialView;

public class Slicer extends TMan {

	public Slicer(String prefix) {
		super(prefix);

		this.descriptor = new RankDescriptor();
	}

	public Slicer() {
		super();
		this.descriptor = new RankDescriptor();
	}

	public void periodicCall() {
		if (!this.rank.equals(Integer.MAX_VALUE)) {
			((RankDescriptor) this.descriptor).setRank(this.rank);
		}

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

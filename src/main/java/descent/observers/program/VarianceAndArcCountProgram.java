package descent.observers.program;

import descent.observers.DictGraph;
import descent.observers.ObserverProgram;

/**
 * Created by julian on 4/29/15.
 */
public class VarianceAndArcCountProgram implements ObserverProgram {

	int lastSize = -1;
	int lastCount = 0;
	int lastCountTemp = 0;
	double firstVar = -1;

	public VarianceAndArcCountProgram() {
		System.out.println("#arcCount arcCountNoDuplicates variance avgPartialView nbPeer lastVariance lastArcCount");
	}

	public void tick(long currentTick, DictGraph observer) {

		if (this.lastSize != observer.size()) {
			this.lastSize = observer.size();
			firstVar = observer.variancePartialView();
			lastCount = lastCountTemp;
			lastCountTemp = observer.countArcs();
		}

		System.out.println(observer.countArcs() + " " + observer.countArcsNoDuplicates() + " "
				+ +observer.variancePartialView() + " " + observer.meanPartialViewSize() + " " + observer.size() + " "
				+ firstVar + " " + lastCount);

	}

	public void onLastTick(DictGraph observer) {
		// System.out.println("LAST");
	}
}

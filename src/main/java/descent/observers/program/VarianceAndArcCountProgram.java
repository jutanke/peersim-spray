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
		System.out.println("#nbPeers arcCount arcCountNoDuplicates stdDeviation avg|P| min|P| max|P|");
	}

	public void tick(long currentTick, DictGraph observer) {

		if (this.lastSize != observer.size()) {
			this.lastSize = observer.size();
			firstVar = observer.variancePartialView();
			lastCount = lastCountTemp;
			lastCountTemp = observer.countArcs();
		}

		System.out.println(observer.size() + " " + observer.countArcs() + " " + observer.countArcsNoDuplicates() + " "
				+ +observer.stdDeviationPartialView() + " " + observer.getViewSizeStats().toString() + " ");
		// + " " + firstVar + " " + lastCount);

	}

	public void onLastTick(DictGraph observer) {
		// System.out.println("LAST");
	}
}

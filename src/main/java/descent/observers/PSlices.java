package descent.observers;

import java.util.ArrayList;

public class PSlices implements ObserverProgram {

	public void tick(long currentTick, DictGraph observer) {
		ArrayList<Integer> distances = observer.getDistancesDiscrete();
		ArrayList<Integer> distribution = observer.countWriters();

		System.out.println(observer.size() + " " + distribution + " " + distances + " ");

	}

	public void onLastTick(DictGraph observer) {
		System.out.println(observer.networkxTManDigraph("AHorseWithNoName"));
	}

}

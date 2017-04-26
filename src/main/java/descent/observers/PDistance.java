package descent.observers;

import java.util.HashMap;

public class PDistance implements ObserverProgram {

	public void tick(long currentTick, DictGraph observer) {
		HashMap<Double, Integer> distribution = observer.getDistances(10);

		String output = "";
		for (Double key : distribution.keySet()) {
			output = distribution.get(key).toString() + " ";
		}

		System.out.println(output);
	}

	public void onLastTick(DictGraph observer) {
	}

}

package descent.observers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class PDistance implements ObserverProgram {

	public void tick(long currentTick, DictGraph observer) {
		HashMap<Double, Integer> distribution = observer.getDistances(10);

		String output = "";

		ArrayList<Double> keys = new ArrayList<Double>(distribution.keySet());
		Collections.sort(keys);

		for (Double key : keys) {
			output = output + distribution.get(key).toString() + " ";
		}

		System.out.println(output);
	}

	public void onLastTick(DictGraph observer) {
	}

}

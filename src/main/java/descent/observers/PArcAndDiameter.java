package descent.observers;

import java.util.ArrayList;

import peersim.core.CommonState;

/**
 *
 */
public class PArcAndDiameter implements ObserverProgram {

	final int step = 10;
	boolean isFirst = true;
	ArrayList<Integer> checkpoint = new ArrayList<Integer>();
	static Integer i = -1;

	public PArcAndDiameter() {
		checkpoint.add(100);
		checkpoint.add(500);
		checkpoint.add(1000);
		checkpoint.add(5000);
		checkpoint.add(10000);
		checkpoint.add(50000);
		checkpoint.add(100000);
		checkpoint.add(500000);
		checkpoint.add(1000000);
	}

	/**
	 *
	 * @param currentTick
	 *            {}
	 * @param observer
	 *            {}
	 */
	public void tick(long currentTick, DictGraph observer) {
		if (observer.size() == checkpoint.get(i + 1)) {
			System.out.println(observer.size() + " " + observer.diameter());
			i += 1;
		}
	}

	// if ((CommonState.getTime() % step) == 0) {
	// System.out.println(observer.size() + " " + observer.diameter());
	// System.out.println(observer.size() + " " + observer.countArcs() +
	// " " + observer.diameter() + " "
	// + observer.maxPercDuplicatesInView() + " " +
	// observer.meanPathLength().avg);
	// }


	/**
	 *
	 * @param observer
	 *            {}
	 */
	public void onLastTick(DictGraph observer) {
		/*
		 * System.out.println("#=================START=================== step:"
		 * + CommonState.getTime());
		 * System.out.println(observer.networkxDigraph(
		 * DictGraph.NetworkX.Connectedness, "g" + (CommonState.getTime()),
		 * true));
		 * System.out.println("#=================END===================");
		 */
	}
}

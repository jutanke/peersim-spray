package descent.observers.program;

import descent.observers.DictGraph;
import descent.observers.ObserverProgram;
import peersim.core.CommonState;

/**
 *
 */
public class ArcAndDiameterProgram implements ObserverProgram {

	final int step = 1000;
	boolean isFirst = true;

	/**
	 *
	 * @param currentTick
	 *            {}
	 * @param observer
	 *            {}
	 */
	public void tick(long currentTick, DictGraph observer) {
		System.out.println(observer.size() + " " + observer.countArcs() + " "
				+ observer.diameter() + " "
				+ observer.maxPercDuplicatesInView() + " "
				+ observer.meanPathLength().avg);
	}

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

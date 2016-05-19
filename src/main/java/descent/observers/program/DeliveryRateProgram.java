package descent.observers.program;

import descent.observers.DictGraph;
import descent.observers.DictGraph.DeliveryRateAndMsg;
import descent.observers.ObserverProgram;

/**
 *
 */
public class DeliveryRateProgram implements ObserverProgram {

	// private static final String PAR_FANOUT = "fanout";

	public final Integer FANOUT;

	public DeliveryRateProgram() {
		this.FANOUT = -1;
	}

	/**
	 *
	 * @param currentTick
	 *            {}
	 * @param observer
	 *            {}
	 */
	public void tick(long currentTick, DictGraph observer) {
		DeliveryRateAndMsg result = observer.deliveryRate(25, 5);// this.FANOUT);
		System.out.println(result.nbNodes + " " + result.nbMsg + " " + result.softRate + " " + result.hardRate);
		// System.out.println(observer.size() + " " + observer.countArcs() + " "
		// + observer.diameter() + " "
		// + observer.maxPercDuplicatesInView() + " " +
		// observer.meanPathLength().avg);
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

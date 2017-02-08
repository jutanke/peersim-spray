package descent.observers.program;

import java.util.function.Function;

import descent.observers.DictGraph;
import descent.observers.DictGraph.DeliveryRateAndMsg;
import peersim.core.CommonState;
import descent.observers.ObserverProgram;

/**
 *
 */
public class DeliveryRateProgram implements ObserverProgram {

	public Function<Integer, Integer> constantFanout = new Function<Integer, Integer>() {
		public Integer apply(Integer whatever) {
			// #1 nothing is adaptive
			return 10;
		}
	};
	public Function<Integer, Integer> divFanout = new Function<Integer, Integer>() {
		public Integer apply(Integer rpsView) {
			// #2 adapts to a subset of the view
			return (int) Math.ceil(rpsView / 6.) + 5;
		}
	};
	public Function<Integer, Integer> allFanout = new Function<Integer, Integer>() {
		public Integer apply(Integer rpsView) {
			// #3 use everything
			return rpsView;
		}
	};

	int lastSize = 0;
	int tick = 0;

	public DeliveryRateProgram() {
		System.out.println("#nbNode nbMsgSent fanout softRate hardRate >0 >=99 >=99.9");
	}

	/**
	 *
	 * @param currentTick
	 *            {}
	 * @param observer
	 *            {}
	 */
	public void tick(long currentTick, DictGraph observer) {
		if (CommonState.getTime() % 10 == 0){
			DeliveryRateAndMsg result = observer.deliveryRate(divFanout, 100);
			System.out.println(result.nbNodes + " " + result.nbMsg + " " + result.fanout + " " + result.softRate
					+ " " + result.hardRate + " " + result.atLeastOne + " " + +result.lesserThanLessHardRate
					+ " " + result.lessHardRate);
		}
		/*if (this.lastSize < observer.size()) {
			this.tick += 1;
			if (this.tick >= 18) {
				this.tick = 0;
				this.lastSize = observer.size();
				//if (observer.size() % ((Math.pow(10, Math.ceil(Math.log10(observer.size())))) / 2) == 0) {
					if (observer.size() % 10 == 0) {
					// #A cheap measure
					DeliveryRateAndMsg result = observer.deliveryRate(constantFanout, 1000);
					System.out.println(result.nbNodes + " " + result.nbMsg + " " + result.fanout + " " + result.softRate
							+ " " + result.hardRate + " " + result.atLeastOne + " " + +result.lesserThanLessHardRate
							+ " " + result.lessHardRate);
				}
			}
		}*/
	}

	/**
	 *
	 * @param observer
	 *            {}
	 */
	public void onLastTick(DictGraph observer) {
		// #B longer measurement
		// DeliveryRateAndMsg result = observer.deliveryRate(divFanout, 100);//
		// this.FANOUT);
		// DeliveryRateAndMsg result = observer.deliveryRate(constantFanout,
		// 100);
		// DeliveryRateAndMsg result = observer.deliveryRate(allFanout, 100);
		// System.out.println(result.nbNodes + " " + result.nbMsg + " " +
		// result.softRate + " " + result.hardRate + " "
		// + result.fanout);
	}

}

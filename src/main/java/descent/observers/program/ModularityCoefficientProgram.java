package descent.observers.program;

import descent.observers.DictGraph;
import descent.observers.ObserverProgram;

public class ModularityCoefficientProgram implements ObserverProgram {

	public void tick(long currentTick, DictGraph observer) {
		System.out.println(observer.countArcs() + " "
				+ observer.modularityCoefficient());
	}

	public void onLastTick(DictGraph observer) {

	}
}

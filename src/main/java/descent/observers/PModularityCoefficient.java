package descent.observers;

public class PModularityCoefficient implements ObserverProgram {

	public void tick(long currentTick, DictGraph observer) {
		System.out.println(observer.countArcs() + " "
				+ observer.modularityCoefficient());
	}

	public void onLastTick(DictGraph observer) {

	}
}

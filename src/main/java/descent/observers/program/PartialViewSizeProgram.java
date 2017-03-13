package descent.observers.program;

import descent.observers.DictGraph;
import descent.observers.ObserverProgram;

/**
 * Created by julian on 11/05/15.
 */
public class PartialViewSizeProgram implements ObserverProgram {

	public void tick(long currentTick, DictGraph observer) {
		System.out.println(observer.getViewSizeStats().mean);
	}

	public void onLastTick(DictGraph observer) {

	}
}

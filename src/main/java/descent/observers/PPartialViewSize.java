package descent.observers;

/**
 * Created by julian on 11/05/15.
 */
public class PPartialViewSize implements ObserverProgram {

	public void tick(long currentTick, DictGraph observer) {
		System.out.println(observer.getViewSizeStats().mean);
	}

	public void onLastTick(DictGraph observer) {

	}
}

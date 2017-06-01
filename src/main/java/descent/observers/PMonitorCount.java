package descent.observers;

/**
 *
 */
public class PMonitorCount implements ObserverProgram {

	/**
	 *
	 * @param currentTick
	 *            {}
	 * @param observer
	 *            {}
	 */
	public void tick(long currentTick, DictGraph observer) {
		if (observer.size() > 0)
			System.out.println(observer.size() + " " + observer.countArcs() + " "
					+ (observer.countArcs() / new Double(observer.size())) + " " + observer.countMonitors() + " "
					+ observer.countWriters()); // + " " +
												// observer.findWriter(10000));
	}

	/**
	 *
	 * @param observer
	 *            {}
	 */
	public void onLastTick(DictGraph observer) {
	}
}

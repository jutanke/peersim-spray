package descent.observers;

public class PEstimator implements ObserverProgram {

	public PEstimator() {
		System.out.println("#nbPeers stdDeviation avg|P| min|P| max|P|");
	}

	public void tick(long currentTick, DictGraph observer) {
		if ((currentTick + 25) % 50 == 0) {
			System.out.println(observer.size() + " " + observer.getViewSizeStats().toString() + " "
					+ observer.stdDeviationPartialView() + " " + observer.getEstimatorStats().toString() + " "
					+ observer.getAggregatedEstimatorStats());
		}
	}

	public void onLastTick(DictGraph observer) {
		// TODO Auto-generated method stub

	}

}

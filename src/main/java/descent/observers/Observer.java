package descent.observers;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import descent.observers.program.VarianceAndArcCountProgram;
import descent.rps.IDynamic;
import descent.rps.IRandomPeerSampling;

/**
 * Created by julian on 3/15/15.
 */
public class Observer implements Control {

	private static final String PAR_PROTOCOL = "protocol";

	private int pid;
	private final ObserverProgram program;
	private boolean isLast = false;

	public Observer(String name) {
		this.pid = Configuration.lookupPid(Configuration.getString(name + "."
				+ PAR_PROTOCOL));

		this.program = new VarianceAndArcCountProgram();
		// this.program = new DebugProgram();
		// this.program = new PythonNetworkProgram();

	}

	public boolean execute() {
		if (!this.isLast) {
			final DictGraph observer = DictGraph.getSingleton(Network.size());
			observer.reset();
			int max = Integer.MIN_VALUE;
			int min = Integer.MAX_VALUE;

			for (int i = 0; i < Network.size(); i++) {
				Node n = Network.get(i);
				IDynamic d = (IDynamic) n.getProtocol(pid);
				if (d.isUp()) {
					IRandomPeerSampling pss = (IRandomPeerSampling) n
							.getProtocol(pid);
					observer.addStrict(n, pss);
					final int size = pss.getAliveNeighbors().size();
					if (size < min) {
						min = size;
					}
					if (size > max) {
						max = size;
					}
				}
			}
			this.program.tick(CommonState.getTime(), observer);
			if (CommonState.getTime() == (CommonState.getEndTime() - 1)) {
				this.program.onLastTick(observer);
				this.isLast = true;
			}
		}
		return false;
	}

}

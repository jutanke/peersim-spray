package descent.observers;

import descent.observers.program.*;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import descent.rps.IDynamic;
import descent.rps.IRandomPeerSampling;

/**
 * Created by julian on 3/15/15.
 */
public class Observer implements Control {

	private static final String PAR_PROTOCOL = "protocol";
	private static final String PROG = "program";

	private int pid;
	private ObserverProgram program;
	private boolean isLast = false;

	public Observer(String name) {
		this.pid = Configuration.lookupPid(Configuration.getString(name + "."
				+ PAR_PROTOCOL));

		final Class<?> programClass = Configuration.getClass(name + "." + PROG);
		try {
			this.program = (ObserverProgram) programClass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not determine Observer program");
		}
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

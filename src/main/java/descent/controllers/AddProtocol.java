package descent.controllers;

import descent.Dynamic;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Node;

/**
 * Created by julian on 3/31/15.
 */
public class AddProtocol implements Control {

	private static final String PAR_ADD_COUNT = "addingPerStep";
	private static final String PAR_ADD_START = "startAdd";
	private static final String PAR_ADD_END = "endAdd";

	public final int ADDING_COUNT;
	public final long ADDING_START;
	public final long ADDING_END;

	public AddProtocol(String n) {
		this.ADDING_COUNT = Configuration.getInt(n + "." + PAR_ADD_COUNT, 0);
		this.ADDING_START = Configuration.getInt(n + "." + PAR_ADD_START,
				Integer.MAX_VALUE);
		this.ADDING_END = Configuration.getInt(n + "." + PAR_ADD_END,
				Integer.MAX_VALUE);
	}

	public boolean execute() {

		final long currentTimestamp = CommonState.getTime();
		final ChurnProtocol churn = ChurnProtocol.current;

		if (currentTimestamp >= this.ADDING_START
				&& currentTimestamp <= this.ADDING_END) {
			// ADD ELEMENTS

			for (int i = 0; i < this.ADDING_COUNT
					&& churn.availableNodes.size() > 0; i++) {
				final Node current = churn.availableNodes.poll();
				final Dynamic d = (Dynamic) current.getProtocol(churn.pid);
				d.up();
				if (churn.graph.size() > 0) {
					final Node contact = churn.graph.get(CommonState.r
							.nextInt(churn.graph.size()));
					churn.addNode(current, contact);
				}
				churn.graph.add(current);
			}
		}

		return false;
	}
}

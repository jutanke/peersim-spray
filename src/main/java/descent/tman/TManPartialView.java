package descent.tman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import peersim.core.CommonState;
import peersim.core.Node;

public class TManPartialView extends HashSet<Node> {

	/**
	 * Get a random peer from the map
	 * 
	 * @return Node
	 */
	public Node getRandom() {
		Integer size = this.size();
		Iterator<Node> it = this.iterator();
		Integer random = CommonState.r.nextInt(size);

		Node result = null;
		for (int i = 0; i < random; ++i) {
			result = it.next();
		}
		return result;
	}

	List<Node> getSample(final TMan other, double size) {
		ArrayList<Node> sample = new ArrayList<Node>();
		
		Comparator<IDescriptor> ranking = new Comparator<IDescriptor>()  {

			public int compare(IDescriptor o1, IDescriptor o2) {
				if (other.descriptor.ranking(o1) < other.descriptor.ranking(o2))
				return 0;
			}
			
		};
		
		Collections.sort(sample, );
		
		
		return sample;
	}

}

package descent.observers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

import descent.controllers.DynamicNetwork;
import descent.rps.APeerSamplingProtocol;
import descent.rps.IPeerSampling;
import descent.spray.Spray;
import descent.tman.Descriptor;
import descent.tman.TMan;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;

/**
 * Created by julian on 26/01/15.
 */
public class DictGraph {

	/*
	 * =================================================================== *
	 * SINGLETON
	 * ===================================================================
	 */

	private static DictGraph singleton;

	public static DictGraph getSingleton(int size) {
		if (singleton == null) {
			singleton = new DictGraph(size);
		}
		return singleton;
	}

	/*
	 * =================================================================== *
	 * PROPERTIES
	 * ===================================================================
	 */

	// public final GraphNode[] nodes;
	public final Map<Long, DictNode> nodes;

	private List<DictNode> neighbourhood;

	private final List<IPeerSampling> pssList;
	private final Map<Long, Integer> dist;
	private final LinkedList<DictNode> Q;

	private DictGraph(int size) {
		this.pssList = new ArrayList<IPeerSampling>();
		this.neighbourhood = new ArrayList<DictNode>();
		this.dist = new HashMap<Long, Integer>();
		// this.prev = new int[size];
		this.Q = new LinkedList<DictNode>();
		this.nodes = new HashMap<Long, DictNode>(size);
	}

	/*
	 * =================================================================== *
	 * PUBLIC
	 * ===================================================================
	 */

	public int size() {
		return this.nodes.size();
	}

	public void reset() {
		this.pssList.clear();
		this.nodes.clear();
	}

	public void add(Node n, IPeerSampling c) {
		DictNode node = new DictNode(n.getID());
		for (Node neighbor : c.getPeers(Integer.MAX_VALUE)) {
			node.neighbors.add(neighbor.getID());
		}
		if (this.nodes.containsKey(n.getID()))
			throw new Error("should never happen");
		this.nodes.put(n.getID(), node);
		this.pssList.add(c);
	}

	public void addStrict(Node n, IPeerSampling pss) {
		DictNode node = new DictNode(n.getID());
		for (Node neighbor : pss.getAliveNeighbors()) {
			node.neighbors.add(neighbor.getID());
		}
		if (this.nodes.containsKey(n.getID())) {
			throw new RuntimeException("Noo.. :(");
		}
		this.nodes.put(n.getID(), node);
		this.pssList.add(pss);
	}

	public AvgReachablePaths avgReachablePathsBothDirections(long v) {
		return null;
	}

	public AvgReachablePaths avgReachablePaths(long v) {
		Map<Long, Integer> dist = dijkstra(nodes.get(v));

		// Map<Long, Integer> dist = dijkstraUndirected(nodes.get(v).id);

		AvgReachablePaths result = new AvgReachablePaths();

		double sum = 0;
		int reachable = 0;
		for (int d : dist.values()) {
			if (d != -1.0) {
				reachable += 1;
				sum += (double) d;
			}
		}

		if (reachable <= 1) {
			result.avg = 0;
		} else {
			result.avg = sum / (reachable - 1); // do not count ourselfs!
		}

		result.count = reachable;
		result.total = this.nodes.size();
		result.reachQuota = reachable / (double) result.total;

		return result;
	}

	public class AvgReachablePaths {
		public double avg;
		public int count;
		public int total;
		public double reachQuota;

		@Override
		public String toString() {
			return "avg:" + avg + "| %:" + reachQuota + " |count:" + this.count + " |total:" + total;
		}
	}

	/**
	 * how often is the pss called this time frame
	 *
	 * @return
	 */
	public int[] histogramPassiveWorkDistribution() {
		final int[] distribution = new int[this.pssList.size()];
		for (int i = 0; i < distribution.length; i++) {
			// distribution[i] = this.pssList.get(i).callsInThisCycle();
		}

		int max = 1;
		for (int i : distribution) {
			if (i + 1 > max) {
				max = i + 1;
			}
		}

		final int[] histo = new int[max];

		for (int i : distribution) {
			histo[i]++;
		}

		return histo;
	}

	// public int[] totalOutboundCostPerTick() {
	// final int[] costs = new int[(int) CommonState.getEndTime() - 1];
	// for (IRandomPeerSampling rps : this.pssList) {
	// for (int i = 0; i < rps.generatedPeerSamplingCost().length - 1; i++) {
	// costs[i] += rps.generatedPeerSamplingCost()[i];
	// }
	// }
	// return costs;
	// }

	public MeanPathLength meanPathLength() {
		MeanPathLength result = new MeanPathLength();
		result.maxReachQuota = Double.MIN_VALUE;
		result.minReachQuota = Double.MAX_VALUE;

		for (DictNode n : this.nodes.values()) {
			AvgReachablePaths avg = avgReachablePaths(n.id);
			result.avg += avg.avg;
			result.reachQuota += avg.reachQuota;
			if (avg.reachQuota > result.maxReachQuota) {
				result.maxReachQuota = avg.reachQuota;
			}
			if (avg.reachQuota < result.minReachQuota) {
				result.minReachQuota = avg.reachQuota;
			}
		}

		result.avg /= this.nodes.size();
		result.reachQuota /= this.nodes.size();

		return result;
	}

	public class MeanPathLength {
		public double avg;
		public double reachQuota;
		public double minReachQuota;
		public double maxReachQuota;

		@Override
		public String toString() {
			return "avg:" + avg + "| %:" + reachQuota + "| min%:" + minReachQuota + "| max%:" + maxReachQuota;
		}
	}

	public Integer diameter() {

		// id1_id2 : Distance between 1 and 2
		Map<String, Integer> d = new HashMap<String, Integer>();

		Map<Long, Map<Long, Integer>> lookup = new HashMap<Long, Map<Long, Integer>>();

		for (DictNode e : this.nodes.values()) {
			lookup.put(e.id, dijkstra(e));
		}

		Integer diameter = 0;
		for (Long source : lookup.keySet()) {
			for (Long arrival : lookup.get(source).keySet()) {
				diameter = Math.max(diameter, lookup.get(source).get(arrival));
			}
		}
		return diameter;
	}

	public double averagePathLength() {

		// id1_id2 : Distance between 1 and 2
		Map<String, Integer> d = new HashMap<String, Integer>();

		Map<Long, Map<Long, Integer>> lookup = new HashMap<Long, Map<Long, Integer>>();

		for (DictNode e : this.nodes.values()) {
			lookup.put(e.id, dijkstra(e));
		}

		for (DictNode a : this.nodes.values()) {
			for (DictNode b : this.nodes.values()) {
				String key = key(a.id, b.id);
				if (a.id != b.id) {
					int d1 = lookup.get(a.id).get(b.id);
					int d2 = lookup.get(b.id).get(a.id);
					if (d1 == -1 && d2 == -1)
						d.put(key, 0);
					else if (d1 == -1)
						d.put(key, d2);
					else if (d2 == -1)
						d.put(key, d1);
					else
						d.put(key, Math.min(d1, d2));
				} else {
					d.put(key, 0);
				}
			}
		}

		// =======================================

		int n = this.nodes.size();

		if (n > 1) {
			int sum = 0;
			for (int distance : d.values()) {
				sum += distance;
			}
			return (2.0 / n * (n * 1.0)) * (double) sum;
		}
		return 0;
	}

	private String key(long id1, long id2) {
		return Math.min(id1, id2) + "_" + Math.max(id1, id2);
	}

	@Override
	public String toString() {
		// return this.nodes.toString();
		StringBuilder sb = new StringBuilder();

		for (DictNode n : this.nodes.values()) {
			sb.append("{");
			sb.append(n.id);
			sb.append("}->[");
			for (long neighbor : n.neighbors) {
				sb.append(" ");
				sb.append(neighbor);
			}
			sb.append("]\n\r");
		}

		return sb.toString();
	}

	public String toGraph() {
		StringBuilder sb = new StringBuilder();

		sb.append("digraph test {");
		sb.append("\n");
		for (DictNode n : this.nodes.values()) {
			for (long neighbor : n.neighbors) {
				sb.append(n.id);
				sb.append(" -> ");
				sb.append(neighbor);
				sb.append(";\n");
			}
			sb.append("\n");
		}
		sb.append("}");

		return sb.toString();
	}

	private static LinkedList<HashSet<Long>> networksDB = new LinkedList<HashSet<Long>>();

	public double modularityCoefficient() {
		Integer m = this.countArcs();
		Double norm = ((double) m); // directed
		// Double norm = (double)2*m); // undirected
		Double sum = 0.0;
		if (DictGraph.networksDB.size() == 0 && DynamicNetwork.networks.get(0).size() > 0) {
			for (int i = 0; i < DynamicNetwork.networks.size(); ++i) {
				HashSet<Long> hs = new HashSet<Long>();
				for (int j = 0; j < DynamicNetwork.networks.get(i).size(); ++j) {
					hs.add(DynamicNetwork.networks.get(i).get(j).getID());
				}
				DictGraph.networksDB.add(hs);
			}
		}
		for (Long origin : this.nodes.keySet()) {
			for (Long destination : this.nodes.keySet()) {
				Double adjacent = 0.;
				if (this.nodes.get(origin).neighbors.contains(destination)) {
					adjacent = 1.;
				}
				Double modularity = adjacent
						- this.nodes.get(origin).neighbors.size() * this.nodes.get(destination).neighbors.size() / norm;
				boolean found = false;
				int i = 0;
				Double delta = 0.;
				while (!found && i < DictGraph.networksDB.size()) {
					if (DictGraph.networksDB.get(i).contains(origin)) {
						found = true;
						if (DictGraph.networksDB.get(i).contains(destination)) {
							delta = 1.;
						}
					}
					++i;
				}
				sum += modularity * delta;
			}
		}
		return sum / norm;
	}

	public double meanClusterCoefficient() {
		double sum = 0;
		for (DictNode e : this.nodes.values()) {
			sum += localClusterCoefficient(e);
		}
		return sum / this.nodes.size();
	}

	public double globalClusterCoefficient() {
		// directed graph
		Integer closed2Paths = 0;
		Integer open2Paths = 0;
		for (Long origin : this.nodes.keySet()) {
			for (Long second : this.nodes.get(origin).neighbors) {
				for (Long third : this.nodes.get(second).neighbors) {
					if (third != origin) {
						boolean found = false;
						int i = 0;
						while (!found && i < this.nodes.get(third).neighbors.size()) {
							if (this.nodes.get(third).neighbors.get(i) == origin) {
								found = true;
							}
							++i;
						}
						if (found) {
							++closed2Paths;
						} else {
							++open2Paths;
						}
					}
				}
			}
		}
		return (double) closed2Paths / (double) open2Paths;
	}

	// =========

	public int countArcs() {
		int count = 0;
		for (DictNode node : this.nodes.values()) {
			count += node.neighbors.size();
		}
		return count;
	}

	public int countArcsNoDuplicates() {
		int count = 0;
		for (DictNode node : this.nodes.values()) {
			count += node.neighbors.size();
			ArrayList<Long> distinct = new ArrayList<Long>();
			for (Long neigbhors : node.neighbors) {
				if (distinct.contains(neigbhors)) {
					count -= 1;
				} else {
					distinct.add(neigbhors);
				}
			}
		}
		return count;
	}

	private HashSet<Long> explored;
	private Stack<Long> toExplore;

	public String countArcsInNetwork(Integer n) {
		this.explored = new HashSet<Long>();
		this.toExplore = new Stack<Long>();
		if (DynamicNetwork.networks.size() > n && DynamicNetwork.networks.get(n).size() > 1) {
			Node starter = DynamicNetwork.networks.get(n).get(0);
			this.toExplore.add(starter.getID());
			int count = 0;
			while (this.toExplore.size() > 0) {
				count += countArcsInNetwork2();
			}
			return explored.size() + " " + count;
		} else {
			return 0 + " " + 0;
		}
	}

	private int countArcsInNetwork2() {
		Long nodeId = this.toExplore.pop();
		if (!this.explored.contains(nodeId)) {
			this.explored.add(nodeId);
			DictNode currentNode = this.nodes.get(nodeId);
			for (Long neighbor : currentNode.neighbors) {
				if (!this.explored.contains(neighbor)) {
					this.toExplore.push(neighbor);
				}
			}
			return currentNode.neighbors.size();
		} else {
			return 0;
		}
	}

	public ArrayList<Integer> distributionOfMinPaths() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (Long node : this.nodes.keySet()) {
			Map<Long, Integer> distances = dijkstra(nodes.get(node));
			for (Integer distance : distances.values()) {
				while (result.size() < distance) {
					result.add(0);
				}
				result.set(distance, result.get(distance) + 1);
			}
		}
		return result;
	}

	public class DeliveryRateAndMsg {
		public final double softRate; // network coverage
		public final double hardRate; // proba that a peer did not get at least
										// 1 msg
		public final double nbMsg; // nb messages broadcast and rebroadcast
									// during the protocol of 1 msg
		public final Integer nbNodes;
		public final double fanout; // avg fanout of peers
		public final Integer nbBroadcast; // nb messages broadcast
		public final double atLeastOne;
		public final double lesserThanLessHardRate;
		public final double lessHardRate;

		public DeliveryRateAndMsg(double sr, double hr, double msg, Integer n, double f, Integer br,
				HashMap<Long, Integer> deliveries) {
			this.softRate = sr / (double) br;
			this.hardRate = 1 - hr / (double) br;
			this.nbMsg = msg / (double) br;
			this.nbNodes = n;
			this.fanout = f;
			this.nbBroadcast = br;

			Integer ninetynine = 0;
			Integer ninetyninedotnine = 0;
			Integer atLeastOne = 0;
			for (Entry<Long, Integer> keyval : deliveries.entrySet()) {
				Integer value = keyval.getValue();
				if (value >= (0.999 * nbBroadcast)) {
					ninetyninedotnine += 1;
				}
				if (value >= (0.99 * nbBroadcast)) {
					ninetynine += 1;
				}
				if (value > 1) {
					atLeastOne += 1;
				}
				if (value == 0) {
					System.out.println("SOMEONE IS DISCONNECTED OR HAS NO LUCK = " + keyval.getKey());
				}
			}
			this.atLeastOne = atLeastOne / (double) this.nbNodes;
			this.lessHardRate = ninetyninedotnine / (double) this.nbNodes;
			this.lesserThanLessHardRate = ninetynine / (double) this.nbNodes;
		}
	}

	public DeliveryRateAndMsg deliveryRate(Function<Integer, Integer> howMany, Integer N) {
		double sumOfResult = 0;
		ArrayList<Long> upNode = new ArrayList<Long>();
		for (Integer i = 0; i < DynamicNetwork.graph.size(); ++i) {
			APeerSamplingProtocol rps = (APeerSamplingProtocol) DynamicNetwork.graph.get(i)
					.getProtocol(APeerSamplingProtocol.pid);
			if (rps.isUp()) {
				upNode.add(DynamicNetwork.graph.get(i).getID());
			}
		}

		Integer failFullDelivery = 0;
		Integer sumFanout = 0;
		Integer sumExplored = 0;

		HashMap<Long, Integer> deliveries = new HashMap<Long, Integer>();
		for (Long node : upNode) {
			deliveries.put(node, 0);
		}

		Integer nbMsg = 0;
		if (upNode.size() > 0) {
			for (Integer i = 0; i < N; ++i) {
				HashSet<Long> explored = new HashSet<Long>();
				Stack<Long> toExplore = new Stack<Long>();

				Long starterId = upNode.get(CommonState.r.nextInt(upNode.size()));
				toExplore.add(starterId);
				while (toExplore.size() > 0) {
					Long nodeId = toExplore.pop();
					explored.add(nodeId);
					deliveries.put(nodeId, deliveries.get(nodeId) + 1);
					DictNode currentNode = this.nodes.get(nodeId);
					Integer fanout = howMany.apply(new Integer(currentNode.neighbors.size()));
					sumFanout += fanout;
					if (fanout > 0) {
						// #A explore $fanout$ neighbors at random
						ArrayList<Long> neighbors = new ArrayList<Long>(currentNode.neighbors);
						ArrayList<Long> chosen = new ArrayList<Long>(); // distinct
						Integer j = 0;
						while (j < fanout && neighbors.size() > 0) {
							Integer randomIndex = CommonState.r.nextInt(neighbors.size());
							Long neighbor = neighbors.get(randomIndex);
							neighbors.remove((int) randomIndex);
							if (!chosen.contains(neighbor)) {
								chosen.add(neighbor);
								if (upNode.contains(neighbor)) {
									++nbMsg;
									if (!explored.contains(neighbor) && !toExplore.contains(neighbor)) {
										toExplore.push(neighbor);
									}
								}
								++j;
							}
						}
					} else {
						// #B explore all
						for (Long neighbor : currentNode.neighbors) {
							if (upNode.contains(neighbor)) {
								++nbMsg;
								if (!explored.contains(neighbor) && !toExplore.contains(neighbor)) {
									toExplore.push(neighbor);
								}
							}
						}
					}
				}
				sumExplored += explored.size();
				if (upNode.size() != explored.size()) {
					failFullDelivery += 1;
					// HashSet<Long> meow = new HashSet<Long>(upNode);
					// meow.removeAll(explored);
					// System.out.println(meow.toString());
				}
				sumOfResult += (double) explored.size() / (double) upNode.size();
			}
		}
		return new DeliveryRateAndMsg(sumOfResult, failFullDelivery, nbMsg, upNode.size(),
				sumFanout / (double) sumExplored, N, deliveries);
	}

	public int[] inDegrees() {
		// ====== #1 Count the in-degree of all peers ======
		Map<Long, Integer> lookup = new HashMap<Long, Integer>(this.nodes.size());
		for (DictNode e : this.nodes.values()) {
			lookup.put(e.id, 0);
		}
		for (DictNode e : this.nodes.values()) {
			for (long n : e.neighbors) {
				lookup.put(n, lookup.get(n) + 1);
			}
		}

		// ====== #2 put the numers in a list ======
		List<Integer> degs = new ArrayList<Integer>(lookup.size());
		for (int deg : lookup.values()) {
			degs.add(deg);
		}
		int[] result = new int[lookup.size()];
		for (int i = 0; i < degs.size(); i++)
			result[i] = degs.get(i);
		return result;
	}

	public int[] outDegreeAsHistogram() {
		// ====== #1 Count the in-degree of all peers ======
		Map<Long, Integer> lookup = new HashMap<Long, Integer>(this.nodes.size());
		for (DictNode e : this.nodes.values()) {
			lookup.put(e.id, 0);
		}
		for (DictNode e : this.nodes.values()) {
			for (long n : e.neighbors) {
				lookup.put(n, lookup.get(n) + 1);
			}
		}

		// ====== #3 create histogram for the counts ======
		Map<Integer, Integer> histogram = new HashMap<Integer, Integer>();
		for (int inDegree : lookup.values()) {
			if (histogram.containsKey(inDegree)) {
				histogram.put(inDegree, histogram.get(inDegree) + 1);
			} else {
				histogram.put(inDegree, 1);
			}
		}

		// ====== #3 turn the lookup into an array ======
		int max = 0;
		for (int deg : histogram.keySet()) {
			if (deg > max)
				max = deg;
		}
		int[] result = new int[max + 1];
		for (int deg : histogram.keySet()) {
			result[deg] = histogram.get(deg);
		}
		return result;
	}

	/**
	 * Count how many partial views in the network have duplicates, triples,
	 * quadruples etc. The number of duplicates does not matter, if the pv has
	 * one, that its counted as +1
	 *
	 * @return
	 */
	public int countPartialViewsWithDuplicates() {
		final Set<Long> lookup = new HashSet<Long>();
		int duplicateCount = 0;
		for (DictNode e : this.nodes.values()) {
			lookup.clear();
			for (long n : e.neighbors) {
				if (lookup.contains(n)) {
					duplicateCount++;
					break;
				}
				lookup.add(n);
			}
		}
		return duplicateCount;
	}

	public class MaxPercResult {
		public final double Max;
		public final double MeanPartialViewSize;
		public final int MaxCount;
		public final int MaxSize;
		public final int NodeCount;

		public MaxPercResult(double m, double me, int mc, int ms, int nc) {
			this.Max = m;
			this.MeanPartialViewSize = me;
			this.MaxCount = mc;
			this.MaxSize = ms;
			this.NodeCount = nc;
		}

		@Override
		public String toString() {
			return Max + " ~" + MeanPartialViewSize + " " + MaxCount + "/" + MaxSize + " +" + NodeCount;
		}
	}

	/**
	 * @return maximum pct of duplicates in a view
	 */
	public MaxPercResult maxPercDuplicatesInView() {

		double max = 0;
		double mean = 0;
		int maxCount = 0;
		int maxSize = 0;

		final Map<Long, Integer> lookup = new HashMap<Long, Integer>();
		for (DictNode e : this.nodes.values()) {
			mean += e.neighbors.size();
			lookup.clear();
			for (long n : e.neighbors) {
				if (lookup.containsKey(n)) {
					lookup.put(n, lookup.get(n) + 1);
				} else {
					lookup.put(n, 1);
				}
			}
			final int size = e.neighbors.size();

			double maxV = 0;
			for (int v : lookup.values()) {
				if (v > maxV) {
					maxV = v;
					maxCount = v;
					maxSize = e.neighbors.size();
				}
			}
			if (size > 0) {
				double currentMax = (maxV - 1) / size;
				if (currentMax > max) {
					max = currentMax;
				}
			}
		}

		return new MaxPercResult(max, mean / this.nodes.size(), maxCount, maxSize, this.nodes.size());
	}

	public int[] duplicates() {

		final Map<Long, Integer> lookup = new HashMap<Long, Integer>();
		final Map<Integer, Integer> values = new HashMap<Integer, Integer>();

		for (DictNode e : this.nodes.values()) {
			lookup.clear();
			for (long n : e.neighbors) {
				if (lookup.containsKey(n)) {
					lookup.put(n, lookup.get(n) + 1);
				} else {
					lookup.put(n, 1);
				}
			}
			for (int v : lookup.values()) {
				if (values.containsKey(v)) {
					values.put(v, values.get(v) + 1);
				} else {
					values.put(v, 1);
				}
			}
		}

		int max = 0;
		for (int k : values.keySet()) {
			if (k > max) {
				max = k;
			}
		}

		final int[] result = new int[max + 1];

		for (int k : values.keySet()) {
			final int v = values.get(k);
			result[k] = v;
		}

		return result;
	}

	/**
	 * @return index of array = number of in-degree
	 */
	public int[] inDegreeAsHistogram() {

		// ====== #1 Count the in-degree of all peers ======
		Map<Long, Integer> lookup = new HashMap<Long, Integer>(this.nodes.size());
		for (DictNode e : this.nodes.values()) {
			lookup.put(e.id, 0);
		}
		for (DictNode e : this.nodes.values()) {
			for (long n : e.neighbors) {
				lookup.put(n, lookup.get(n) + 1);
			}
		}

		// ====== #3 create histogram for the counts ======
		Map<Integer, Integer> histogram = new HashMap<Integer, Integer>();
		for (int inDegree : lookup.values()) {
			if (histogram.containsKey(inDegree)) {
				histogram.put(inDegree, histogram.get(inDegree) + 1);
			} else {
				histogram.put(inDegree, 1);
			}
		}

		// ====== #3 turn the lookup into an array ======
		int max = 0;
		for (int deg : histogram.keySet()) {
			if (deg > max)
				max = deg;
		}
		int[] result = new int[max + 1];
		for (int deg : histogram.keySet()) {
			result[deg] = histogram.get(deg);
		}
		return result;
	}

	public double variancePartialView() {
		double var = 0;
		final double mean = this.getViewSizeStats().mean;
		final Collection<DictNode> N = this.nodes.values();
		for (DictNode n : N) {
			final double c = n.neighbors.size() - mean;
			var += c * c;
		}
		return var / N.size();
	}

	public double stdDeviationPartialView() {
		return Math.sqrt(this.variancePartialView());
	}

	public ClusterResult countClusters() {
		final HashMap<Long, DictNode> lookup = new HashMap<Long, DictNode>(this.nodes);
		int clusterCount = 0;
		int maxClusterSize = 0;
		int deadLinks = 0;
		final HashSet<Long> currentCluster = new HashSet<Long>();

		while (lookup.size() > 0) {
			DictNode r = (DictNode) lookup.values().toArray()[0];
			dfsMarking(r, currentCluster);
			if (currentCluster.size() > maxClusterSize) {
				maxClusterSize = currentCluster.size();
			}
			for (long c : currentCluster) {
				lookup.remove(c);
			}
			currentCluster.clear();
			clusterCount += 1;
		}

		return new ClusterResult(clusterCount, maxClusterSize, deadLinks);
	}

	private void dfsMarking(final DictNode node, final HashSet<Long> currentCluster) {
		final Stack<DictNode> stack = new Stack<DictNode>();
		stack.push(node);
		while (!stack.isEmpty()) {
			final DictNode current = stack.pop();
			if (!currentCluster.contains(current.id)) {
				currentCluster.add(current.id);
				for (long n : current.neighbors) {
					if (!currentCluster.contains(n)) {
						final DictNode neighbor = this.nodes.get(n);
						if (neighbor != null) {
							stack.push(neighbor);
						}
					}
				}
			}
		}

	}

	private void dfsMarkingRec(final DictNode n, final HashSet<Long> currentCluster) {
		if (!currentCluster.contains(n.id)) {
			currentCluster.add(n.id);
			for (long neighbor : n.neighbors) {
				if (!currentCluster.contains(neighbor)) {
					DictNode o = this.nodes.get(neighbor);
					dfsMarking(o, currentCluster);
				}
			}
		}
	}

	public class ClusterResult {
		public final int count;
		public final int maxClusterSize;
		public final int deadLinks;

		private ClusterResult(int c, int mCs, int dl) {
			this.count = c;
			this.maxClusterSize = mCs;
			this.deadLinks = dl;
		}

		@Override
		public String toString() {
			return "Cluster result:" + this.count + ", max cluster size:" + maxClusterSize;
		}
	}

	public enum NetworkX {
		Connectedness, Graph, Draw
	}

	public String networkxDigraph(NetworkX type) {
		return this.networkxDigraph(type, "graph", true);
	}

	public String networkxDigraph(NetworkX type, String graph, boolean importNetworkX) {

		StringBuilder sb = new StringBuilder();

		if (importNetworkX) {
			sb.append("import networkx as nx\n");
		}
		if (type == NetworkX.Draw && importNetworkX) {
			sb.append("import matplotlib.pyplot as plt\n");
			sb.append("from random import random\n");
			sb.append(
					"colors=[(random(),random(),random()) for _i in range(" + DynamicNetwork.networks.size() + ")]\n");
		}

		final String progName = "exec" + graph;

		sb.append("def ");
		sb.append(progName);
		sb.append("():\n");

		sb.append("\t");
		sb.append(graph);
		sb.append(" = nx.DiGraph()\n");

		for (DictNode e : this.nodes.values()) {
			if (e.neighbors.size() > 0) {
				for (long n : e.neighbors) {
					sb.append("\t");
					sb.append(graph);
					sb.append(".add_edge(");
					sb.append(e.id);
					sb.append(",");
					sb.append(n);
					sb.append(")\n");
				}
			} else {
				sb.append("\t");
				sb.append(graph);
				sb.append(".add_node(");
				sb.append(e.id);
				sb.append(")\n");
			}
		}

		// print("weak:" +
		// str(nx.algorithms.number_weakly_connected_components(MG)))
		// print("strong:" +
		// str(nx.algorithms.number_strongly_connected_components(MG)))

		switch (type) {
		case Connectedness:
			sb.append("\tprint(\"graph:");
			sb.append(graph);
			sb.append("\")\n");
			sb.append("\tprint(\"count:\" + str(");
			sb.append(graph);
			sb.append(".number_of_nodes()))\n");
			sb.append("\tprint(\"weak:\" + str(nx.algorithms.number_weakly_connected_components(");
			sb.append(graph);
			sb.append(")))\n");
			sb.append("\tprint(\"strong:\" + str(nx.algorithms.number_strongly_connected_components(");
			sb.append(graph);
			sb.append(")))\n");
			sb.append(progName);
			sb.append("()");
			break;
		case Graph:
			break;
		case Draw:
			int i = 0;
			for (LinkedList<Node> nodes : DynamicNetwork.networks) {
				if (nodes.size() > 0) {
					sb.append("\tlistNodes" + i + "= [");
					int j = 0;
					for (Node node : nodes) {
						sb.append(node.getID());
						++j;
						if (j < nodes.size()) {
							sb.append(',');
						}
					}
					sb.append("]\n");
				}
				++i;
			}

			sb.append("\tpos = nx.spring_layout(" + graph + ")\n");
			for (i = 0; i < DynamicNetwork.networks.size(); ++i) {
				sb.append("\tnx.draw(" + graph + ",pos , edge_color='#A9A9A9', nodelist= listNodes"
						+ (DynamicNetwork.networks.size() - i - 1) + ", node_size=40, node_color=colors[" + i
						+ "], with_labels=False)\n");
			}
			sb.append("\tplt.savefig('" + graph + "',dpi=225)\n");
			sb.append("\tplt.clf()\n");
			break;
		}
		sb.append(progName);
		sb.append("()\n");
		return sb.toString();
	}

	/*
	 * =================================================================== *
	 * PRIVATE
	 * ===================================================================
	 */

	public double localClusterCoefficient(long id) {
		return localClusterCoefficient(nodes.get(id));
	}

	public double localClusterCoefficient(DictNode v) {
		// List<DictNode> N = neighborhood(v);
		HashSet<Long> N = neighbors(v.id);
		if (N.size() == 0)
			return 0;
		double possible = N.size() * (N.size() - 1);
		if (possible == 0)
			return 0;
		double actual = 0;
		for (long a : N) {
			for (long b : N) {
				if (a != b) {
					// if (areUndirectlyConnected(a, b)) {
					if (hasDirectedConnection(a, b)) {
						actual += 1;
					}
				}
			}
		}
		return actual / possible;
	}

	public class ViewSizeStats {
		public final double mean;
		public final Integer min;
		public final Integer max;

		public ViewSizeStats(double mean, Integer min, Integer max) {
			this.mean = mean;
			this.min = min;
			this.max = max;
		}

		@Override
		public String toString() {

			return this.mean + " " + this.min + " " + this.max;
		}
	}

	public ViewSizeStats getViewSizeStats() {
		double mean = 0.;
		Integer max = 0;
		Integer min = Integer.MAX_VALUE;
		final Collection<DictNode> N = this.nodes.values();
		for (DictNode n : N) {
			mean += n.neighbors.size();
			if (max < n.neighbors.size()) {
				max = n.neighbors.size();
			}
			if (min > n.neighbors.size()) {
				min = n.neighbors.size();
			}
		}
		return new ViewSizeStats(mean / N.size(), min, max);
	}

	private boolean areUndirectlyConnected(long a, long b) {
		DictNode aNode = nodes.get(a);
		DictNode bNode = nodes.get(b);
		if (aNode == null || bNode == null) {
			return false;
		}
		return in(a, bNode.neighbors) || in(b, aNode.neighbors);
	}

	private boolean areInterconnected(long a, long b) {
		DictNode aNode = nodes.get(a);
		DictNode bNode = nodes.get(b);
		return in(a, bNode.neighbors) && in(b, aNode.neighbors);
	}

	/**
	 * get the immediately connected neighbours:
	 *
	 * @param v
	 * @return N_i = {v_j : e_ij \in E ^ e_ji \in E }
	 */
	public List<DictNode> neighbourhood(DictNode v) {
		this.neighbourhood.clear();
		// for (long n : v.neighbors) {
		for (long n : neighbors(v.id)) {
			if (hasDirectedConnection(n, v.id)) {
				this.neighbourhood.add(this.nodes.get(n));
			}
		}
		return this.neighbourhood;
	}

	private boolean hasDirectedConnection(long from, long to) {
		DictNode fromNode = this.nodes.get(from);
		return in(to, fromNode.neighbors);
	}

	private boolean in(long i, List<Long> list) {
		for (long n : list) {
			if (n == i)
				return true;
		}
		return false;
	}

	/**
	 * @param src
	 * @return
	 */
	public Map<Long, Integer> fastDijkstra(DictNode src) {
		dist.clear();

		FibonacciHeap Q = new FibonacciHeap();

		// http://keithschwarz.com/interesting/code/?dir=fibonacci-heap

		return dist;
	}

	public Map<Long, Integer> dijkstra(DictNode src) {
		return dijkstra(src.id);
	}

	/**
	 * @param src
	 * @return
	 */
	public Map<Long, Integer> dijkstra(long src) {
		dist.clear();
		Q.clear();

		final long source = src;
		final int INFINITY = -1;

		dist.put(source, 0);

		for (DictNode v : nodes.values()) {
			if (v.id != source) {
				dist.put(v.id, INFINITY);
			}
			Q.add(v);
		}

		while (Q.size() > 0) {
			DictNode u = min(Q, dist);
			if (u == null)
				break; // disconnected graph
			Q.remove(u);
			for (long v : u.neighbors) {
				int alt = dist.get(u.id) + 1;
				if (dist.get(v) == INFINITY || alt < dist.get(v)) {
					dist.put(v, alt);
				}
			}
		}
		return dist;
	}

	public Map<Long, Integer> dijkstraUndirected(final long source) {
		dist.clear();
		Q.clear();

		final int INFINITY = -1;

		dist.put(source, 0);

		for (DictNode v : nodes.values()) {
			if (v.id != source) {
				dist.put(v.id, INFINITY);
			}
			Q.add(v);
		}

		while (Q.size() > 0) {
			DictNode u = min(Q, dist);
			if (u == null)
				break; // disconnected graph
			Q.remove(u);
			for (long v : neighbors(u.id)) {
				int alt = dist.get(u.id) + 1;
				if (dist.get(v) == INFINITY || alt < dist.get(v)) {
					dist.put(v, alt);
				}
			}
		}

		return dist;
	}

	private HashSet<Long> neighbors(final long id) {
		final HashSet<Long> result = new HashSet<Long>(this.nodes.get(id).neighbors);
		for (DictNode e : this.nodes.values()) {
			if (e.id != id && !result.contains(e.id)) {
				for (long n : e.neighbors) {
					if (n == id) {
						result.add(e.id);
					}
				}
			}
		}
		return result;
	}

	private DictNode min(LinkedList<DictNode> Q, Map<Long, Integer> dist) {
		int m = Integer.MAX_VALUE;
		DictNode min = null;
		for (DictNode u : Q) {
			int distance = dist.get(u.id);
			if (distance >= 0 && m >= distance) {
				m = distance;
				min = u;
			}
		}
		// if (min == null && Q.size() > 0) {
		// return Q.get(0);
		// }
		return min;
	}

	/**
	 * Get the distribution of distances between neighbors at the given
	 * resolution
	 * 
	 * @param resolution
	 * @return distance => number of peers
	 */
	public HashMap<Double, Integer> getDistances(Integer resolution) {
		Double maximal = Descriptor.NUMBER;
		HashMap<Double, Integer> results = new HashMap<Double, Integer>();

		Double min = Double.POSITIVE_INFINITY;
		Double max = Double.NEGATIVE_INFINITY;

		ArrayList<Double> distances = new ArrayList<Double>();

		for (int i = 0; i < Network.size(); ++i) {
			Node n = Network.get(i);
			TMan nTMan = (TMan) n.getProtocol(TMan.pid);
			for (Node neighbor : nTMan.partialViewTMan) {
				TMan neighborTMan = (TMan) neighbor.getProtocol(TMan.pid);
				Double distance = nTMan.descriptor.ranking(neighborTMan.descriptor);
				if (min > distance) {
					min = distance;
				}
				if (max < distance) {
					max = distance;
				}
				// System.out.println("desc1 " + ((Descriptor)
				// nTMan.descriptor).x);
				// System.out.println("d " + distance);
				distances.add(distance);
			}
		}

		// Double bucketSize = (1+ max - min) / new Double(resolution);
		Double bucketSize = maximal / new Double(resolution);

		for (int i = 0; i < resolution; ++i) {
			// results.put(min + (i + 1) * (bucketSize / 2), 0);
			results.put(0 + (i + 1) * (bucketSize / 2), 0);
		}

		for (int i = 0; i < distances.size(); ++i) {
			Double bucket = Math.floor(distances.get(i) / bucketSize);
			// System.out.println("bucket " + bucket );
			// results.put(min + (bucket + 1) * (bucketSize / 2),
			// results.get(min + (bucket + 1) * (bucketSize / 2)) + 1);
			results.put(0 + (bucket + 1) * (bucketSize / 2), results.get(0 + (bucket + 1) * (bucketSize / 2)) + 1);
		}

		return results;
	}

	/**
	 * Count the number of "monitor". A peer is a monitor only if no peers in
	 * its neighborhoods is a monitor.
	 */
	public Integer countMonitors() {
		HashSet<Long> monitors = new HashSet<Long>();
		for (int i = 0; i < DynamicNetwork.networks.get(0).size(); ++i) {
			Node n = DynamicNetwork.networks.get(0).get(i);

			int j = 0;
			boolean found = false;
			while (n.isUp() && j < this.nodes.get(n.getID()).neighbors.size() && !found) {
				if (monitors.contains(this.nodes.get(n.getID()).neighbors.get(j))) {
					found = true;
				}
				++j;
			}
			if (!found) {
				monitors.add(n.getID());
			}
		}
		return monitors.size();
	}

	/**
	 * Count writers. Same as "monitors" but the assigning is made at joining
	 * time and is not based on the same principle.
	 */
	public ArrayList<Integer> countWriters() {
		ArrayList<Integer> sums = new ArrayList<Integer>();

		for (int i = 0; i < DynamicNetwork.networks.get(0).size(); ++i) {
			Node n = DynamicNetwork.networks.get(0).get(i);
			Spray s = (Spray) n.getProtocol(Spray.pid);
			while (sums.size() <= s.rank) {
				sums.add(0);
			}
			sums.set(s.rank, sums.get(s.rank) + 1);
		}

		return sums;
	}

	/**
	 * Count the number of hop in average to get to a writer.
	 * 
	 * @param N
	 *            The number of tries
	 * @return
	 */
	public Double findWriter(Integer N) {
		Integer sum = 0;
		for (int i = 0; i < N; ++i) {
			Integer hop = 0;
			Node current = DynamicNetwork.networks.get(0)
					.get(CommonState.r.nextInt(DynamicNetwork.networks.get(0).size()));
			Spray currentSpray = (Spray) current.getProtocol(Spray.pid);
			while (!currentSpray.rank.equals(0)) {
				Node next = currentSpray.getPeers(1).get(0);
				currentSpray = (Spray) next.getProtocol(Spray.pid);
				++hop;
			}
			sum += hop;
		}

		return (sum / new Double(N));

	}
}

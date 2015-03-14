package example.paper.cyclon;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by julian on 3/14/15.
 */
public class Cyclon implements Linkable, EDProtocol, CDProtocol, example.PeerSamplingService {

    private static final String PAR_CACHE = "cache";
    private static final String PROT = "0";
    private static final String PAR_L = "l";
    private static final String PAR_TRANSPORT = "transport";
    private static final int DELTA_T = 5; // randomize parameter to spread the shuffle speed

    // ===========================================
    // P R O P E R T I E S
    // ===========================================

    private final int size;
    private final int l;
    private final int tid,pid;
    private final boolean isUnitTest;
    private int myStep;
    private boolean isBlocked = false;
    private List<Node> peers;
    private int timeoutCounter = 0;
    private final int MAX_TIMEOUT = 10;

    private List<CyclonEntry> cache = null;
    private List<Event> events;

    private List<CyclonEntry> currentSentSubset;

    // ===========================================
    // C T O R
    // ===========================================

    /**
     * Only used for unit tests!
     *
     * @param size
     * @param l
     */
    public Cyclon(int size, int l) {
        this.size = size;
        this.l = l;
        this.tid = -1;
        this.pid = -1;
        this.cache = new ArrayList<CyclonEntry>(size);
        this.isUnitTest = true;
        this.myStep = nextInt(DELTA_T - 1);
    }

    /**
     * Called by the simulation
     *
     * @param n
     */
    public Cyclon(String n) {
        this.size = Configuration.getInt(n + "." + PAR_CACHE);
        this.l = Configuration.getInt(n + "." + PAR_L);
        this.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);
        this.pid = Configuration.getPid(n + "." + PROT);
        this.cache = new ArrayList<CyclonEntry>(size);
        this.isUnitTest = false;
        this.myStep = nextInt(DELTA_T - 1);
        this.events = new ArrayList<Event>();
        this.peers = new ArrayList<Node>();
    }

    @Override
    public Object clone() {
        Cyclon cyclon = null;
        try {
            cyclon = (Cyclon) super.clone();
            cyclon.cache = new ArrayList<CyclonEntry>(size);
            cyclon.myStep = nextInt(DELTA_T - 1);
            cyclon.events = new ArrayList<Event>();
            cyclon.peers = new ArrayList<Node>();
        } catch (CloneNotSupportedException e) {
        } // never happens
        // ...
        return cyclon;
    }

    // ===========================================
    // P U B L I C
    // ===========================================

    @Override
    public void nextCycle(Node node, int protocolID) {

        if (!this.isBlocked) {
            // run stacked events
            for (Event e : this.events) {
                this.processEvent(e.node, e.pid, e.message);
            }
            this.events.clear();
        }

        if (!this.isBlocked && this.cache.size() > 0 && CommonState.getTime() % DELTA_T == this.myStep) {

            this.isBlocked = true;

            this.increaseAge();

            Node q = this.popOldest();

            List<CyclonEntry> send = this.popRandomSubset(this.l - 1);
            this.currentSentSubset = new ArrayList<CyclonEntry>(send);
            send.add(new CyclonEntry(0, node)); // add own address

            this.send(q, CyclonMessage.shuffle(node, send));
        } else {
            // break deadlocks and dead nodes
            this.timeoutCounter += 1;
            if (this.timeoutCounter > MAX_TIMEOUT) {
                // ROLLBACK
                this.timeoutCounter = 0;
                this.isBlocked = false;
                for (CyclonEntry ce : this.currentSentSubset) {
                    this.insert(ce);
                }
            }
        }

    }

    @Override
    public int degree() {
        return this.cache.size();
    }

    @Override
    public Node getNeighbor(int i) {
        return null;
    }

    @Override
    public boolean addNeighbor(Node neighbour) {
        return false;
    }

    @Override
    public boolean contains(Node neighbor) {
        return this.contains(new CyclonEntry(0, neighbor)); // TODO make this better
    }

    @Override
    public void pack() {

    }

    @Override
    public void onKill() {

    }

    @Override
    public void processEvent(Node node, int pid, Object event) {

        CyclonMessage message = (CyclonMessage) event;

        List<CyclonEntry> received = null;
        List<CyclonEntry> send = null;

        switch (message.type) {
            case Shuffle:
                if (this.isBlocked) {
                    // We are currently initiating a shuffle with someone else
                    // careful: this might result in a deadlock!
                    this.events.add(new Event(pid, node, message));
                } else {
                    Node p = message.sender;
                    received = message.send;
                    send = this.popRandomSubset(this.l);
                    this.insertReceivedItems(received, send, node.getID());
                    this.send(p, CyclonMessage.shuffleResponse(node, send, message));
                }
                break;
            case ShuffleResponse:
                if (!this.isBlocked) {
                    throw new RuntimeException("must be blocking!");
                }
                received = message.send;
                send = message.received; // the message that we send back then..
                this.insertReceivedItems(received, send, node.getID());
                this.isBlocked = false;
                break;
        }
    }

    @Override
    public List<Node> getPeers() {
        this.peers.clear();
        for (CyclonEntry ce : this.cache) {
            this.peers.add(ce.n);
        }
        return this.peers;
    }

    @Override
    public String debug() {
        StringBuilder sb = new StringBuilder();
        Collections.sort(this.cache, new CyclonEntry());
        for (CyclonEntry ce : this.cache) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(ce);
        }
        return sb.toString();
    }

    // ===========================================
    // P R I V A T E
    // ===========================================

    public String debugIds() {
        StringBuilder sb = new StringBuilder();
        Collections.sort(this.cache, new CyclonEntry());
        for (CyclonEntry ce : this.cache) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(ce.n.getID());
        }
        return sb.toString();
    }

    /**
     * Increment the age of our local cache and sort it
     */
    public void increaseAge() {
        for (CyclonEntry ce : this.cache) {
            ce.age += 1;
        }
    }

    /**
     * Selects the oldest Node and removes it from the list
     *
     * @return the oldest Node or NULL (if the list is empty)
     */
    public Node popOldest() {
        Collections.sort(cache, new CyclonEntry());
        if (this.cache.size() > 0) {
            Node result = this.cache.get(this.cache.size() - 1).n;
            this.cache.remove(this.cache.size() - 1);
            return result;
        }
        return null;
    }

    /**
     * finds the position of the cache entry regarding the node. As the cache
     * can only contain one pointer towards this node this is no problem
     *
     * @param ce
     * @return
     */
    public int indexOf(CyclonEntry ce) {
        return this.indexOf(ce.n);
    }

    /**
     * ...
     *
     * @param n
     * @return
     */
    public int indexOf(Node n) {
        for (int i = 0; i < this.cache.size(); i++) {
            if (this.cache.get(i).n.getID() == n.getID()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets a subset of size "l" and removes it from the local cache
     *
     * @param l
     * @return subset
     */
    public List<CyclonEntry> popRandomSubset(int l) {
        List<CyclonEntry> subset;
        if (l >= this.cache.size()) {
            subset = new ArrayList<CyclonEntry>(this.cache);
            this.cache.clear();
        } else {
            subset = new ArrayList<CyclonEntry>(l);
            for (int i = 0; i < l; i++) {
                int pos = nextInt(this.cache.size() - 1);
                subset.add(this.cache.get(pos));
                this.cache.remove(pos);
            }
        }
        return subset;
    }

    private int nextInt(int n) {
        if (isUnitTest) {
            return new Random().nextInt(n);
        } else {
            return CommonState.r.nextInt(n);
        }
    }

    /**
     * merges the sent and received data with the current local cache
     * It first adds the received elements and then the sent ones, if there is still
     * enough space
     *
     * @param received
     * @param sent
     */
    public void insertReceivedItems(List<CyclonEntry> received, List<CyclonEntry> sent, long ownId) {
        if (this.cache.size() + received.size() > this.size) {
            throw new RuntimeException("Cannot merge all received elements: Overflow");
        }

        for (CyclonEntry ce : received) {
            if (ce.n.getID() != ownId) {
                this.insert(ce);
            }
        }

        Collections.sort(sent, new CyclonEntry()); // take the youngest first!

        for (CyclonEntry ce : sent) {
            if (ce.n.getID() != ownId) {
                this.insert(ce);
            }
        }

    }

    /**
     * check
     *
     * @param ce
     * @return
     */
    public boolean contains(CyclonEntry ce) {
        return this.indexOf(ce) > -1;
    }

    /**
     * unsorted!
     *
     * @param ce
     */
    public void insert(CyclonEntry ce) {
        if (!this.contains(ce) && this.cache.size() < this.size) {
            this.cache.add(ce);
        }
    }

    /**
     * send a message to the network
     * @param destination
     * @param message
     */
    public void send(Node destination, CyclonMessage message) {
        final Node sender = message.sender;
        if (sender.getID() == destination.getID()) {
            throw new RuntimeException("must not send to oneself");
        }
        Transport tr = (Transport) sender.getProtocol(this.tid);
        tr.send(sender, destination, message, pid);
    }

    /**
     * to ensure no overlapping
     */
    private class Event {
        public int pid;
        public Node node;
        public CyclonMessage message;
        public Event(int pid, Node node, CyclonMessage message) {
            this.pid = pid;
            this.node = node;
            this.message = message;
        }
    }
}

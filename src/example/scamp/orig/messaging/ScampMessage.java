package example.scamp.orig.messaging;

import peersim.core.Node;

/**
 * Created by julian on 01/02/15.
 */
public abstract class ScampMessage {

    public final Node sender;
    private final int ttl;

    public static final int START_TTL = 100;

    public ScampMessage(Node sender, int ttl) {
        this.sender = sender;
        this.ttl = ttl;
    }

    public ScampMessage(Node sender) {
        this.sender = sender;
        this.ttl = START_TTL;
    }

    public boolean isExpired() {
        return this.ttl <= 0;
    }
}

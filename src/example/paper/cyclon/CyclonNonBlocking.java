package example.paper.cyclon;

import peersim.core.CommonState;
import peersim.core.Fallible;
import peersim.core.Node;

import java.util.List;

/**
 * Created by julian on 3/23/15.
 */
public class CyclonNonBlocking extends CyclonProtocol {

    // ===========================================
    // C T O R
    // ===========================================

    private int step;
    private final int STEP_RANGE = 10;

    public CyclonNonBlocking(String prefix) {
        super(prefix);
        this.step = CommonState.r.nextInt(STEP_RANGE) + 1;
    }

    @Override
    public Object clone() {
        CyclonNonBlocking e = (CyclonNonBlocking) super.clone();
        e.step = CommonState.r.nextInt(STEP_RANGE) + 1;
        return e;
    }

    // ===========================================
    // P U B L I C
    // ===========================================

    @Override
    public void processMessage(Node me, CyclonMessage message) {
        if (me.isUp()) {
            final Node destination;
            final List<CyclonEntry> sent, received;
            switch (message.type) {

                case Shuffle:
                    destination = me; // hacky..
                    sent = this.getSample(l);
                    received = message.send;
                    final CyclonMessage response = CyclonMessage.shuffleResponse(me, sent, message);
                    this.insertLists(me, destination, received, sent);
                    this.send(message.sender, response);
                    break;
                case ShuffleResponse:
                    destination = message.sender;
                    sent = message.received;
                    received = message.send;
                    this.insertLists(me, destination, received, sent);
                    break;
                default:
                    throw new RuntimeException("unhandled event");

            }
        }
    }

    @Override
    public void nextCycle(Node node, int protocolID) {
        if (node.isUp() && (CommonState.getTime() % this.step) == 0) {
            this.increaseAge();
            final Node oldest = this.oldest();
            if (oldest == null) {
                System.err.println("nop");
                return;
            }
            final List<CyclonEntry> nodesToSend = this.getSample(l - 1);
            final CyclonMessage message = CyclonMessage.shuffle(node, nodesToSend);
            this.send(oldest, message);
        }
    }

    // ===========================================
    // P R I V A T E
    // ===========================================
}

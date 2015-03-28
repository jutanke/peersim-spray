package example.Scamplon;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.edsim.EDProtocol;

/**
 * THIS IMPLEMENTATION IGNORES THE
 *
 * Created by julian on 2/5/15.
 */
public abstract class ScamplonProtocol implements Linkable, EDProtocol, CDProtocol, example.PeerSamplingService {

    public static int c, tid, pid;

    // ============================================
    // E N T I T Y
    // ============================================

    private static final String PAR_C = "c";
    public static final String SCAMPLON_PROT = "0";
    private static final String PAR_TRANSPORT = "transport";

    public ScamplonProtocol(String n) {
        c = Configuration.getInt(n + "." + PAR_C, 0);
        tid = Configuration.getPid(n + "." + PAR_TRANSPORT);
        pid = Configuration.lookupPid(SCAMPLON_PROT);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    // ============================================
    // P U B L I C
    // ============================================

    @Override
    public void pack() {

    }

    @Override
    public void onKill() {

    }


}

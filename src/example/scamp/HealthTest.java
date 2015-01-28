package example.scamp;

import peersim.config.Configuration;
import peersim.core.Control;

/**
 * CODE TAKEN FROM: https://github.com/csko/Peersim/tree/master/scamp
 *
 * <p/>
 * Created by julian on 28/01/15.
 */
public class HealthTest implements Control {

    // ===================== fields =======================================
    // ====================================================================

    /**
     * The protocol to operate on.
     *
     * @config
     */
    private static final String PAR_PROT = "protocol";

    /**
     * The name of this observer in the configuration
     */
    private final String name;

    private final int protocolID;


    // ===================== initialization ================================
    // =====================================================================


    public HealthTest(String name) {

        this.name = name;
        protocolID = Configuration.getInt(name + "." + PAR_PROT);
    }


    // ====================== methods ======================================
    // =====================================================================


    public boolean execute() {

        System.out.println(name + ": " + Scamp.test(protocolID));

        return false;
    }
}

package example.paper.scamplon;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julian on 3/27/15.
 */
public class Churn implements Control {

    private static final String PROTOCOL = "o1";
    private static final String PAR_ADD_COUNT = "adding";
    private static final String PARR_REM_COUNT = "removing";
    private static final String PAR_ADD_START = "startadd";
    private static final String PAR_REM_START = "startrem";
    private static final String PAR_ADD_END = "startadd";
    private static final String PAR_REM_END = "startrem";

    public final int ADDING_COUNT;
    public final int REMOVING_COUNT;
    public final int ADDING_START;
    public final int REMOVING_START;
    public final int REMOVING_END;
    public final int ADDING_END;

    List<Node> graph = new ArrayList<Node>();

    public Churn(String n) {
        this.ADDING_COUNT = Configuration.getInt(n + "." + PAR_ADD_COUNT, 0);
        this.REMOVING_COUNT = Configuration.getInt(n + "." + PARR_REM_COUNT, 0);
        this.ADDING_START = Configuration.getInt(n + "." + PAR_ADD_START, Integer.MAX_VALUE);
        this.REMOVING_START = Configuration.getInt(n + "." + PAR_REM_START, Integer.MAX_VALUE);
        this.REMOVING_END = Configuration.getInt(n + "." + PAR_ADD_END, Integer.MAX_VALUE);
        this.ADDING_END = Configuration.getInt(n + "." + PAR_REM_END, Integer.MAX_VALUE);
    }

    @Override
    public boolean execute() {

        final long currentTimestamp = CommonState.getTime();
        if (currentTimestamp >= this.ADDING_START && currentTimestamp <= this.ADDING_END) {
            // ADD ELEMENTS

        }

        if (currentTimestamp >= this.REMOVING_START && currentTimestamp <= this.REMOVING_END) {
            // REMOVE ELEMENTS

        }

        return false;
    }
}

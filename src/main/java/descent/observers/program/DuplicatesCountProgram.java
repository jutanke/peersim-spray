package descent.observers.program;

import descent.observers.DictGraph;
import descent.observers.ObserverProgram;

/**
 * Created by julian on 08/05/15.
 */
public class DuplicatesCountProgram implements ObserverProgram {

    public void tick(long currentTick, DictGraph observer) {
        //System.out.println(print(observer.duplicates()));
        System.out.println(observer.countPartialViewsWithDuplicates());
    }

    public void onLastTick(DictGraph observer) {

    }

    private String print(int[] l) {
        final StringBuilder sb = new StringBuilder();
        for (int i : l) {
            sb.append(i);
            sb.append(" ");
        }
        return sb.toString();
    }
}

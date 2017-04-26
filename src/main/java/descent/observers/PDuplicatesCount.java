package descent.observers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julian on 08/05/15.
 */
public class PDuplicatesCount implements ObserverProgram {

    final List<DictGraph.MaxPercResult> maxPerc = new ArrayList<DictGraph.MaxPercResult>();

    public void tick(long currentTick, DictGraph observer) {
        //System.out.println(print(observer.duplicates()));
        //System.out.println(observer.size() + " " + observer.maxPercDuplicatesInView());
        if (currentTick > 25) this.maxPerc.add(observer.maxPercDuplicatesInView());
        //System.out.println(observer.countPartialViewsWithDuplicates());
    }

    public void onLastTick(DictGraph observer) {
        double total = 0;
        double totalMax = 0;
        double totalSize = 0;
        for (DictGraph.MaxPercResult v : this.maxPerc) {
            total += v.Max;
            totalMax += v.MaxCount;
            totalSize += v.MaxSize;

        }
        System.out.println(observer.size() + " " + (total/this.maxPerc.size()) +
                " " + totalMax / this.maxPerc.size() +
                "/" + totalSize/this.maxPerc.size() + " " +
                " ~" + this.maxPerc.get(0).MeanPartialViewSize +
            " ");
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

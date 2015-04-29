package descent.observers;

/**
 * Created by julian on 4/29/15.
 */
public interface ObserverProgram {

    public void tick(long currentTick, DictGraph observer);

    public void onLastTick(DictGraph observer);

}

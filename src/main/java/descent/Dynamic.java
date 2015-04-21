package descent;

/**
 * Simple interface to indicate if a node is up or down. Somehow the build-in failstate does not
 * work correctly
 * Created by julian on 3/27/15.
 */
public interface Dynamic {

    boolean isUp();
    void up();
    void down();
    int hash();
    int degree();

}

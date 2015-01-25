package example.webrtc;

import java.util.List;

/**
 * Created by julian on 24/01/15.
 */
public interface PeerSamplingService {

    long id();

    List<PeerSamplingService> getPeers();

}

package shared;

import java.util.ArrayList;

/**
 * Created by nhattran on 27/3/17.
 */
public interface ICallback {
    void handle(ArrayList<String> payloads);
}

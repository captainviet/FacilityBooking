package shared;

/**
 * TODO: Describe purpose and behavior of Network
 */
public class Network {

    static final int LOSS_RATE = 3;
    static int counter = 0;
    static boolean isReliable = false;

    public static boolean attemptingTransmission() {
        counter = (counter + 1) % LOSS_RATE;
        if (!isReliable && counter == 0) {
            return false;
        }
        return true;
    }
}

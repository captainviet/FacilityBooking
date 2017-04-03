package shared;

/**
 * TODO: Describe purpose and behavior of Network
 */
public class Network {

    static final int LOSS_RATE = 3;
    static int counter = 0;
    static boolean isReliable = false;

    private enum Host {
        Server(0),
        Client(1);

        int code;

        private Host(int code) {
            this.code = code;
        }
    }

    public static boolean serverPacketLost() {
        return isPacketLost(Host.Server);
    }

    public static boolean clientPacketLost() {
        return isPacketLost(Host.Client);
    }

    private static boolean isPacketLost(Host host) {
        counter = (counter + 1) % LOSS_RATE;
        if (!isReliable && counter == host.code) {
            return true;
        }
        return false;
    }
}

package client;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by nhattran on 24/3/17.
 */
public class Reply {
    public final static int ERROR_REPLY_CODE = 1;
    private DatagramPacket packet;
    private ArrayList<String> payloads;
    private String error;
    public int statusCode;
    public Reply() {
        byte[] byteData = new byte[1024];
        packet = new DatagramPacket(byteData, byteData.length);
    }

    public DatagramPacket getPacket() {
        return packet;
    }

    public void Unmarshal(){
        int cursor = 0;
        byte[] byteArray = packet.getData();
        statusCode = (int) byteArray[cursor++];
        while (byteArray[cursor] != Byte.MIN_VALUE) {
            int pLength = (int) byteArray[cursor++];
            String p = new String(Arrays.copyOfRange(byteArray, cursor, cursor + pLength));
            payloads.add(p);
        }
        if (statusCode == ERROR_REPLY_CODE) {
            error = payloads.get(0);
        }
    }

    public ArrayList<String> getPayloads() {
        return payloads;
    }

    public String getError() {
        return error;
    }
}

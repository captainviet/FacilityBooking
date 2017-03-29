package client;

import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nhattran on 24/3/17.
 */
public class Request {
    private static long counter = 0;
    private String requestType;
    private ArrayList<String> payloads;
    private String id;
    private InetAddress serverHost;
    private int serverPort;
    public Request(String clientIp, String requestType, ArrayList<String> payloads, InetAddress serverHost, int serverPort) {
        counter++;
        this.id = clientIp + '[' + counter + ']';
        this.requestType = requestType;
        this.payloads = payloads;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public byte[] marshal() {
        byte[] idByte = id.getBytes();
        byte[] requestTypeByte = requestType.getBytes();

        byte[] byteArray = new byte[32768];
        int cursor = 0;

        byteArray[cursor++] = (byte) idByte.length;
        System.arraycopy(idByte, 0, byteArray, cursor, idByte.length);
        cursor += idByte.length;
        byteArray[cursor++] = (byte) requestTypeByte.length;
        System.arraycopy(requestTypeByte, 0, byteArray, cursor, requestTypeByte.length);
        cursor += requestTypeByte.length;
        for (String p: payloads) {
            byte[] byteP = p.getBytes();
            byteArray[cursor++] = (byte) byteP.length;
            System.arraycopy(byteP, 0, byteArray, cursor, byteP.length);
            cursor += byteP.length;
        }
        byteArray[cursor] = Byte.MIN_VALUE;
        return byteArray;
    }

    public DatagramPacket getPacket(){
        byte[] data = this.marshal();
        return new DatagramPacket(data, data.length, serverHost, serverPort);
    }

    public String getType() {
        return this.requestType;
    }

    public ArrayList<String> getPayloads() {
        return payloads;
    }
}

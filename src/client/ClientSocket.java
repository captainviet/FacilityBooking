package client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by nhattran on 24/3/17.
 */
public class ClientSocket {
    private DatagramSocket socket;
    private String error;
    public boolean hasError;
    public String error() {
        return error;
    }

    public ClientSocket()  {
        try {
            socket = new DatagramSocket();
        } catch (SocketException se) {
            hasError = true;
            error = se.getMessage();
        }
    }

    private void close() {
        if (socket != null) {
            socket.close();
        }
    }

    public void sendRequest(Request request) {
        hasError = false;
        error = null;
        try {
            socket.send(request.getPacket());
        } catch (IOException ie) {
            hasError = true;
            error =  ie.getMessage();
        }
    }

    public Reply receiveReply(boolean willClose) {
        Reply reply = new Reply();
        hasError = false;
        error = null;
        try {
            socket.receive(reply.getPacket());
        } catch (IOException e) {
            hasError = true;
            error = e.getMessage();
        }
        if (willClose) close();
        return reply;
    }
}

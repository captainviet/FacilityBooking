package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import shared.Network;
import shared.Request;

/**
 * Created by nhattran on 24/3/17.
 */
public class ClientSocket {
    public static final String TIMEOUT = "timeout";
    public static final int timeout = 5000;
    private DatagramSocket socket;
    public static final int MAX_PACKET_SIZE = 32768;
    private String error;
    private InetAddress serverHost;
    private int serverPort;

    public String error() {
        return error;
    }

    public ClientSocket(InetAddress serverHost, int serverPort) throws SocketException {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        socket = new DatagramSocket();
        socket.setSoTimeout(timeout);
    }

    public void close() {
        if (socket != null) {
            socket.close();
        }
    }

    public void sendRequest(Request request) {
        clearError();
        byte[] data = Request.marshal(request);
		DatagramPacket packet = new DatagramPacket(data, data.length, serverHost, serverPort);
    	try {
    		if (!Network.attemptingTransmission()) {
    			Thread.sleep(timeout);
    			System.out.println("Timeout transmitting request");
    		}
    		System.out.println("Transmitting...");
    		socket.send(packet);
    	} catch (IOException ioe) {
    		error = ioe.getStackTrace().toString();
    	} catch (InterruptedException e) {
			error = e.getStackTrace().toString();
		} 
    }

    public byte[] receiveReply() {
        clearError();
        byte[] data = new byte[MAX_PACKET_SIZE];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        try {
            socket.receive(packet);
            System.out.println("Received reply.");
        } catch (IOException e) {
            if (e instanceof SocketTimeoutException) {
                error = TIMEOUT;
            } else {
                error = e.getMessage();
            }
        }
        System.out.println(new String(packet.getData()));
        return packet.getData();
    }

    public void setTimeOut(int timeInSeconds) throws SocketException {
        if (timeInSeconds * 1000 >= Integer.MAX_VALUE || timeInSeconds <= 0) {
            return;
        }
        socket.setSoTimeout(timeInSeconds * 1000);
    }

    public String getIp() {
        return this.socket.getLocalAddress().getHostAddress();
    }

    private void clearError() {
        error = null;
    }
}

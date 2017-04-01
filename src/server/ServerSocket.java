package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import client.Reply;
import shared.ICallback;

public class ServerSocket {
	private DatagramSocket socket;
	private int port;
	private String error;
	private InetAddress clientHost;
	private int clientPort;
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public static final int MAX_PACKET_SIZE = 32768;
	public ServerSocket(int port) throws SocketException{
		this.port = port;
		socket = new DatagramSocket(port);
	}
	
	public InetAddress getHost() {
		return socket.getLocalAddress();
	}
	
	public byte[] receiveRequest(){
		clearError();
		byte[] data = new byte[MAX_PACKET_SIZE];
    	DatagramPacket packet = new DatagramPacket(data, data.length);
    	try {
    		socket.receive(packet);
    	} catch (IOException ie) {
    		error = ie.getMessage();
    		return null;
    	}
    	clientHost = packet.getAddress();
    	clientPort = packet.getPort();
    	return packet.getData();
	}
	
	public void sendReply(Reply reply) {
		clearError();
		byte[] data = Reply.marshal(reply);
		DatagramPacket packet = new DatagramPacket(data, data.length, clientHost, clientPort);
		try {
			socket.send(packet);
		} catch (IOException ie) {
			error = ie.getMessage();
		}
	}
	
	public String error(){
		return error;
	}
	
	private void clearError() {
		error = null;
	}
}

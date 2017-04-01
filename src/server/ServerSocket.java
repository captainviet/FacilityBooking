package server;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import shared.ICallback;

public class ServerSocket {
	private DatagramSocket socket;
	private int port;
	private String error;
	
	public ServerSocket(int port) {
		try {
            socket = new DatagramSocket(port);
        } catch (SocketException se) {
            error = se.getMessage();
        }
	}
	
	public InetAddress getHost() {
		return socket.getLocalAddress();
	}
	
	public String receiveRequest(ICallback callback){
		
		return null;
	}
	
	public void sendReply() {
		
	}
	
	public String error(){
		return error;
	}
	
	private void clearError() {
		error = null;
	}
}

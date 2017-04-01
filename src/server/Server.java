package server;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

/**
 * TODO: Describe purpose and behavior of Server
 */
public class Server {

    private enum SemanticsMode {
        AT_MOST_ONCE,
        AT_LEAST_ONCE
    }
    
    private ServerSocket serverSocket;
    private SemanticsMode mode;
    private int serverPort;
    private InetAddress serverHost;
    private HashMap<String, ReplyRecord> replyHistory;
    public Server(SemanticsMode mode, int serverPort) {
    	this.mode = mode;
    	this.serverPort = serverPort;
    }
    
    public void start() {
    	serverSocket = new ServerSocket(serverPort);
    	serverHost = serverSocket.getHost();
    	System.out.printf("Server listening on host: %s, port: %s", serverHost.getHostName(), serverPort);
    }
    
    
    private void filterRequest() {
    	
    }
   

}

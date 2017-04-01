package server;

public class ServerUI {

	private static int serverPort;
	private static int serverMode;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		serverPort = Integer.parseInt(args[0]);
		serverMode = Integer.parseInt(args[1]);
		Server server = new Server(serverMode, serverPort);
		try {
        	server.start();
        } catch (Exception e) {
        	e.printStackTrace();
        	return;
        }
		while (true) {
			
		}
	}

}

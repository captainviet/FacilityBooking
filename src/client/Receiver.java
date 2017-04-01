package client;

import java.util.concurrent.Callable;

import shared.ICallback;

public class Receiver implements Callable<String> {
	private ClientSocket clientSocket;
	private boolean multipleReply;
	private ICallback callback;
	private Request request;
	public Receiver(Request request, ClientSocket clientSocket, boolean multipleReply, ICallback f) {
		this.clientSocket = clientSocket;
		this.multipleReply = multipleReply;
		this.callback = f;
	}
	@Override
	public String call() throws Exception {
		// TODO Auto-generated method stub
		while(true) {
            byte[] data = clientSocket.receiveReply();
            String error = clientSocket.error();
            if (error != null) {
            	if (error != ClientSocket.TIMEOUT) {
            		return error;
            	}
            	System.out.println("Timeout receving reply. Retransmit requess...");
            	clientSocket.sendRequest(request);
            	continue;
            }
            Reply reply = Reply.Unmarshal(data);
            if (reply.statusCode == Reply.ERROR_REPLY_CODE) {
                return clientSocket.error();
                
            } else {
                if (multipleReply && reply.getPayloads().get(0) == Client.STOP_MONITOR) {
                    break;
                }
                callback.handle(reply.getPayloads());
                if (!multipleReply) {
                    break;
                }
            }
        }
		return null;
	}

}

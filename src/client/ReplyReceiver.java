package client;

import java.util.concurrent.Callable;

import shared.ICallback;
import shared.Reply;
import shared.Request;

public class ReplyReceiver implements Callable<String> {
	private ClientSocket clientSocket;
	private boolean multipleReply;
	private ICallback callback;
	private Request request;
	public ReplyReceiver(Request request, ClientSocket clientSocket, boolean multipleReply, ICallback f) {
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
            if (error != null){
            	if (error == ClientSocket.TIMEOUT) {
            		System.out.println("Timeout receiving reply. Retransmit request...");
            		clientSocket.sendRequest(request);
            		continue;
            	}
            	return error;
            }
            Reply reply = Reply.unmarshal(data);
            if (reply.statusCode == Reply.ERROR_REPLY_CODE) {
                return clientSocket.error();
                
            } else {
                if (multipleReply && reply.getPayloads().get(0).equals(Client.STOP_MONITOR)) {
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

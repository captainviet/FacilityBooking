package client;

import java.util.concurrent.Callable;

import shared.Constant;
import shared.ICallback;
import shared.Reply;
import shared.Request;

public class ReplyReceiver implements Callable<String> {
	private ClientSocket clientSocket;
    private static enum Reply_Mode {
    	NORMAL,
    	MONITOR
    }
	private Reply_Mode replyReceiveMode;
	private ICallback callback;
	private Request request;
	public ReplyReceiver(Request request, ClientSocket clientSocket, boolean multipleReply, ICallback f) {
		this.clientSocket = clientSocket;
		this.replyReceiveMode = multipleReply ? Reply_Mode.MONITOR : Reply_Mode.NORMAL;
		this.callback = f;
		this.request = request;
	}
	@Override
	public String call() throws Exception {
		// TODO Auto-generated method stub
		while(true) {
            byte[] data = clientSocket.receiveReply();
            String error = clientSocket.error();
            if (error != null){
            	if (error.equals(ClientSocket.TIMEOUT)) {
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
                callback.handle(reply.getPayloads());
                if (replyReceiveMode == Reply_Mode.NORMAL) {
                    break;
                } else {
                	while (true) {
                		data = clientSocket.receiveReply();
                        error = clientSocket.error();
                        if (error != null) {
                        	if (!error.equals(ClientSocket.TIMEOUT)) {
                        		return error;
                        	} 
                        	continue;
                        }
                        reply = Reply.unmarshal(data);
                        callback.handle(reply.getPayloads());
                        if (reply.getPayloads().get(0).equals(Constant.STOP_MONITOR)) {
                        	break;
                        }
                	}
                }
            }
        }
		return null;
	}

}

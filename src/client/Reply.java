package client;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.SSLEngineResult.Status;

/**
 * Created by nhattran on 24/3/17.
 */
public class Reply {
    public final static int ERROR_REPLY_CODE = 1;
    private final static String MESSAGE_END_CODE = "end";
    private ArrayList<String> payloads;
    private String errorMessage;
    public int statusCode;
    public int getStatusCode() {
		return statusCode;
	}

    private Reply() {}
    
	public Reply(boolean hasError, ArrayList<String> payloads) {
		this.statusCode = hasError ? 1 : 0;
		if (hasError) {
			this.errorMessage = payloads.get(0);
		} 
		this.payloads = payloads;
    }

    public static Reply unmarshal(byte[] data){
    	Reply reply = new Reply();
        int cursor = 0;
        int statusCode = (int) data[cursor++] & 0xff;
        ArrayList<String> payloads = new ArrayList<>();
        while (payloads.get(payloads.size() - 1) != "end") {
        	ByteBuffer buffer = ByteBuffer.wrap(Arrays.copyOfRange(data, cursor, cursor + 4));
    		cursor += 4;
    		int pLength = buffer.getInt();
    		payloads.add(new String(Arrays.copyOfRange(data, cursor, pLength)));
    		cursor += pLength;
        }
        reply.setPayloads(payloads);
        if (statusCode == ERROR_REPLY_CODE) {
            reply.setErrorMessage(payloads.get(0));
        }
        reply.setStatusCode(statusCode);
        return reply;
    }

    public static byte[] marshal(Reply reply) {
    	byte[] data = new byte[ClientSocket.MAX_PACKET_SIZE];
    	int cursor = 0;
    	data[cursor++] = (byte) reply.getStatusCode();
    	ByteBuffer buffer = ByteBuffer.allocate(4);
    	if (reply.getStatusCode() == ERROR_REPLY_CODE) {
    		byte[] errorMessageBytes = reply.getErrorMessage().getBytes();
    		buffer.putInt(errorMessageBytes.length);
    		System.arraycopy(buffer.array(), 0, data, cursor, 4);
    		System.arraycopy(errorMessageBytes, 0, data, cursor, errorMessageBytes.length);
            cursor += errorMessageBytes.length;
            buffer.clear();
    	} else {
            for (String p: reply.getPayloads()) {
                byte[] byteP = p.getBytes();
                buffer.putInt(byteP.length);
                System.arraycopy(buffer.array(), 0, data, cursor, 4);
                cursor += 4;
                System.arraycopy(byteP, 0, data, cursor, byteP.length);
                cursor += byteP.length;
                buffer.clear();
            }
    	}
    	return data;
    }
    
    private void setErrorMessage(String errorMessage) {
		// TODO Auto-generated method stub
		this.errorMessage = errorMessage;
	}
    
	private void setPayloads(ArrayList<String> payloads) {
		// TODO Auto-generated method stub
		this.payloads = payloads;
	}

	private void setStatusCode(int statusCode) {
    	this.statusCode = statusCode;
		// TODO Auto-generated method stub
		
	}

	public ArrayList<String> getPayloads() {
        return payloads;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

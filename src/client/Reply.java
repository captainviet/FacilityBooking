package client;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by nhattran on 24/3/17.
 */
public class Reply {
    public final static int ERROR_REPLY_CODE = 1;
    private ArrayList<String> payloads;
    private String errorMessage;
    public int statusCode;
    public Reply() {
    }

    public static Reply Unmarshal(byte[] data){
    	Reply reply = new Reply();
        int cursor = 0;
        int statusCode = (int) data[cursor++];
        ArrayList<String> payloads = new ArrayList<>();
        while (data[cursor] != Byte.MIN_VALUE) {
            int pLength = (int) data[cursor++];
            String p = new String(Arrays.copyOfRange(data, cursor, cursor + pLength));
            payloads.add(p);
        }
        reply.setPayloads(payloads);
        if (statusCode == ERROR_REPLY_CODE) {
            reply.setError(payloads.get(0));
        }
        reply.setStatusCode(statusCode);
        return reply;
    }

    private void setError(String errorMessage) {
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

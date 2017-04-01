package client;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by nhattran on 24/3/17.
 */
public class Request {
	private final static String MESSAGE_END_CODE = "end";
    private static long counter = 0;
    private String requestType;
    public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	private ArrayList<String> payloads;
    public void setPayloads(ArrayList<String> payloads) {
		this.payloads = payloads;
	}

	private String id;
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	private Request() {
		
	}
	
    public Request(String clientIp, String requestType, ArrayList<String> payloads) {
        counter++;
        this.id = clientIp + '[' + counter + ']';
        this.requestType = requestType;
        this.payloads = payloads;
        this.payloads.add(MESSAGE_END_CODE);
    }

    public static byte[] marshal(Request request) {
        byte[] idBytes = request.getId().getBytes();
        byte[] requestTypeBytes = request.getRequestType().getBytes();

        byte[] data = new byte[ClientSocket.MAX_PACKET_SIZE];
        int cursor = 0;

        data[cursor++] = (byte) idBytes.length;
        System.arraycopy(idBytes, 0, data, cursor, idBytes.length);
        cursor += idBytes.length;
        data[cursor++] = (byte) requestTypeBytes.length;
        System.arraycopy(requestTypeBytes, 0, data, cursor, requestTypeBytes.length);
        cursor += requestTypeBytes.length;
        ByteBuffer buffer = ByteBuffer.allocate(4);
        for (String p: request.getPayloads()) {
            byte[] byteP = p.getBytes();
            buffer.putInt(byteP.length);
            System.arraycopy(buffer.array(), 0, data, cursor, 4);
            cursor += 4;
            System.arraycopy(byteP, 0, data, cursor, byteP.length);
            cursor += byteP.length;
            buffer.clear();
        }
        return data;
    }

    public String getType() {
        return this.requestType;
    }

    public ArrayList<String> getPayloads() {
        return payloads;
    }
    
    public static Request unmarshal(byte[] data) {
    	Request request = new Request();
    	int cursor = 0;
    	
    	int idLength  = (int) data[cursor++] & 0xFF;
    	String id = new String(Arrays.copyOfRange(data, cursor, idLength));
    	request.setId(id);
    	cursor += idLength;
    	int typeLength = (int) data[cursor++] & 0xFF;
    	String requestType = new String(Arrays.copyOfRange(data, cursor, typeLength));
    	request.setRequestType(requestType);
    	cursor += typeLength;
    	ArrayList<String> payloads = new ArrayList<>();
    	while(payloads.get(payloads.size() - 1) != MESSAGE_END_CODE) {
    		ByteBuffer buffer = ByteBuffer.wrap(Arrays.copyOfRange(data, cursor, cursor + 4));
    		cursor += 4;
    		int pLength = buffer.getInt();
    		payloads.add(new String(Arrays.copyOfRange(data, cursor, pLength)));
    		cursor += pLength;
    	}
    	request.setPayloads(payloads);
		return request;
    }
    
}

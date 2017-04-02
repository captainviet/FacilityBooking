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
	public static final String HELP = "h";
    public static final String QUERY = "q";
    public static final String BOOK = "b";
    public static final String EDIT = "e";
    public static final String MONITOR = "m";
    public static final String GET_ALL = "a";
    public static final String CANCEL = "c";
    public static final String QUIT = "Q";
    public static final String INVALID = "INVALID";
	private final static String MESSAGE_END_CODE = "end";
    private static long counter = 0;
    private String type;
    public String getType() {
		return type;
	}

	public void setType(String requestType) {
		this.type = requestType;
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
        this.type = requestType;
        this.payloads = payloads;
    }

    public static byte[] marshal(Request request) {
        byte[] idBytes = request.getId().getBytes();
        byte[] requestTypeBytes = request.getType().getBytes();

        byte[] data = new byte[ClientSocket.MAX_PACKET_SIZE];
        int cursor = 0;

        data[cursor++] = (byte) idBytes.length;
        System.arraycopy(idBytes, 0, data, cursor, idBytes.length);
        cursor += idBytes.length;
        data[cursor++] = (byte) requestTypeBytes.length;
        System.arraycopy(requestTypeBytes, 0, data, cursor, requestTypeBytes.length);
        cursor += requestTypeBytes.length;
        ByteBuffer buffer = ByteBuffer.allocate(4);
        ArrayList<String> payloads = request.getPayloads();
        payloads.add(MESSAGE_END_CODE);
        for (String p: request.getPayloads()) {
            byte[] byteP = p.getBytes();
            buffer.putInt(byteP.length);
            System.arraycopy(buffer.array(), 0, data, cursor, 4);
            cursor += 4;
            System.arraycopy(byteP, 0, data, cursor, byteP.length);
            cursor += byteP.length;
            buffer.clear();
        }
        payloads.remove(MESSAGE_END_CODE);
        return data;
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
    	request.setType(requestType);
    	cursor += typeLength;
    	ArrayList<String> payloads = new ArrayList<>();
    	while(payloads.get(payloads.size() - 1).equals(MESSAGE_END_CODE)) {
    		ByteBuffer buffer = ByteBuffer.wrap(Arrays.copyOfRange(data, cursor, cursor + 4));
    		cursor += 4;
    		int pLength = buffer.getInt();
    		payloads.add(new String(Arrays.copyOfRange(data, cursor, pLength)));
    		cursor += pLength;
    	}
    	payloads.remove(MESSAGE_END_CODE);
    	request.setPayloads(payloads);
		return request;
    }
    
}

package shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
    //    private final static String MESSAGE_END_CODE = "end";
    private static long counter = 0;
    private String type;

    public String getType() {
        return type;
    }

    //    public void setType(String requestType) {
    //        this.type = requestType;
    //    }

    private List<String> payloads = new ArrayList<>();

    //    public void setPayloads(ArrayList<String> payloads) {
    //        this.payloads = payloads;
    //    }

    private String id;

    public String getId() {
        return id;
    }

    //    public void setId(String id) {
    //        this.id = id;
    //    }

    public static Request constructRequest(String clientIp, String requestType, List<String> payloads) {
        counter++;
        String id = clientIp + '[' + counter + ']';
        return new Request(id, requestType, payloads);
    }

    private Request(String id, String type, List<String> payloads) {
        this.id = id;
        this.type = type;
        this.payloads.addAll(payloads);
    }

    //    public Request(String clientIp, String requestType, ArrayList<String> payloads) {
    //        counter++;
    //        this.id = clientIp + '[' + counter + ']';
    //        this.type = requestType;
    //        this.payloads = payloads;
    //    }

    public static byte[] marshal(Request request) {
        String requestId = request.getId();
        String requestType = request.getType();
        List<String> requestData = request.getPayloads();
        StringBuilder data = new StringBuilder();
        data.append(requestId).append(Constant.REQUEST_DELIM);
        data.append(requestType).append(Constant.REQUEST_DELIM);
        data.append(requestData.size()).append(Constant.REQUEST_DELIM);
        for (String s : requestData) {
            data.append(s).append(Constant.REQUEST_DELIM);
        }
        return data.toString().getBytes();
        //        byte[] idBytes = request.getId().getBytes();
        //        byte[] requestTypeBytes = request.getType().getBytes();
        //
        //        byte[] data = new byte[ClientSocket.MAX_PACKET_SIZE];
        //        int cursor = 0;
        //
        //        data[cursor++] = (byte) idBytes.length;
        //        System.arraycopy(idBytes, 0, data, cursor, idBytes.length);
        //        cursor += idBytes.length;
        //        data[cursor++] = (byte) requestTypeBytes.length;
        //        System.arraycopy(requestTypeBytes, 0, data, cursor, requestTypeBytes.length);
        //        cursor += requestTypeBytes.length;
        //        ByteBuffer buffer = ByteBuffer.allocate(4);
        //        ArrayList<String> payloads = request.getPayloads();
        //        payloads.add(MESSAGE_END_CODE);
        //        for (String p : request.getPayloads()) {
        //            byte[] byteP = p.getBytes();
        //            buffer.putInt(byteP.length);
        //            System.arraycopy(buffer.array(), 0, data, cursor, 4);
        //            cursor += 4;
        //            System.arraycopy(byteP, 0, data, cursor, byteP.length);
        //            cursor += byteP.length;
        //            buffer.clear();
        //        }
        //        payloads.remove(MESSAGE_END_CODE);
        //        return data;
    }

    public List<String> getPayloads() {
        return payloads;
    }

    public static Request unmarshal(byte[] data) {
        String dataStr = new String(data);
        Scanner scanner = new Scanner(dataStr);
        String requestId = scanner.next();
        String requestType = scanner.next();
        int payloadSize = scanner.nextInt();
        List<String> payloads = new ArrayList<>();
        for (int i = 0; i < payloadSize; i++) {
            String s = scanner.next();
            payloads.add(s);
        }
        scanner.close();
        return new Request(requestId, requestType, payloads);
        //        Request request = new Request();
        //        int cursor = 0;
        //
        //        int idLength = data[cursor++] & 0xFF;
        //        String id = new String(Arrays.copyOfRange(data, cursor, idLength));
        //        request.setId(id);
        //        cursor += idLength;
        //        int typeLength = data[cursor++] & 0xFF;
        //        String requestType = new String(Arrays.copyOfRange(data, cursor, typeLength));
        //        request.setType(requestType);
        //        cursor += typeLength;
        //        ArrayList<String> payloads = new ArrayList<>();
        //        while (payloads.get(payloads.size() - 1).equals(MESSAGE_END_CODE)) {
        //            ByteBuffer buffer = ByteBuffer.wrap(Arrays.copyOfRange(data, cursor, cursor + 4));
        //            cursor += 4;
        //            int pLength = buffer.getInt();
        //            payloads.add(new String(Arrays.copyOfRange(data, cursor, pLength)));
        //            cursor += pLength;
        //        }
        //        payloads.remove(MESSAGE_END_CODE);
        //        request.setPayloads(payloads);
        //        return request;
    }

}

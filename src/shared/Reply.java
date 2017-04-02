package shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by nhattran on 24/3/17.
 */
public class Reply {
    public final static int ERROR_REPLY_CODE = 1;
    public final static int SUCCESS_REPLY_CODE = 0;
    //    private final static String MESSAGE_END_CODE = "end";
    private ArrayList<String> payloads = new ArrayList<>();
    private String errorMessage;
    public int statusCode;

    public int getStatusCode() {
        return statusCode;
    }

    private Reply(int statusCode, List<String> payloads) {
        this.statusCode = statusCode;
        if (statusCode == ERROR_REPLY_CODE) {
            this.errorMessage = payloads.get(0);
        }
        this.payloads.addAll(payloads);
    }

    public static Reply constructReply(boolean hasError, List<String> payloads) {
        int statusCode = hasError ? ERROR_REPLY_CODE : SUCCESS_REPLY_CODE;
        return new Reply(statusCode, payloads);
    }

    public static Reply unmarshal(byte[] data) {
        String dataStr = new String(data);
        Scanner scanner = new Scanner(dataStr);
        int statusCode = scanner.nextInt();
        int payloadSize = scanner.nextInt();
        List<String> payloads = new ArrayList<>();
        for (int i = 0; i < payloadSize; i++) {
            String s = scanner.next();
            payloads.add(s);
        }
        scanner.close();
        return new Reply(statusCode, payloads);
        //        Reply reply = new Reply();
        //        int cursor = 0;
        //        int statusCode = data[cursor++] & 0xff;
        //        ArrayList<String> payloads = new ArrayList<>();
        //        while (payloads.get(payloads.size() - 1).equals(MESSAGE_END_CODE)) {
        //            ByteBuffer buffer = ByteBuffer.wrap(Arrays.copyOfRange(data, cursor, cursor + 4));
        //            cursor += 4;
        //            int pLength = buffer.getInt();
        //            payloads.add(new String(Arrays.copyOfRange(data, cursor, pLength)));
        //            cursor += pLength;
        //        }
        //        payloads.remove(MESSAGE_END_CODE);
        //        reply.setPayloads(payloads);
        //        if (statusCode == ERROR_REPLY_CODE) {
        //            reply.setErrorMessage(payloads.get(0));
        //        }
        //        reply.setStatusCode(statusCode);
        //        return reply;
    }

    public static byte[] marshal(Reply reply) {
        int statusCode = reply.getStatusCode();
        List<String> replyData = reply.getPayloads();
        StringBuilder data = new StringBuilder();
        data.append(statusCode).append(Constant.REQUEST_DELIM);
        data.append(replyData.size()).append(Constant.REQUEST_DELIM);
        for (String s : replyData) {
            data.append(s).append(Constant.REQUEST_DELIM);
        }
        return data.toString().getBytes();
        //    	byte[] data = new byte[ClientSocket.MAX_PACKET_SIZE];
        //    	int cursor = 0;
        //    	data[cursor++] = (byte) reply.getStatusCode();
        //    	ByteBuffer buffer = ByteBuffer.allocate(4);
        //    	ArrayList<String> payloads = reply.getPayloads();
        //        payloads.add(MESSAGE_END_CODE);
        //        for (String p: reply.getPayloads()) {
        //         	byte[] byteP = p.getBytes();
        //            buffer.putInt(byteP.length);
        //            System.arraycopy(buffer.array(), 0, data, cursor, 4);
        //            cursor += 4;
        //            System.arraycopy(byteP, 0, data, cursor, byteP.length);
        //            cursor += byteP.length;
        //            buffer.clear();
        //    	}
        //    	payloads.remove(MESSAGE_END_CODE);
        //        return data;
    }

    //    private void setErrorMessage(String errorMessage) {
    //        // TODO Auto-generated method stub
    //        this.errorMessage = errorMessage;
    //    }
    //
    //    private void setPayloads(ArrayList<String> payloads) {
    //        // TODO Auto-generated method stub
    //        this.payloads = payloads;
    //    }
    //
    //    private void setStatusCode(int statusCode) {
    //        this.statusCode = statusCode;
    //        // TODO Auto-generated method stub
    //
    //    }

    public ArrayList<String> getPayloads() {
        return payloads;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

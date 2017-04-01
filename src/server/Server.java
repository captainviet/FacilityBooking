package server;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import client.Reply;
import client.Request;
import shared.DayOfWeek;
import shared.FreeSlot;
import shared.ICallback;

/**
 * TODO: Describe purpose and behavior of Server
 */
public class Server {
	private final static String ERROR_INVALID_FACILITY_NAME = "Invalid facility name";
    private enum SemanticsMode {
        AT_MOST_ONCE,
        AT_LEAST_ONCE
    }
    
    private SemanticsMode semanticsModevalueOf(int value) {
    	return value == 0 ? SemanticsMode.AT_MOST_ONCE : SemanticsMode.AT_LEAST_ONCE;
    }
    private ServerSocket serverSocket;
    private SemanticsMode mode;
    private int serverPort;
    private InetAddress serverHost;
    private HashMap<String, ReplyRecord> replyHistory;
    public Server(int mode, int serverPort) {
    	this.mode = semanticsModevalueOf(mode);
    	this.serverPort = serverPort;
    }
    
    public void start() throws SocketException{
    	serverSocket = new ServerSocket(serverPort);
    	serverHost = serverSocket.getHost();
    	System.out.printf("Server listening on host: %s, port: %s", serverHost.getHostName(), serverPort);
    }
    
    public String receiveAndProcessRequest(ICallback callback) {
    	String error;
    	byte[] data = serverSocket.receiveRequest();
    	if (serverSocket.getError() != null) {
    		return serverSocket.getError();
    	} else {
    		Request request = Request.unmarshal(data);
    		error = handleRequest(request);
    		callback.printRequestResult(request, error);
    	}
    	return null;
    }
    
    private String handleRequest(Request request) {
    	switch(request.getRequestType()) {
    	case Request.QUERY:
    		 processQueryAvailabilityRequest(request.getPayloads());
    		break;
    	case Request.BOOK:
    		handleBookingRequest(request.getPayloads());  		
    		break;
    	case Request.EDIT:
    		handleEditBookingRequest(request.getPayloads());
    		break;
    	case Request.MONITOR:
    		handleMonitorRequest(request.getPayloads());
    		break;
    	case Request.CANCEL:
    		handleCancelRequest(request.getPayloads());
    		break;
    	case Request.GET_ALL:
    		handleGetAllRequest(request.getPayloads());
    		break;
    	default:
    		return "Invalid_operation";
    	}
    	return null;
    }
    
    private void handleGetAllRequest(ArrayList<String> payloads) {
		// TODO Auto-generated method stub
    	
	}

	private void handleCancelRequest(ArrayList<String> payloads) {
		// TODO Auto-generated method stub
		
	}

	private void handleMonitorRequest(ArrayList<String> payloads) {
		// TODO Auto-generated method stub
		
	}

	private void handleEditBookingRequest(ArrayList<String> payloads) {
		// TODO Auto-generated method stub
		
	}

	private void handleBookingRequest(ArrayList<String> payloads) {
		// TODO Auto-generated method stub
		
	}

	private String processQueryAvailabilityRequest(ArrayList<String> payloads) {
		ArrayList<String> result = new ArrayList<>();
		boolean hasError = false;
		String facilityName = payloads.get(0);
		Facility facility = Facility.getFacilityByName(facilityName);
		if (facility == null) {
			hasError = true;
			result.add(ERROR_INVALID_FACILITY_NAME);
		} else {
	    	String[] days = payloads.get(1).split(" ");
	    	for (String day : days) {
	    		StringBuilder resultOneDay = new StringBuilder();
	    		resultOneDay.append(day).append("|");
	    		DayOfWeek d = DayOfWeek.valueOf(Integer.parseInt(day));
	    		List<FreeSlot> freeSlots = QueryService.getAvailableFacility(facilityName, d);
	    		for (int i = 0; i < freeSlots.size(); i++) {
	    			resultOneDay.append(freeSlots.get(i).toString());
	    			if (i != freeSlots.size() - 1) { 
	    				resultOneDay.append("|");
	    			}
	    		}
	    		result.add(resultOneDay.toString());
	    	}
		}
    	Reply reply = new Reply(hasError, result);
    	return sendReply(reply);
    }
	
    private String sendReply(Reply reply) {
    	serverSocket.sendReply(reply);
    	if (serverSocket.getError() != null) {
    		return serverSocket.getError();
    	}
    	return null;
		
	}

	private void filterRequest() {
    	
    }
   
    

}

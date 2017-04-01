package client;

import shared.DateTime;
import shared.DayOfWeek;
import shared.Encoder;
import shared.ICallback;
import shared.Time;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 
 * @author Tran Vu Xuan Nhat
 *
 */
public class Client {
    public static final String START_MONITOR = "start_monitor";
    public static final String STOP_MONITOR = "stop_monitor";
    public static final String TIME_SERVER = "t_server";
    private final ExecutorService scheduler = Executors.newSingleThreadExecutor();
    private String clientIp;
    private InetAddress serverHost;
    private int serverPort;
    private ClientSocket clientSocket;

    /**
     * 
     * @param serverHost
     * @param serverPort
     */
    public Client(InetAddress serverHost, int serverPort) {
    	this.serverHost = serverHost;
    	this.serverPort = serverPort;
    }

    public void start() throws SocketException {
    	clientSocket = new ClientSocket(serverHost, serverPort);
    	clientIp = clientSocket.getIp();
    }
    /**
     * Return the error when querying for available slots of a facility in one or multiple days. Print all slots and return null if no error.
     * @param facilityName the facility name to be queried for available slot in days.
     * @param days the list of day to be queried for available slot of the facility with name facilityName.
     * @return the string error of query operation
     */
    public String queryAvailability(String facilityName, String days) {
    	String[] params = new String[]{facilityName, days};
        Request r = new Request(clientIp, Request.QUERY, (ArrayList<String>) Arrays.asList(params));
        String error = doOperation(r, false, payloads -> handleQueryAvailabilityResult(payloads));
        if (error != null) { return error; }
        return null;
    }

    /**
     *
     * @param freeSlots
     */
    private void handleQueryAvailabilityResult(ArrayList<String> freeSlots) {
        System.out.print("Free Slot:");
        for (String s : freeSlots) {
            String[] slots  = s.split(" ");
            System.out.printf("%s: from %s to %s\n", slots[0], Encoder.fromStringToTime(slots[1]), Encoder.fromStringToTime(slots[2]));
        }
    }

    /**
     * Return the error when booking for a facility with time slot (start, end) (inclusive). Print the confirmation id and
     * return null if no error.
     * @param facilityName the facility name to be booked with slot start and end
     * @param start the start time of booking slot
     * @param end the end time of booking slot
     * @return the string error of booking operation with given input.
     */
    public String bookFacility(String facilityName, String start, String end) {
        String startFormatted  = formatDateTimeInput(start);
        String endFormatted  = formatDateTimeInput(end);
        String[] params = {facilityName, startFormatted, endFormatted};
        Request r = new Request(clientIp, Request.BOOK, (ArrayList<String>) Arrays.asList(params));
        String error = doOperation(r, false, payloads -> {
            handleBookingResult(payloads);
        });
        if (error != null) { return error; }
        return null;
    }

    /**
     *
     * @param payloads
     */
    private void handleBookingResult(ArrayList<String> payloads) {
        String confirmationId = payloads.get(0);
        System.out.printf("Booking confirmed, id: %s\n", confirmationId);
    }

    /**
     * 
     * @param confirmationID
     * @param editMode
     * @param minute
     * @return
     */
    public String editBooking(String confirmationID, String editMode, String minute) {
    	String[] params = new String[]{confirmationID, editMode, minute};
        Request r = new Request(clientIp, Request.EDIT, (ArrayList<String>) Arrays.asList(params));
        String error = doOperation(r, false, payloads -> {
            handleEditBookingResult(payloads);
        });
        if (error != null) { return error; }
        return null;
    }

    /**
     *
     * @param payloads
     */
    private void handleEditBookingResult(ArrayList<String> payloads) {
        String confirmationId = payloads.get(0);
        String ack = payloads.get(1);
        System.out.printf("Edit booking %s success\n", confirmationId);
    }
    
    /**
     * 
     * @param facilityName
     * @param endDateTime
     * @return
     */
    public String monitorFacility(String facilityName, String endDateTime) {
    	String endFormatted  = formatDateTimeInput(endDateTime);
    	String[] params = new String[]{facilityName, endFormatted, clientIp};
        Request r = new Request(clientIp, Request.MONITOR, (ArrayList<String>) Arrays.asList(params));
        String error = doOperation(r, true, payloads -> {
            handleMonitorFacilityResult(payloads);
        });
        if (error != null) { return error; }
        return null;
    }

    private void handleMonitorFacilityResult(ArrayList<String> payloads) {
    	SimpleDateFormat format = new SimpleDateFormat("E h:m");
    	String time = format.format(Calendar.getInstance().getTime());
        System.out.printf("[%s] Activity on facility %s: %s", time, payloads.get(0), payloads.get(1));
    }

    /**
     * 
     * @param confirmationId
     * @return
     */
    public String cancelBooking(String confirmationId) {
    	String[] params = new String[]{confirmationId};
    	Request r = new Request(clientIp, Request.CANCEL, (ArrayList<String>) Arrays.asList(params));
    	String error = doOperation(r, false, payloads -> {
    		handleCancelBookingResult(payloads);
    	});
    	if (error != null) { return error; }
    	return null;
    }
    
    /**
     * 
     * @param payloads
     */
    private void handleCancelBookingResult(ArrayList<String> payloads) {
    	System.out.printf("Cancel booking %s success\n", payloads.get(0));
    }
    
    /**
     * 
     * @param date
     * @return
     */
    public String getAllAvailableFacilities(String date) {
    	String[] params = new String[]{date};
    	Request r = new Request(clientIp, Request.GET_ALL, (ArrayList<String>) Arrays.asList(params));
    	String error = doOperation(r, false, payloads -> {
    		payloads.add(0, date);
    		handleGetAllAvailableFacilities(payloads);
    	});
    	if (error != null) { return error; }
    	return null;
    }
    
    /**
     * 
     * @param payloads
     */
    private void handleGetAllAvailableFacilities(ArrayList<String> payloads) {
    	int date = Integer.parseInt(payloads.get(0));
    	System.out.printf("All available facilities in %s:\n", DayOfWeek.valueOf(date));
    	for (int i = 1; i < payloads.size() - 1; ++i) {
    		String[] payloadSplit = payloads.get(i).split(" ");
    		System.out.printf("Facility %s, available slots: %s", payloadSplit[0], payloadSplit[1]);
    	}
    }

    /**
     * Format the DateTime input from command line and return the string value of it.
     * @param time the time in d:hh:mm format
     * @return the formatted DateTime string
     */
    private String formatDateTimeInput(String time) {
        String[] timeSplit = time.split(":");
        int day = Integer.parseInt(timeSplit[0]);
        int hour = Integer.parseInt(timeSplit[1]);
        int minute = Integer.parseInt(timeSplit[2]);
        Time tTime = Time.getTime(hour, minute);
        DateTime dt = DateTime.getDateTime(DayOfWeek.valueOf(day), tTime.getTotalMinutes());
        return Encoder.fromDateTimeToString(dt);
    }
    
    /**
     * Create a socket and send request to remote server, then wait to receive the reply and unmarshal it to get the payloads.
     * Return an OperationResult with two field error and payloads. If error is not null, payloads is null.
     * @param request the request to be send to sever
     * @param multipleReply the option to wait for multiple reply from server
     * @param f the callback function to handle replies from server
     * @return
     */
    private String doOperation(Request request, boolean multipleReply, ICallback callback) {
    	String error;
    	clientSocket.sendRequest(request);
        if (clientSocket.error() != null) {
            return clientSocket.error();
        }
        long interval = 30;
        
		if (multipleReply) {
			interval = getMonitorInterval(request.getPayloads().get(1));
		}
		Receiver receiver = new Receiver(request, clientSocket, multipleReply, callback);
		Future<String> future = scheduler.submit(receiver);
		try {
			error = future.get(interval * 60 + 4, TimeUnit.SECONDS);
		} catch (Exception e) {
			error = e.getMessage();
		}
        if (error != null) { return error; }
        return null;
    }
    
    /**
     * 
     * @param end
     * @return
     */
    private static long getMonitorInterval(String end) {
    	DateTime endDateTime = Encoder.fromStringToDateTime(end);
		DateTime now = DateTime.now();
		return endDateTime.minutesFrom(now);
    }
}

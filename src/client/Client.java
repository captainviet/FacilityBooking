package client;

import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import shared.DateTime;
import shared.DayOfWeek;
import shared.Encoder;
import shared.FreeSlot;
import shared.ICallback;
import shared.Request;
import shared.Time;

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
        //    	String[] params = new String[]{facilityName, days};
        ArrayList<String> params = new ArrayList<>();
        params.add(facilityName);
        params.add(days);
        //        Request r = new Request(clientIp, Request.QUERY, (ArrayList<String>) Arrays.asList(params));
        Request r = Request.constructRequest(clientIp, Request.QUERY, params);
        String error = doOperation(r, false, payloads -> handleQueryAvailabilityResult(payloads));
        if (error != null) {
            return error;
        }
        return null;
    }

    /**
     *
     * @param freeSlotsInDays
     */
    private void handleQueryAvailabilityResult(ArrayList<String> freeSlotsInDays) {
        System.out.print("Free Slot:\n");
        for (String freeSlotsInDay : freeSlotsInDays) {
            String[] s = freeSlotsInDay.split("|");
            DayOfWeek day = DayOfWeek.valueOf(Integer.parseInt(s[0]));
            for (int i = 1; i < s.length; ++i) {
                FreeSlot freeSlot = Encoder.fromStringToFreeSlot(s[i]);
                System.out.printf("%s: from %s to %s\n", day, freeSlot.getStart().toString(),
                        freeSlot.getEnd().toString());
            }
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
        String startFormatted = formatDateTimeInput(start);
        String endFormatted = formatDateTimeInput(end);
        //        String[] params = {facilityName, startFormatted, endFormatted};
        ArrayList<String> params = new ArrayList<>();
        params.add(facilityName);
        params.add(startFormatted);
        params.add(endFormatted);
        //        Request r = new Request(clientIp, Request.BOOK, (ArrayList<String>) Arrays.asList(params));
        Request r = Request.constructRequest(clientIp, Request.BOOK, params);
        String error = doOperation(r, false, payloads -> {
            handleBookingResult(payloads);
        });
        if (error != null) {
            return error;
        }
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
     * @param timeOffset
     * @return
     */
    public String editBooking(String confirmationID, String editMode, String timeOffset) {
        String formattedTimeOffset = formatTimeInput(timeOffset);
        //    	String[] params = new String[]{confirmationID, editMode, formattedTimeOffset};
        ArrayList<String> params = new ArrayList<>();
        params.add(confirmationID);
        params.add(editMode);
        params.add(formattedTimeOffset);
        //        Request r = new Request(clientIp, Request.EDIT, (ArrayList<String>) Arrays.asList(params));
        Request r = Request.constructRequest(clientIp, Request.EDIT, params);
        String error = doOperation(r, false, payloads -> {
            handleEditBookingResult(payloads);
        });
        if (error != null) {
            return error;
        }
        return null;
    }

    /**
     *
     * @param payloads
     */
    private void handleEditBookingResult(ArrayList<String> payloads) {
        String confirmationId = payloads.get(0);
        System.out.printf("Edit booking %s success\n", confirmationId);
    }

    /**
     * 
     * @param facilityName
     * @param endDateTime
     * @return
     */
    public String monitorFacility(String facilityName, String endDateTime) {
        String endFormatted = formatDateTimeInput(endDateTime);
        //    	String[] params = new String[]{facilityName, endFormatted, clientIp};
        ArrayList<String> params = new ArrayList<>();
        params.add(facilityName);
        params.add(endFormatted);
        params.add(clientIp);
        //        Request r = new Request(clientIp, Request.MONITOR, (ArrayList<String>) Arrays.asList(params));
        Request r = Request.constructRequest(clientIp, Request.MONITOR, params);
        String error = doOperation(r, true, payloads -> {
            payloads.add(0, facilityName);
            handleMonitorFacilityResult(payloads);
        });
        if (error != null) {
            return error;
        }
        return null;
    }

    private void handleMonitorFacilityResult(ArrayList<String> payloads) {
        System.out.printf("[%s] Available on facility %s overweek:\n", currentFormatTime(), payloads.get(0));
        payloads.remove(0);
        for (String freeSlotsInDay : payloads) {
            String[] s = freeSlotsInDay.split("|");
            DayOfWeek day = DayOfWeek.valueOf(Integer.parseInt(s[0]));
            for (int i = 1; i < s.length; ++i) {
                FreeSlot freeSlot = Encoder.fromStringToFreeSlot(s[i]);
                System.out.printf("%s: from %s to %s\n", day, freeSlot.getStart().toString(),
                        freeSlot.getEnd().toString());
            }
        }
    }

    /**
     * 
     * @param confirmationId
     * @return
     */
    public String cancelBooking(String confirmationId) {
        //        String[] params = new String[]{confirmationId};
        ArrayList<String> params = new ArrayList<>();
        params.add(confirmationId);
        Request r = Request.constructRequest(clientIp, Request.CANCEL, params);
        String error = doOperation(r, false, payloads -> {
            handleCancelBookingResult(payloads);
        });
        if (error != null) {
            return error;
        }
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
    public String getAllAvailableFacilitiesInTimeRange(String date, String startTime, String endTime) {
        String formattedStartTime = formatTimeInput(startTime);
        String formattedEndTime = formatTimeInput(endTime);
        //    	String[] params = new String[]{date, formattedStartTime, formattedEndTime};
        ArrayList<String> params = new ArrayList<>();
        params.add(date);
        params.add(formattedStartTime);
        params.add(formattedEndTime);
        //        Request r = new Request(clientIp, Request.GET_ALL, (ArrayList<String>) Arrays.asList(params));
        Request r = Request.constructRequest(clientIp, Request.GET_ALL, params);
        String error = doOperation(r, false, payloads -> {
            payloads.add(0, date);
            handleGetAllAvailableFacilities(payloads);
        });
        if (error != null) {
            return error;
        }
        return null;
    }

    /**
     * 
     * @param payloads
     */
    private void handleGetAllAvailableFacilities(ArrayList<String> payloads) {
        int date = Integer.parseInt(payloads.remove(0));
        System.out.printf("All available facilities in %s:\n", DayOfWeek.valueOf(date));
        for (String freeSlotsByFacility : payloads) {
            String[] s = freeSlotsByFacility.split("|");
            String facilityName = s[0];
            for (int i = 1; i < s.length; ++i) {
                FreeSlot freeSlot = Encoder.fromStringToFreeSlot(s[i]);
                System.out.printf("Facility %s: from %s to %s\n", facilityName, freeSlot.getStart().toString(),
                        freeSlot.getEnd().toString());
            }
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
     * 
     * @param time
     * @return
     */
    private String formatTimeInput(String time) {
        String[] timeSplit = time.split(":");
        int hour = Integer.parseInt(timeSplit[0]);
        int minute = Integer.parseInt(timeSplit[1]);
        Time tTime = Time.getTime(hour, minute);
        return Encoder.fromTimeToString(tTime);
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
        ReplyReceiver receiver = new ReplyReceiver(request, clientSocket, multipleReply, callback);
        Future<String> future = scheduler.submit(receiver);
        try {
            error = future.get(interval * 60 + 4, TimeUnit.SECONDS);
        } catch (Exception e) {
            error = e.getMessage();
        }
        if (error != null) {
            return error;
        }
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

    private static String currentFormatTime() {
        SimpleDateFormat format = new SimpleDateFormat("E h:m");
        return format.format(Calendar.getInstance().getTime());
    }
}

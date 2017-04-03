package client;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import shared.Constant;
import shared.DateTime;
import shared.DayOfWeek;
import shared.Encoder;
import shared.FreeSlot;
import shared.ICallback;
import shared.Request;
import shared.Time;
import shared.Utils;

/**
 * 
 * @author Tran Vu Xuan Nhat
 *
 */
public class Client {
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
     * Query the availability of a facility named facilityName over selection of one or multiple days. 
     * Return a null String if no error, else return a string error message.
     * Success reply’s payloads are handled by handleQueryAvailabilityResult.
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
        Request r = Request.constructRequest(Request.QUERY, params);
        String error = doOperation(r, false, payloads -> handleQueryAvailabilityResult(payloads));
        if (error != null) {
            return error;
        }
        return null;
    }

    /**
     * Handle the successful reply’s payloads of queryAvailability function.
     * @param freeSlotsInDays
     */
    private void handleQueryAvailabilityResult(ArrayList<String> freeSlotsInDays) {
        System.out.print("Free Slot:\n");
        for (String freeSlotsInDay : freeSlotsInDays) {
            String[] s = freeSlotsInDay.split("\\|");
            DayOfWeek day = DayOfWeek.valueOf(Integer.parseInt(s[0]));
            for (int i = 1; i < s.length; ++i) {
                FreeSlot freeSlot = Encoder.fromStringToFreeSlot(s[i]);
                System.out.printf("%s: from %s to %s\n", day, freeSlot.getStart().toString(),
                        freeSlot.getEnd().toString());
            }
        }
    }

    /**
     * Book a facility named facilityNamed with start DateTime and end DateTime. 
     * Return a null String if no error, else return a string error message. 
     * Success reply’s payloads are handled by handleBookingResult.
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
        Request r = Request.constructRequest(Request.BOOK, params);
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
     * Edit a booking with confirmationId, advance (editMode=0, at most 1 hour), or postpone (editMode=1, at most 30 minutes), 
     * with timeOffset minutes. 
     * Return a null String if no error, else return a string error message. 
     * Success reply’s payloads are handled by handleEditBookingResult.
     * @param confirmationID
     * @param editMode
     * @param timeOffset
     * @return
     */
    public String editBooking(String confirmationId, String editMode, String timeOffset) {
        //    	String[] params = new String[]{confirmationID, editMode, formattedTimeOffset};
        ArrayList<String> params = new ArrayList<>();
        params.add(confirmationId);
        params.add(editMode);
        params.add(timeOffset);
        //        Request r = new Request(clientIp, Request.EDIT, (ArrayList<String>) Arrays.asList(params));
        Request r = Request.constructRequest(Request.EDIT, params);
        String error = doOperation(r, false, payloads -> {
            handleEditBookingResult(payloads);
        });
        if (error != null) {
            return error;
        }
        return null;
    }

    /**
     * Handle the successful reply’s payloads of editBooking function, which contains confirmationId.
     * @param payloads
     */
    private void handleEditBookingResult(ArrayList<String> payloads) {
        String confirmationId = payloads.get(0);
        System.out.printf("Edit booking %s success\n", confirmationId);
    }

    /**
     * Monitor a facility named facilityName until endDateTime. 
     * Return a null String if no error, else return a string error message. 
     * Success reply’s payloads are handled by handleMonitorFacilityResult.
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
        Request r = Request.constructRequest(Request.MONITOR, params);
        String error = doOperation(r, true, payloads -> {
            payloads.add(0, facilityName);
            handleMonitorFacilityResult(payloads);
        });
        if (error != null) {
            return error;
        }
        return null;
    }

    /** 
     * Handle the successful reply’s payloads of monitorFacility function, which may contain start_monitor message 
     * or stop_monitor message or availability of a facility over week.
     * @param payloads
     */
    private void handleMonitorFacilityResult(ArrayList<String> payloads) {
        if (payloads.get(1).equals(Constant.START_MONITOR)) {
            System.out.printf("[%s] Start monitoring facility %s\n", Utils.currentLogFormatTime(), payloads.get(0));
            return;
        }
        if (payloads.get(1).equals(Constant.STOP_MONITOR)) {
            System.out.printf("[%s] Stop monitoring facility %s\n", Utils.currentLogFormatTime(), payloads.get(0));
            return;
        }
        System.out.printf("[%s] Available slots on facility %s overweek:\n", Utils.currentLogFormatTime(),
                payloads.get(0));
        payloads.remove(0);
        for (String freeSlotsInDay : payloads) {
            String[] s = freeSlotsInDay.split("\\|");
            DayOfWeek day = DayOfWeek.valueOf(Integer.parseInt(s[0]));
            for (int i = 1; i < s.length; ++i) {
                FreeSlot freeSlot = Encoder.fromStringToFreeSlot(s[i]);
                System.out.printf("%s: from %s to %s\n", day, freeSlot.getStart().toString(),
                        freeSlot.getEnd().toString());
            }
        }
    }

    /**
     * Cancel the booking with confirmation id confirmationId. 
     * Return a null String if no error, else return a string error message. 
     * Success reply’s payloads are handled by handleCancelBookingResult.
     * @param confirmationId
     * @return
     */
    public String cancelBooking(String confirmationId) {
        //        String[] params = new String[]{confirmationId};
        ArrayList<String> params = new ArrayList<>();
        params.add(confirmationId);
        Request r = Request.constructRequest(Request.CANCEL, params);
        String error = doOperation(r, false, payloads -> {
            handleCancelBookingResult(payloads);
        });
        if (error != null) {
            return error;
        }
        return null;
    }

    /**
     * Handle the successful reply’s payloads of cancelBooking function, which contains confirmationId.
     * @param payloads
     */
    private void handleCancelBookingResult(ArrayList<String> payloads) {
        System.out.printf("Cancel booking %s success\n", payloads.get(0));
    }

    /**
     * Get all available facilities over a time range [starTime, endTime] on a selected day. 
     * Return a null String if no error, else return a string error message. 
     * Success reply’s payloads are handled by handleGetAllAvailableFacilities.
     * @param day
     * @return
     */
    public String getAllAvailableFacilitiesInTimeRange(String day, String startTime, String endTime) {
        String formattedStartTime = formatTimeInput(startTime);
        String formattedEndTime = formatTimeInput(endTime);
        //    	String[] params = new String[]{date, formattedStartTime, formattedEndTime};
        ArrayList<String> params = new ArrayList<>();
        params.add(day);
        params.add(formattedStartTime);
        params.add(formattedEndTime);
        //        Request r = new Request(clientIp, Request.GET_ALL, (ArrayList<String>) Arrays.asList(params));
        Request r = Request.constructRequest(Request.GET_ALL, params);
        String error = doOperation(r, false, payloads -> {
            payloads.add(0, day);
            handleGetAllAvailableFacilities(payloads);
        });
        if (error != null) {
            return error;
        }
        return null;
    }

    /**
     * Handle successful reply of getAllAvailableFacilitiesInTimeRange function, which contains 
     * availability of all facilities in the desired time range and day.
     * @param payloads
     */
    private void handleGetAllAvailableFacilities(ArrayList<String> payloads) {
        int day = Integer.parseInt(payloads.remove(0));
        System.out.printf("All available facilities in %s:\n", DayOfWeek.valueOf(day));
        for (String freeSlotsByFacility : payloads) {
            String[] s = freeSlotsByFacility.split("\\|");
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
     * Create a socket and end request to server and wait for reply. Reply can be multiple reply 
     * from server for monitor request or a single reply for other requests. 
     * Return a null String if no error sending request and receiving reply, else return 
     * a string error message. Payloads of successful reply are handled via callback.
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
        } else {

        }
        ReplyReceiver receiver = new ReplyReceiver(request, clientSocket, multipleReply, callback);
        Future<String> future = scheduler.submit(receiver);
        try {
            if (multipleReply) {
                error = future.get(interval * 60 + 4, TimeUnit.SECONDS);
            } else {
                error = future.get();
            }
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

}

package client;

import shared.Encoder;
import shared.ICallback;
import shared.Time;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * TODO: Describe purpose and behavior of ClientMonitor
 */
public class Client {
    private static HashMap<String, String> bookingCache = new HashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final String INSTRUCTION = "Input your operation or h for help:\n";
    private static final String HELP = "LIST OF OPERATIONS (day is from 0 to 6 (Monday to Sunday), Format of time is hh:mm) \n";
    private static final String QUERY_AVAILABLE = "Query availability operation: q facility_name day1,day2,...,dayN (N <= 6)\n";
    private static final String BOOKING = "Book a facility: b facility_name start end (format of start, end, is day:time)\n";
    private static final String EDIT_BOOKING = "Edit a booking: e confirmation_id edit_mode time (edit_mode 0: advance, 1: postpone)\n";
    private static final String MONITORING = "Monitoring a facility: m facility_name end (end is day:time)\n";
    private static final String QUIT_PROGRAM = "Quit client: Q (or Ctrl + C)\n";
    private static final String QUERY = "q";
    private static final String BOOK = "b";
    private static final String EDIT = "e";
    private static final String MONITOR = "m";
    private static final String GET_ALL = "a";
    private static final String CANCEL = "c";
    private static final String QUIT = "Q";
    private static final String INVALID = "INVALID";
    public static final String STOP_MONITOR = "stop";
    private static String clientIp;
    private static InetAddress serverHost;
    private static int serverPort;
    public static void main(String[] args) throws Exception{
        serverHost = InetAddress.getByName(args[0]);
        serverPort = Integer.parseInt(args[1]);
        clientIp = InetAddress.getLocalHost().getHostName();
        String[] operationCmd;
        String error;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print(INSTRUCTION);
            operationCmd = br.readLine().split(" ");
            if (operationCmd[0].equals("h")) {
                printHelp();
                continue;
            } else {
                switch (operationCmd[0]) {
                    case QUERY:
                        error = queryAvailability(operationCmd[1], operationCmd[2].replace(",",""));
                        if (error != null) {
                            printError(QUERY, error);
                        }
                        break;
                    case BOOK:
                        error = bookFacility(operationCmd[1], operationCmd[2].replace(":", " "), operationCmd[3].replace(":", " "));
                        if (error != null) {
                            printError(BOOK, error);
                        }
                        break;
                    case EDIT:
                        error = editBooking(operationCmd[1], operationCmd[2], toMinutes(operationCmd[3]));
                        if (error != null) {
                            printError(EDIT, error);
                        }
                        break;
                    case MONITOR:
                        error = monitorFacility(operationCmd[1], operationCmd[2]);
                    case QUIT:
                    default:
                        printError(INVALID,"Invalid operation");
                }
            }
        }
    }


    /**
     *  Print the help message to screen. The help message show format of operation input
     */
    private static void printHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append(HELP).append(QUERY_AVAILABLE).append(BOOKING).append(EDIT_BOOKING).append(MONITORING).append(QUIT_PROGRAM);
        System.out.print(sb.toString());
    }

    /**
     * Print the error message in format to screen.
     * @param operation the operation that causes error
     * @param msg the error message generated due to failed operation
     */
    private static void printError(String operation,String msg) {
        System.out.printf("Operation: %s, Error: %s\n", operation, msg);
    }

    /**
     * Return the error when querying for available slots of a facility in one or multiple days. Print all slots and return null if no error.
     * @param facilityName the facility name to be queried for available slot in days.
     * @param days the list of day to be queried for available slot of the facility with name facilityName.
     * @return the string error of query operation
     */
    private static String queryAvailability(String facilityName, String days) {
        Request r = new Request(clientIp, QUERY, (ArrayList) Arrays.asList(new String[]{facilityName, days}), serverHost, serverPort);
        String error = doOperation(r, payloads -> handleQueryAvailableResult(payloads));
        if  (error != null) {
            return error;
        }
        return null;
    }

    /**
     *
     * @param freeSlots
     */
    private static void handleQueryAvailableResult(ArrayList<String> freeSlots) {
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
    private static String bookFacility(String facilityName, String start, String end) {
        String startFormatted  = formatDateTimeInput(start);
        String endFormatted  = formatDateTimeInput(end);
        String[] params = {facilityName, startFormatted, endFormatted};
        Request r = new Request(clientIp, BOOK, (ArrayList) Arrays.asList(params), serverHost, serverPort);
        String error = doOperation(r, payloads -> {
            payloads.add(start);
            payloads.add(end);
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
    private static void handleBookingResult(ArrayList<String> payloads) {
        String confirmationId = payloads.get(0);
//        bookingCache.put(confirmationId, payloads.get(1) + " " + payloads.get(2));
        System.out.printf("Booking confirmed, id: %s\n", confirmationId);
    }

    public static String editBooking(String confirmationID, String editMode, String minute) {
        Request r = new Request(clientIp, EDIT, (ArrayList) Arrays.asList(new String[]{confirmationID, editMode, minute}), serverHost, serverPort);
        String error = doOperation(r, payloads -> {
            payloads.add(0, confirmationID);

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
    private static void handleEditBookingResult(ArrayList<String> payloads) {
        String confirmationId = payloads.get(0);
        String ack = payloads.get(1);
        System.out.printf("Edit booking %s, result: %s\n", confirmationId, ack);
    }
    private static String monitorFacility(String facilityName, String endDateTime) {
        Request r = new Request(clientIp, EDIT, (ArrayList) Arrays.asList(new String[]{facilityName, endDateTime}), serverHost, serverPort);
        String error = doOperation(r, payloads -> {
            payloads.add(0, facilityName);
            handleMonitorFacilityResult(payloads);
        });
        return null;
    }

    private static void handleMonitorFacilityResult(ArrayList<String> payloads) {
        System.out.printf("[%s] Activity on facility %s: %s", payloads.get(0), payloads.get(1));
    }



//    public ArrayList<DayOfWeek> parseDays(String s) {
//        String[] dayArr = s.split(",");-
//        ArrayList<DayOfWeek> days = new ArrayList<>();
//        for (String d: dayArr) {
//            days.add(DayOfWeek.valueOf(Integer.parseInt(d)));
//        }
//        return days;
//    }

    /**
     * Return the time in minutes from the beginning of day.
     * @param time the time in hh:mm format
     * @return the time in number of minutes
     */
    public static String toMinutes(String time) {
        String[] timeSplit = time.split(":");
        return String.valueOf(Integer.parseInt(timeSplit[0]) * 60 + Integer.parseInt(timeSplit[1]));
    }

    /**
     * Format the DateTime input from command line and return the string value of it.
     * @param time the time in d:hh:mm format
     * @return the formatted DateTime string
     */
    private static String formatDateTimeInput(String time) {
        String[] timeSplit = time.split(":");
        return timeSplit[0] + " " + Encoder.fromTimeToString(Time.getTime(Integer.parseInt(timeSplit[0]), Integer.parseInt(timeSplit[1])));
    }
    /**
     * Create a socket and send request to remote server, then wait to receive the reply and unmarshal it to get the payloads.
     * Return an OperationResult with two field error and payloads. If error is not null, payloads is null.
     * @param request
     * @param f
     * @return
     */
    public static String doOperation(Request request, ICallback f) {
        ClientSocket clientSocket = new ClientSocket();
        clientSocket.sendRequest(request);
        if (clientSocket.hasError) {
            return clientSocket.error();
        }
        boolean isMonitorRequest = request.getType() != MONITOR;

        while(true) {
            Reply reply = clientSocket.receiveReply();
            if (clientSocket.hasError) {
                clientSocket.close();
                return clientSocket.error();
            }
            reply.Unmarshal();
            if (reply.statusCode == reply.ERROR_REPLY_CODE) {
                clientSocket.close();
                return clientSocket.error();
            } else {
                if (reply.getPayloads().get(0) == STOP_MONITOR) {
                    break;
                }
                f.handle(reply.getPayloads());
                if (!isMonitorRequest) {
                    break;
                }
            }
        }
        clientSocket.close();
        return null;
    }

}

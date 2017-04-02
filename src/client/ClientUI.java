package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;

import shared.ErrorCode;
import shared.Request;

public class ClientUI {
    private static final String INSTRUCTION = "Input your operation or h for help:\n";
    private static final String HELP_INTRO = "LIST OF OPERATIONS (day is from 0 to 6 (Monday to Sunday), Format of time is hh:mm) \n";
    private static final String QUERY_AVAILABLE = "Query availability operation: q facility_name day1,day2,...,dayN (N <= 6)\n";
    private static final String BOOKING = "Book a facility: b facility_name start end (format of start, end, is day:time)\n";
    private static final String EDIT_BOOKING = "Edit a booking: e confirmation_id edit_mode time (edit_mode 0: advance, 1: postpone)\n";
    private static final String MONITORING = "Monitoring a facility: m facility_name end (end is day:time)\n";
    private static final String GET_ALL_AVAILABLE_IN_TIME_RANGE = "Get all available facilities in time range: g day time_start time_end\n";
    private static final String CANCEL_BOOKING = "Cancel a booking: c confirmation_id\n";
    private static final String QUIT_PROGRAM = "Quit client: Q (or Ctrl + C)\n";
    private static InetAddress serverHost;
    private static int serverPort;

    public static void main(String[] args) throws Exception {
        serverHost = InetAddress.getByName(args[0]);
        serverPort = Integer.parseInt(args[1]);
        Client client = new Client(serverHost, serverPort);
        try {
            client.start();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String[] operationCmd;
        String error;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print(INSTRUCTION);
            operationCmd = br.readLine().split(" ");
            if (operationCmd[0].equals(Request.HELP)) {
                printHelp();
                continue;
            } else {
                switch (operationCmd[0]) {
                case Request.QUERY:
                    error = client.queryAvailability(operationCmd[1], operationCmd[2]);
                    if (error != null) {
                        printError(Request.QUERY, error);
                    }
                    break;
                case Request.BOOK:
                    error = client.bookFacility(operationCmd[1], operationCmd[2], operationCmd[3]);
                    if (error != null) {
                        printError(Request.BOOK, error);
                    }
                    break;
                case Request.EDIT:
                    error = client.editBooking(operationCmd[1], operationCmd[2], operationCmd[3]);
                    if (error != null) {
                        printError(Request.EDIT, error);
                    }
                    break;
                case Request.MONITOR:
                    error = client.monitorFacility(operationCmd[1], operationCmd[2]);
                    if (error != null) {
                        printError(Request.MONITOR, error);
                    }
                    break;
                case Request.CANCEL:
                    error = client.cancelBooking(operationCmd[1]);
                    if (error != null) {
                        printError(Request.CANCEL, error);
                    }
                    break;
                case Request.GET_ALL:
                    error = client.getAllAvailableFacilitiesInTimeRange(operationCmd[1], operationCmd[2],
                            operationCmd[3]);
                    if (error != null) {
                        printError(Request.CANCEL, error);
                    }
                    break;
                case Request.QUIT:
                    return;
                default:
                    printError(Request.INVALID, "Invalid operation");
                }
            }
        }
    }

    /**
     *  Print the help message to screen. The help message show format of operation input
     */
    private static void printHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append(HELP_INTRO).append(QUERY_AVAILABLE).append(BOOKING).append(EDIT_BOOKING).append(MONITORING)
                .append(GET_ALL_AVAILABLE_IN_TIME_RANGE).append(CANCEL_BOOKING).append(QUIT_PROGRAM);
        System.out.print(sb.toString());
    }

    /**
     * Print the error message in format to screen.
     * @param operation the operation that causes error
     * @param msg the error message generated due to failed operation
     */
    private static void printError(String operation, String msg) {
        System.out.printf("Operation: %s, Error: %s\n", operation, ErrorCode.getError(msg));
    }

}

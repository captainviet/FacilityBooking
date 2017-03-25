package client;

import server.DateTime;
import server.DayOfWeek;
import server.Time;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * TODO: Describe purpose and behavior of ClientMonitor
 */
public class Client {
    private static final String INSTRUCTION = "Input your operation or h for help:\n";
    private static final String HELP = "LIST OF OPERATIONS (day is from 0 to 6 (Monday to Sunday), Format of time is hh:mm) \n";
    private static final String QUERY_AVAILABLE = "Query availability operation: q facility_name day1,day2,...,dayN (N <= 6)\n";
    private static final String BOOKING = "Book a facility: b facility_name start end (format of start, end, is day:time)\n";
    private static final String EDIT_BOOKING = "Edit a booking: e confirmation_id edit_mode time (edit_mode is 0: advanced, 1: postpone)\n";
    private static final String MONITORING = "Monitoring a facility: m facility_name interval (interval is number of hours)\n";
    private static final String QUIT_PROGRAM = "Quit client: Q (or Ctrl + C)\n";
    private static final String QUERY = "q";
    private static final String BOOK = "b";
    private static final String EDIT = "e";
    private static final String MONITOR= "m";
    private static final String QUIT = "Q";
    private static String clientIp;
    private static InetAddress serverHost;
    private static int serverPort;
    public static void main(String[] args) throws Exception{
        serverHost = InetAddress.getByName("localhost");
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
                            printError(error);
                        }
                    case BOOK:
                    case EDIT:
                    case MONITOR:
                    case QUIT:
                    default:
                        printError("Wrong operation");
                        continue;
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
     * @param msg
     */
    private static void printError(String msg) {
        System.out.printf("Error: %s\n", msg);
    }

    /**
     * Return the error when query for available slot of a facility in one or multiple days. Return null if no error.
     * @param facilityName the facility name to be queried for available slot in days.
     * @param days the list of day to be queried for available slot of the facility with name facilityName.
     */
    public static String queryAvailability(String facilityName, String days) {
        Request r = new Request(clientIp, QUERY, (ArrayList) Arrays.asList(new String[]{facilityName, days}), serverHost, serverPort);
        OperationResult or = sendRequest(r);
        if  (or.error != null) {
            return or.error;
        }
        return null;
    }

    public void bookFacility(String name, DateTime start, DateTime end) {

    }

    public void editBooking(long confirmationID, Time minute) {

    }

    public void monitoring(String name, DateTime start, DateTime end) {

    }

    public ArrayList<DayOfWeek> parseDays(String s) {
        String[] dayArr = s.split(",");
        ArrayList<DayOfWeek> days = new ArrayList<>();
        for (String d: dayArr) {
            days.add(DayOfWeek.valueOf(Integer.parseInt(d)));
        }
        return days;
    }

    public static OperationResult sendRequest(Request request) {
        ClientSocket clientSocket = new ClientSocket();
        clientSocket.sendRequest(request);
        if (clientSocket.hasError) {
            return new OperationResult(clientSocket.error(), null);
        }
        Reply reply = clientSocket.receiveReply(true);
        if (clientSocket.hasError) {
            return new OperationResult(clientSocket.error(), null);
        }
        reply.Unmarshal();
        if (reply.statusCode == reply.ERROR_REPLY_CODE) {
            return new OperationResult(reply.getError(), null);
        } else {
            return new OperationResult(null, reply.getPayloads());
        }
    }

    private static class OperationResult {
        public String error;
        public ArrayList<String> payloads;
        public OperationResult(String error, ArrayList<String> payloads) {
            this.error = error;
            this.payloads = payloads;
        }
    }
}

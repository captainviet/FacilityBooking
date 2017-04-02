package server;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import shared.Constant;
import shared.DateTime;
import shared.DayOfWeek;
import shared.Encoder;
import shared.FreeSlot;
import shared.Network;
import shared.Reply;
import shared.Request;
import shared.Time;
import shared.Utils;

/**
 * TODO: Describe purpose and behavior of Server
 */
public class Server {
    private final static String ERROR_INVALID_FACILITY_NAME = "Invalid facility name";
    private final static String ERROR_BOOKING_CLASHED = "Booking time clashed";
    private final static String ERROR_BOOKING_NOT_EXIST = "Booking does not exist";
    private final static long EXPIRED_DURATION_IN_MILLIS = 3600000;

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
    private HashMap<String, ReplyRecord> replyHistory = new HashMap<>();

    public Server(int mode, int serverPort) {
        this.mode = semanticsModevalueOf(mode);
        this.serverPort = serverPort;
    }

    public void start() throws SocketException {
        serverSocket = new ServerSocket(serverPort);
        serverHost = serverSocket.getHost();
        Facility.initialize();
        System.out.printf("Server listening on host: %s, port: %s\n", serverHost.getHostName(), serverPort);
    }

    public String receiveAndProcessRequest() {
        String error;
        byte[] data = serverSocket.receiveRequest();
        if (serverSocket.getError() != null) {
            return serverSocket.getError();
        } else {
            Request request = Request.unmarshal(data);
            String requestKey = serverSocket.getClientHost().getHostAddress() + '[' + request.getId() + ']'; 
            //filtering request if invocation semantic is at most once
            if (mode == SemanticsMode.AT_MOST_ONCE && replyHistory.containsKey(requestKey)) {
                ReplyRecord record = replyHistory.get(requestKey);
                error = sendReply(record.getReply());
            } else {
                error = handleRequest(request, requestKey);
            }
        }
        return error;
    }

    private String handleRequest(Request request, String requestKey) {
        switch (request.getType()) {
        case Request.QUERY:
            return processQueryAvailabilityRequest(request, requestKey);
        case Request.BOOK:
            return processBookingRequest(request, requestKey);
        case Request.EDIT:
            return processEditBookingRequest(request, requestKey);
        case Request.MONITOR:
            return processMonitorRequest(request, requestKey);
        case Request.CANCEL:
            return processCancelBookingRequest(request, requestKey);
        case Request.GET_ALL:
            return processGetAllAvailableInTimeRangeRequest(request, requestKey);
        default:
            return "Invalid_operation";
        }
    }

    private void logRequest(String requestId, String requestType, String clientIp, int clientPort) {
        System.out.printf("[%s] Successfully processed request, id: %s, type: %s, client %s:%d",
                Utils.currentLogFormatTime(), requestId, requestType, clientIp, clientPort);
    }

    private String processGetAllAvailableInTimeRangeRequest(Request request, String requestKey) {
        List<String> result = new ArrayList<>();
        boolean hasError = false;
        List<String> payloads = request.getPayloads();
        DayOfWeek day = DayOfWeek.valueOf(Integer.parseInt(payloads.get(0)));
        Time start = Encoder.fromStringToTime(payloads.get(1));
        Time end = Encoder.fromStringToTime(payloads.get(2));
        List<Facility> facilities = Facility.getAllFacility();
        for (Facility facility : facilities) {
            StringBuilder resultOneFacility = new StringBuilder();
            resultOneFacility.append(facility.getFacilityName()).append("|");
            List<FreeSlot> freeSlots = QueryService.getAvailableFacility(facility, day, start, end);
            for (int i = 0; i < freeSlots.size(); i++) {
                resultOneFacility.append(freeSlots.get(i).toString());
                if (i != freeSlots.size() - 1) {
                    resultOneFacility.append("|");
                }
            }
            result.add(resultOneFacility.toString());
        }

        Reply reply = Reply.constructReply(hasError, result);
        String error = sendReply(reply);
        if (error != null) {
            return error;
        }
        if (mode == SemanticsMode.AT_MOST_ONCE) {
            saveReplyForRequest(reply, requestKey);
        }
        logRequest(request.getId(), request.getType(), serverSocket.getClientHost().getHostAddress(),
                serverSocket.getClientPort());
        return null;

    }

    private String processCancelBookingRequest(Request request, String requestKey) {
        List<String> result = new ArrayList<>();
        boolean hasError = false;
        List<String> payloads = request.getPayloads();
        int confirmationId = Integer.parseInt(payloads.get(0));
        String facilityName = null;
        if (!Booking.checkBookingExists(confirmationId)) {
            hasError = true;
            result.add(ERROR_BOOKING_NOT_EXIST);
        } else {
            facilityName = Booking.getFacilityBookedByID(confirmationId).getFacilityName();
            boolean canCancel = QueryService.cancelBookedConfirmation(confirmationId);
            if (!canCancel) {
                hasError = true;
                result.add(ERROR_BOOKING_CLASHED);
            } else {
                result.add(String.valueOf(confirmationId));
            }
        }
        Reply reply = Reply.constructReply(hasError, result);
        String error = sendReply(reply);
        if (error != null) {
            return error;
        }
        if (mode == SemanticsMode.AT_MOST_ONCE) {
            saveReplyForRequest(reply, requestKey);
        }
        logRequest(request.getId(), request.getType(), serverSocket.getClientHost().getHostAddress(),
                serverSocket.getClientPort());
        if (facilityName != null && !hasError) {
            MonitoringService.updateClients(facilityName, serverSocket);
        }
        return null;
    }

    private String processMonitorRequest(Request request, String requestKey) {
        List<String> result = new ArrayList<>();
        boolean hasError = false;
        List<String> payloads = request.getPayloads();
        String facilityName = payloads.get(0);
        Facility facility = Facility.getFacilityByName(facilityName);
        if (facility == null) {
            hasError = true;
            result.add(ERROR_INVALID_FACILITY_NAME);
        } else {
            DateTime endMonitorDateTime = Encoder.fromStringToDateTime(payloads.get(1));
            ClientMonitor clientMonitor = new ClientMonitor(serverSocket.getClientHost(), serverSocket.getClientPort(),
                    facilityName, endMonitorDateTime);
            if (!MonitoringService.checkClientMonitorExisted(clientMonitor)) {
                MonitoringService.registerClient(clientMonitor);
            }
        }
        Reply reply = Reply.constructReply(hasError, result);
        String error = sendReply(reply);
        if (error != null) {
            return error;
        }
        if (mode == SemanticsMode.AT_MOST_ONCE) {
            saveReplyForRequest(reply, requestKey);
        }
        logRequest(request.getId(), request.getType(), serverSocket.getClientHost().getHostAddress(),
                serverSocket.getClientPort());
        return null;
    }

    private String processEditBookingRequest(Request request, String requestKey) {
        List<String> result = new ArrayList<>();
        boolean hasError = false;
        List<String> payloads = request.getPayloads();
        int confirmationId = Integer.parseInt(payloads.get(0));
        int editMode = Integer.parseInt(payloads.get(1));
        int timeOffset = Integer.parseInt(payloads.get(2));
        timeOffset = editMode == 0 ? -timeOffset : timeOffset;
        String facilityName = null;
        if (!Booking.checkBookingExists(confirmationId)) {
            hasError = true;
            result.add(ERROR_BOOKING_NOT_EXIST);
        } else {
            facilityName = Booking.getFacilityBookedByID(confirmationId).getFacilityName();
            boolean canEdit = QueryService.editBookedConfirmation(confirmationId, timeOffset);
            if (!canEdit) {
                hasError = true;
                result.add(ERROR_BOOKING_CLASHED);
            } else {
                result.add(String.valueOf(confirmationId));
            }
        }
        Reply reply = Reply.constructReply(hasError, result);
        String error = sendReply(reply);
        if (error != null) {
            return error;
        }
        if (mode == SemanticsMode.AT_MOST_ONCE) {
            saveReplyForRequest(reply, requestKey);
        }
        logRequest(request.getId(), request.getType(), serverSocket.getClientHost().getHostAddress(),
                serverSocket.getClientPort());
        if (facilityName != null && !hasError) {
            MonitoringService.updateClients(facilityName, serverSocket);
        }
        return null;
    }

    private String processBookingRequest(Request request, String requestKey) {
        List<String> result = new ArrayList<>();
        boolean hasError = false;
        List<String> payloads = request.getPayloads();
        String facilityName = payloads.get(0);
        Facility facility = Facility.getFacilityByName(facilityName);
        if (facility == null) {
            hasError = true;
            result.add(ERROR_INVALID_FACILITY_NAME);
        } else {
            DateTime start = Encoder.fromStringToDateTime(payloads.get(1));
            DateTime end = Encoder.fromStringToDateTime(payloads.get(2));
            long confirmationId = QueryService.getConfirmationID(facility, start, end);
            if (confirmationId == -1) {
                hasError = true;
                result.add(ERROR_BOOKING_CLASHED);
            } else {
                result.add(String.valueOf(confirmationId));
            }
        }
        Reply reply = Reply.constructReply(hasError, result);
        String error = sendReply(reply);
        if (error != null) {
            return error;
        }
        if (mode == SemanticsMode.AT_MOST_ONCE) {
            saveReplyForRequest(reply, requestKey);
        }
        logRequest(request.getId(), request.getType(), serverSocket.getClientHost().getHostAddress(),
                serverSocket.getClientPort());
        if (!hasError) {
            MonitoringService.updateClients(facilityName, serverSocket);
        }
        return null;
    }

    private String processQueryAvailabilityRequest(Request request, String requestKey) {
        List<String> result = new ArrayList<>();
        boolean hasError = false;
        List<String> payloads = request.getPayloads();
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
                List<FreeSlot> freeSlots = QueryService.getAvailableFacility(facility, d);
                for (int i = 0; i < freeSlots.size(); i++) {
                    resultOneDay.append(Encoder.fromFreeSlotToString(freeSlots.get(i)));
                    if (i != freeSlots.size() - 1) {
                        resultOneDay.append("|");
                    }
                }
                result.add(resultOneDay.toString());
            }
        }
        if (Constant.DEBUG) {
            System.out.println(hasError);
            System.out.println(result);
        }
        Reply reply = Reply.constructReply(hasError, result);
        String error = sendReply(reply);
        if (error != null) {
            return error;
        }
        if (mode == SemanticsMode.AT_MOST_ONCE) {
            saveReplyForRequest(reply, requestKey);
        }
        logRequest(request.getId(), request.getType(), serverSocket.getClientHost().getHostAddress(),
                serverSocket.getClientPort());
        return null;
    }

    private void saveReplyForRequest(Reply reply, String requestKey) {
        long expired = System.currentTimeMillis() + EXPIRED_DURATION_IN_MILLIS;
        ReplyRecord record = new ReplyRecord(requestKey, expired, reply);
        replyHistory.put(requestKey, record);
    }

    private String sendReply(Reply reply) {
        if (!Network.attemptingTransmission()) {
            return null;
        }
        serverSocket.sendReply(reply);
        if (serverSocket.getError() != null) {
            return serverSocket.getError();
        }
        return null;

    }

}

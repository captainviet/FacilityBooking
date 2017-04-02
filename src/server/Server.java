package server;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import client.Reply;
import client.Request;
import shared.DateTime;
import shared.DayOfWeek;
import shared.Encoder;
import shared.FreeSlot;
import shared.Network;
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
        System.out.printf("Server listening on host: %s, port: %s\n", serverHost.getHostName(), serverPort);
    }

    public String receiveAndProcessRequest() {
        String error;
        byte[] data = serverSocket.receiveRequest();
        if (serverSocket.getError() != null) {
            return serverSocket.getError();
        } else {
            Request request = Request.unmarshal(data);
            //filtering request if invocation semantic is at most once
            if (mode == SemanticsMode.AT_MOST_ONCE && replyHistory.containsKey(request.getId())) {
                ReplyRecord record = replyHistory.get(request.getId());
                error = sendReply(record.getReply());
            } else {
                error = handleRequest(request);
            }
        }
        return error;
    }

    private String handleRequest(Request request) {
        switch (request.getType()) {
        case Request.QUERY:
            return processQueryAvailabilityRequest(request);
        case Request.BOOK:
            return processBookingRequest(request);
        case Request.EDIT:
            processEditBookingRequest(request);
            break;
        case Request.MONITOR:
            processMonitorRequest(request);
            break;
        case Request.CANCEL:
            processCancelBookingRequest(request);
            break;
        case Request.GET_ALL:
            processGetAllAvailableInTimeRangeRequest(request);
            break;
        default:
            return "Invalid_operation";
        }
        return null;
    }

    private void logRequest(String requestId, String requestType, String clientIp, int clientPort) {
        System.out.printf("[%s] Successfully processed request, id: %s, type: %s, client %s:%d",
                Utils.currentLogFormatTime(), requestId, requestType, clientIp, clientPort);
    }

    private String processGetAllAvailableInTimeRangeRequest(Request request) {
        ArrayList<String> result = new ArrayList<>();
        boolean hasError = false;
        ArrayList<String> payloads = request.getPayloads();
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

        Reply reply = new Reply(hasError, result);
        String error = sendReply(reply);
        if (error != null) {
            return error;
        }
        if (mode == SemanticsMode.AT_MOST_ONCE) {
            saveReplyForRequest(reply, request.getId());
        }
        logRequest(request.getId(), request.getType(), serverSocket.getClientHost().getHostAddress(),
                serverSocket.getClientPort());
        return null;

    }

    private String processCancelBookingRequest(Request request) {
        ArrayList<String> result = new ArrayList<>();
        boolean hasError = false;
        ArrayList<String> payloads = request.getPayloads();
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
        Reply reply = new Reply(hasError, result);
        String error = sendReply(reply);
        if (error != null) {
            return error;
        }
        if (mode == SemanticsMode.AT_MOST_ONCE) {
            saveReplyForRequest(reply, request.getId());
        }
        logRequest(request.getId(), request.getType(), serverSocket.getClientHost().getHostAddress(),
                serverSocket.getClientPort());
        if (facilityName != null && !hasError) {
            MonitoringService.updateClients(facilityName, serverSocket);
        }
        return null;
    }

    private String processMonitorRequest(Request request) {
        ArrayList<String> result = new ArrayList<>();
        boolean hasError = false;
        ArrayList<String> payloads = request.getPayloads();
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
        Reply reply = new Reply(hasError, result);
        String error = sendReply(reply);
        if (error != null) {
            return error;
        }
        if (mode == SemanticsMode.AT_MOST_ONCE) {
            saveReplyForRequest(reply, request.getId());
        }
        logRequest(request.getId(), request.getType(), serverSocket.getClientHost().getHostAddress(),
                serverSocket.getClientPort());
        return null;
    }

    private String processEditBookingRequest(Request request) {
        ArrayList<String> result = new ArrayList<>();
        boolean hasError = false;
        ArrayList<String> payloads = request.getPayloads();
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
        Reply reply = new Reply(hasError, result);
        String error = sendReply(reply);
        if (error != null) {
            return error;
        }
        if (mode == SemanticsMode.AT_MOST_ONCE) {
            saveReplyForRequest(reply, request.getId());
        }
        logRequest(request.getId(), request.getType(), serverSocket.getClientHost().getHostAddress(),
                serverSocket.getClientPort());
        if (facilityName != null && !hasError) {
            MonitoringService.updateClients(facilityName, serverSocket);
        }
        return null;
    }

    private String processBookingRequest(Request request) {
        ArrayList<String> result = new ArrayList<>();
        boolean hasError = false;
        ArrayList<String> payloads = request.getPayloads();
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
        Reply reply = new Reply(hasError, result);
        String error = sendReply(reply);
        if (error != null) {
            return error;
        }
        if (mode == SemanticsMode.AT_MOST_ONCE) {
            saveReplyForRequest(reply, request.getId());
        }
        logRequest(request.getId(), request.getType(), serverSocket.getClientHost().getHostAddress(),
                serverSocket.getClientPort());
        if (!hasError) {
            MonitoringService.updateClients(facilityName, serverSocket);
        }
        return null;
    }

    private String processQueryAvailabilityRequest(Request request) {
        ArrayList<String> result = new ArrayList<>();
        boolean hasError = false;
        ArrayList<String> payloads = request.getPayloads();
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
        Reply reply = new Reply(hasError, result);
        String error = sendReply(reply);
        if (error != null) {
            return error;
        }
        if (mode == SemanticsMode.AT_MOST_ONCE) {
            saveReplyForRequest(reply, request.getId());
        }
        logRequest(request.getId(), request.getType(), serverSocket.getClientHost().getHostAddress(),
                serverSocket.getClientPort());
        return null;
    }

    private void saveReplyForRequest(Reply reply, String requestId) {
        long expired = System.currentTimeMillis() + EXPIRED_DURATION_IN_MILLIS;
        ReplyRecord record = new ReplyRecord(requestId, expired, reply);
        replyHistory.put(requestId, record);
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

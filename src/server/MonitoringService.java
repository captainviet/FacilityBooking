package server;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

import shared.Constant;
import shared.DateTime;
import shared.DayOfWeek;
import shared.Encoder;
import shared.FreeSlot;
import shared.Reply;

public class MonitoringService {
    private static ArrayList<ClientMonitor> clients = new ArrayList<>();

    private MonitoringService() {

    }

    public static boolean registerClient(ClientMonitor client) {
        clients.add(client);
        return true;
    }

    public static void updateClients(String facilityName, ServerSocket serverSocket) {
        byte[] monitorReply = null;
        byte[] stopMonitorReply = null;
        DateTime now = DateTime.now();
        ArrayList<ClientMonitor> deregister = new ArrayList<>();
        ArrayList<ClientMonitor> toBeUpdated = new ArrayList<>();
        for (ClientMonitor client : clients) {
            if (client.getMonitorEnd().compareTo(now) >= 0) {
                if (client.getFacilityName().equals(facilityName)) {
                    // send update to the clients
                    toBeUpdated.add(client);
                }
            } else {
                deregister.add(client);
            }
        }
        if (!deregister.isEmpty()) {
            stopMonitorReply = getStopMonitorReply();
        }
        for (ClientMonitor client : deregister) {
            deregisterClient(client);
            DatagramPacket packet = new DatagramPacket(stopMonitorReply, stopMonitorReply.length, client.getHost(),
                    client.getPort());
            serverSocket.sendPacket(packet);
            if (serverSocket.error() != null) {
                printError(serverSocket.error(), client);
            }

        }
        if (!toBeUpdated.isEmpty()) {
            monitorReply = getMonitorReply(facilityName);
        }
        for (ClientMonitor client : toBeUpdated) {
            DatagramPacket packet = new DatagramPacket(monitorReply, monitorReply.length, client.getHost(),
                    client.getPort());
            serverSocket.sendPacket(packet);
            if (serverSocket.error() != null) {
                printError(serverSocket.error(), client);
            }
        }
    }

    private static byte[] getStopMonitorReply() {
        ArrayList<String> payloads = new ArrayList<>();
        payloads.add(Constant.STOP_MONITOR);
        Reply reply = Reply.constructReply(false, payloads);
        return Reply.marshal(reply);
    }

    private static byte[] getMonitorReply(String facilityName) {
        Facility facility = Facility.getFacilityByName(facilityName);
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < 6; ++i) {
            StringBuilder resultOneDay = new StringBuilder();
            resultOneDay.append(i).append("|");
            DayOfWeek d = DayOfWeek.valueOf(i);
            List<FreeSlot> freeSlots = QueryService.getAvailableFacility(facility, d);
            for (int j = 0; j < freeSlots.size(); j++) {
                resultOneDay.append(Encoder.fromFreeSlotToString(freeSlots.get(i)));
                if (i != freeSlots.size() - 1) {
                    resultOneDay.append("|");
                }
            }
            result.add(resultOneDay.toString());
        }
        Reply reply = Reply.constructReply(false, result);
        return Reply.marshal(reply);
    }

    private static void deregisterClient(ClientMonitor client) {
        clients.remove(client);
    }

    public static boolean checkClientMonitorExisted(ClientMonitor clientMonitor) {
        for (ClientMonitor client : clients) {
            if (client.equals(clientMonitor)) {
                return true;
            }
        }
        return false;
    }

    private static void printError(String error, ClientMonitor client) {
        System.out.printf("Error %s on updating monitor data to client %s:%s\n", error, client.getHost(),
                client.getPort());
    }
}

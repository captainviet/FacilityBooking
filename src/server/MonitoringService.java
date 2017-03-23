package server;


import java.util.ArrayList;

public class MonitoringService {
    private static ArrayList<ClientMonitor> clients = new ArrayList<>();

    public MonitoringService() {

    }

    public static boolean registerClient(ClientMonitor client){
        clients.add(client);
        return true;
    }

    public static void updateClients(String facilityName, DateTime now){
        ArrayList<ClientMonitor> deregister = new ArrayList<>();
        for (ClientMonitor client : clients){
            if (client.getMonitorStart().compareTo(now) <= 0
                    && client.getMonitorEnd().compareTo(now) >= 0){
                if (client.getFacilityName().equals(facilityName)){
                    // send update to the clients
                }
            }
            else {
                deregister.add(client);
            }
        }
        for (ClientMonitor client : deregister){
            deregisterClient(client);
        }
    }

    private static void deregisterClient(ClientMonitor client){
        clients.remove(client);
    }
}

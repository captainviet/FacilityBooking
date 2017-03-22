package server;


import java.util.ArrayList;

public class MonitoringService {
    private ArrayList<ClientMonitor> clients;

    public MonitoringService() {
        this.clients = new ArrayList<>();
    }

    public boolean registerClient(ClientMonitor client){
        this.clients.add(client);
        return true;
    }

    public void updateClients(String facility_name, DateTime now){
        ArrayList<ClientMonitor> deregister = new ArrayList<>();
        for (ClientMonitor client : clients){
            if (client.getMonitor_start().compareTo(now) <= 0
                    && client.getMonitor_end().compareTo(now) >= 0){
                if (client.getFacility_name().equals(facility_name)){
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

    public void deregisterClient(ClientMonitor client){
        clients.remove(client);
    }
}

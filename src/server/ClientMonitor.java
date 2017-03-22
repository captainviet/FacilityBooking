package server;


public class ClientMonitor {
    private String ip;
    private int port;
    private String facility_name;
    private DateTime monitor_start;
    private DateTime monitor_end;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getFacility_name() {
        return facility_name;
    }

    public void setFacility_name(String facility_name) {
        this.facility_name = facility_name;
    }

    public DateTime getMonitor_start() {
        return monitor_start;
    }

    public void setMonitor_start(DateTime monitor_start) {
        this.monitor_start = monitor_start;
    }

    public DateTime getMonitor_end() {
        return monitor_end;
    }

    public void setMonitor_end(DateTime monitor_end) {
        this.monitor_end = monitor_end;
    }

    public ClientMonitor(String ip, int port, String facility_name, DateTime monitor_start, DateTime monitor_end) {
        this.ip = ip;
        this.port = port;
        this.facility_name = facility_name;
        this.monitor_start = monitor_start;
        this.monitor_end = monitor_end;
    }

}

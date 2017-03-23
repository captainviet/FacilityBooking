package server;


public class ClientMonitor {
    private String ip;
    private int port;
    private String facilityName;
    private DateTime monitorStart;
    private DateTime monitorEnd;

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

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public DateTime getMonitorStart() {
        return monitorStart;
    }

    public void setMonitorStart(DateTime monitorStart) {
        this.monitorStart = monitorStart;
    }

    public DateTime getMonitorEnd() {
        return monitorEnd;
    }

    public void setMonitorEnd(DateTime monitorEnd) {
        this.monitorEnd = monitorEnd;
    }

    public ClientMonitor(String ip, int port, String facilityName, DateTime monitorStart, DateTime monitorEnd) {
        this.ip = ip;
        this.port = port;
        this.facilityName = facilityName;
        this.monitorStart = monitorStart;
        this.monitorEnd = monitorEnd;
    }

}

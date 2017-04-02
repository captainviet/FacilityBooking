package server;

import java.net.InetAddress;

import shared.DateTime;

public class ClientMonitor {
    private InetAddress host;

	private int port;
    private String facilityName;
    private DateTime monitorEnd;

    public InetAddress getHost() {
		return host;
	}

	public void setHost(InetAddress host) {
		this.host = host;
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

    public DateTime getMonitorEnd() {
        return monitorEnd;
    }

    public void setMonitorEnd(DateTime monitorEnd) {
        this.monitorEnd = monitorEnd;
    }

    public ClientMonitor(InetAddress host, int port, String facilityName, DateTime monitorEnd) {
        this.host = host;
        this.port = port;
        this.facilityName = facilityName;
        this.monitorEnd = monitorEnd;
    }
    
    @Override
    public boolean equals(Object object) {
    	if (object == null) {
    		return false;
    	}
    	if (object instanceof ClientMonitor) {
    		ClientMonitor other = (ClientMonitor) object;
    		if (this.host == other.getHost() && this.port == other.getPort() 
    				&& this.facilityName.equals(other.getFacilityName())) {
    			return true;
    		}
    	}
    	return false;
    }

}

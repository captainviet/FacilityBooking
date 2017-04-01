package server;

public class ReplyRecord {
	private String requestId;
	private long expired;
	private String reply;
	
	public ReplyRecord(String requestId, long expired, String reply) {
		this.requestId = requestId;
		this.reply = reply;
		this.expired = expired;
		
	}
	public String getRequestId() {
		return requestId;
	}

	public long getExpired() {
		return expired;
	}

	public String getReply() {
		return reply;
	}
	
}

package server;

import shared.Reply;

public class ReplyRecord {
	private String requestId;
	private long expired;
	private Reply reply;
	
	public ReplyRecord(String requestId, long expired, Reply reply) {
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

	public Reply getReply() {
		return reply;
	}
	
}

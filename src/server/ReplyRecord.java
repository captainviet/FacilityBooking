package server;

import shared.Reply;

public class ReplyRecord {
	private String requestKey;
	private long expired;
	private Reply reply;
	
	public ReplyRecord(String requestKey, long expired, Reply reply) {
		this.requestKey = requestKey;
		this.reply = reply;
		this.expired = expired;
		
	}
	public String getRequestKey() {
		return requestKey;
	}

	public long getExpired() {
		return expired;
	}

	public Reply getReply() {
		return reply;
	}
	
}

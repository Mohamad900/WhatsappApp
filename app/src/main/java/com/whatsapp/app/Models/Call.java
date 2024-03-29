package com.whatsapp.app.Models;

public class Call {

	private String from, type, to, messageID, time, date, name,status;


	public Call(){

	}

	public Call(String from, String type, String to, String messageID, String time, String date, String name, String status) {
		this.from = from;
		this.type = type;
		this.to = to;
		this.messageID = messageID;
		this.time = time;
		this.date = date;
		this.name = name;
		this.status = status;
	}


	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getMessageID() {
		return messageID;
	}

	public void setMessageID(String messageID) {
		this.messageID = messageID;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}

package com.whatsapp.app.Models;

import java.util.ArrayList;

public class Status {
	private ArrayList<String> images;
	public String userNname;
	private String time;


	public Status(ArrayList<String> images, String userNname, String time) {
		this.images = images;
		this.userNname = userNname;
		this.time = time;
	}

	public Status(ArrayList<String> images, String userNname) {
		this.images = images;
		this.userNname = userNname;
	}


	public ArrayList<String> getImages() {
		return images;
	}

	public void setImages(ArrayList<String> images) {
		this.images = images;
	}

	public String getUserNname() {
		return userNname;
	}

	public void setUserNname(String userNname) {
		this.userNname = userNname;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
}

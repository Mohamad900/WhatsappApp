package com.whatsapp.app.Models;

import java.util.ArrayList;

public class Status {
	private String id;
	private ArrayList<String> images;
	public String userNname;
	private String time;
	private String image;


	public Status(ArrayList<String> images, String userNname, String time) {
		this.images = images;
		this.userNname = userNname;
		this.time = time;
	}

	public Status(ArrayList<String> images, String userNname) {
		this.images = images;
		this.userNname = userNname;
	}

	public Status(String id,String image) {
		this.id = id;
		this.image = image;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
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

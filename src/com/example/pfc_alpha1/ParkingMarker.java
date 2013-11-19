package com.example.pfc_alpha1;

public class ParkingMarker {
	
	private String name;
	private double lat;
	private double lng;
	private boolean free;
	private double dist;

	
	

	public ParkingMarker(String name, double lat, double lng, boolean free, double dist) {
		super();
		this.name = name;
		this.lat = lat;
		this.lng = lng;
		this.free = free;
		this.dist = dist;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	public boolean getFree() {
		return free;
	}
	public void setFree(Boolean free) {
		this.free = free;
	}
	
	public double getDist() {
		return dist;
	}

	public void setDist(double dist) {
		this.dist = dist;
	}
	
}

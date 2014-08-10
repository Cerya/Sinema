package com.nami.android.sinema;

import java.util.ArrayList;
import java.util.List;

public class Movie {
	String id;
	String imageLink;
	String title;
	List<String> genre = new ArrayList<String>();
	String year;
	float rating;
	String peers;
	String seeds;
	String magnetLink;
	String quality;
	String size;
	String synopsis;
	String trailer;
	String torrentLink;
	String hash;

	public Movie(String id, String imageLink, String trailer, String title, String synopsis, List<String> genre,
			String year, String size, float rating, String quality, String peers, String seeds, String torrentLink, String magnetLink, String hash) {
		this.id = id;
		this.imageLink = imageLink;
		this.title = title;
		this.synopsis = synopsis;
		this.genre = genre;
		this.year = year;
		this.rating = rating;
		this.peers = peers;
		this.seeds = seeds;
		this.torrentLink = torrentLink;
		this.magnetLink = magnetLink;
		this.quality = quality;
		this.size = size;
		this.trailer = trailer;
		this.hash = hash;
	}

	public String getHash(){
		return this.hash;
	}
	public String getTrailer(){
		return this.trailer;
	}
	public String getSynopsis(){
		return this.synopsis;
	}
	public String getId(){
		return this.id;
	}
	public String getSize(){
		return this.size;	
	}
	
	public String getTitle() {
		return this.title;
	}

	public String getImageLink() {
		return this.imageLink;
	}

	public List<String> getGenre() {
		return this.genre;
	}

	public String getYear() {
		return this.year;
	}

	public float getRating() {
		return this.rating;
	}

	public String getSeeds() {
		return this.seeds;
	}

	public String getPeers() {
		return this.peers;
	}
	
	public String getQuality(){
		return this.quality;
	}
	public String getMagnetLink(){
		return this.magnetLink;
	}
	public String getTorrentLink(){
		return this.torrentLink;
	}
	public void setTrailer(String trailer){
		this.trailer = trailer;
	}
}

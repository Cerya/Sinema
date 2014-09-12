package com.nami.android.sinema;

public class TVGuide {
	private int id;
	private String simage;
	private String title;
	private String episode;
	private String duration;
	private int progress;
	public TVGuide(int id, String simage, String title, String episode, String duration, int progress){
		this.id = id;
		this.setSimage(simage);
		this.setTitle(title);
		this.setEpisode(episode);
		this.setDuration(duration);
		this.setProgress(progress);
	}
	public int getProgress() {
		return progress;
	}
	public void setProgress(int progress) {
		this.progress = progress;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getEpisode() {
		return episode;
	}
	public void setEpisode(String episode) {
		this.episode = episode;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSimage() {
		return simage;
	}
	public void setSimage(String simage) {
		this.simage = simage;
	}
}

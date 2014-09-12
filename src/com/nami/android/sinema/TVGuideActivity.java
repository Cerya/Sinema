package com.nami.android.sinema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.unbescape.html.HtmlEscape;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.image.SmartImageView;

public class TVGuideActivity extends Activity {
	private ArrayAdapter<TVGuide> adapter;
	private List<TVGuide> tvguide = new ArrayList<TVGuide>();
	private ListView list;
	private ProgressDialog mProgressDialog;
	List<String> channels = new ArrayList<String>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show);
		setTitle("TV Guide");
		if(isNetworkAvailable()){
			new LoadOperation(TVGuideActivity.this).execute(true);
		} else {
			new LoadOperation(TVGuideActivity.this).execute(false);
		}
	}
	
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
	
	private void populateTVGuideList(String filter) throws IOException {
		
		Document doc = Jsoup.connect("http://tv.bartv.be").ignoreContentType(true).timeout(10 * 1000).get();
    	Elements resultList = doc.select(".showitem");
    	Element tvshow = null;
        for (int i=0;i<resultList.size();i++) {
        	tvshow = resultList.get(i);
        	int id = 0;
        	String simage = HtmlEscape.unescapeHtml(tvshow.select("img").first().attr("abs:src"));
        	String title = HtmlEscape.unescapeHtml(tvshow.select(".largelink").html());
        	String episode = HtmlEscape.unescapeHtml(tvshow.select(".episode").html());
        	String duration;
        	if(!episode.equalsIgnoreCase("")){
        		//There is an episode
        		duration = HtmlEscape.unescapeHtml(tvshow.select(".episode").first().nextSibling().toString());
        	} else {
        		duration = HtmlEscape.unescapeHtml(tvshow.select("h3").first().nextSibling().toString());
        	}
        	int progress = Integer.parseInt(tvshow.select("div.progress").first().attr("class").substring(tvshow.select("div.progress").first().attr("class").length()-1));
        	
            TVGuide a = new TVGuide(id, simage, title, episode, duration, progress);
            tvguide.add(a);
           
        }
    }
	
	private void populateListView() {
		adapter = new MyListAdapter();
		list = (ListView) findViewById(R.id.listTVGuide);
		adapter.setNotifyOnChange(true);
		list.setAdapter(adapter);
	}


	
	private class MyListAdapter extends ArrayAdapter<TVGuide> {
		public MyListAdapter() {
			super(TVGuideActivity.this, R.layout.view_list_item, tvguide);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Make sure we have a view to work with
			View itemView = convertView;
			if (itemView == null) {
				itemView = getLayoutInflater().inflate(R.layout.view_tvguide_item, parent, false);
			}
			// Find the result to work with
			final TVGuide currentShow = tvguide.get(position);
			
			// Fill the view
			
			//Icon
			SmartImageView tvicon = (SmartImageView) itemView.findViewById(R.id.imgTvIcon);
			tvicon.setImageUrl(currentShow.getSimage());
			//Title
			TextView title = (TextView) itemView.findViewById(R.id.txtTvTitle);
			title.setText(currentShow.getTitle());
			//Time
			TextView time = (TextView) itemView.findViewById(R.id.txtTvTime);
			time.setText(currentShow.getDuration());
			//Episode
			TextView episode = (TextView) itemView.findViewById(R.id.txtTvEpisode);
			episode.setText(currentShow.getEpisode());
			//ProgressBar
			ProgressBar pb = (ProgressBar) itemView.findViewById(R.id.pbTv);
			pb.setProgress(currentShow.getProgress() * 10);
			return itemView;
		}
	}
	
	
	
private class LoadOperation extends AsyncTask<Boolean, Integer, String> {
		
        private Context context;
        public LoadOperation(Context context) {
            this.context = context;
        }
        
        @Override
        protected String doInBackground(Boolean... params) {
        	if(params[0].booleanValue()){
        		mProgressDialog.setMessage("Fetching channels and show information...");
                try {
                	populateTVGuideList("");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
        	} else {
        		//mProgressDialog.setTitle("Connection problem");
        		mProgressDialog.setMessage("Please check your internet connection...\nShutting down Sinema");
    			//mProgressDialog.show();
        		try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		finish();
        	}
        	return null;
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(TVGuideActivity.this);
			mProgressDialog.setIndeterminate(true);
	        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        mProgressDialog.setCancelable(false);
	        mProgressDialog.setTitle("Loading");
	        mProgressDialog.show();
			
        }
        
        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMax(0);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            if(tvguide.size() > 0){
	        	populateListView();
            } else {
            	Toast.makeText(TVGuideActivity.this, "No channels found.", Toast.LENGTH_LONG).show();
            }
            mProgressDialog.dismiss();
        }
    }
	
}

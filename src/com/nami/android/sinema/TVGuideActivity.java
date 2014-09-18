package com.nami.android.sinema;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.unbescape.html.HtmlEscape;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
	private int currentPage = 0;
	private Handler mHandler = new Handler();
	List<String> channels = new ArrayList<String>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show);
		setTitle("TV Guide");
		int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
		TextView yourTextView = (TextView)findViewById(titleId); 
		yourTextView.setTextColor(Color.WHITE);
		if(isNetworkAvailable()){
			new LoadOperation(TVGuideActivity.this).execute(true);
		} else {
			new LoadOperation(TVGuideActivity.this).execute(false);
		}
		final TextView showingNow = (TextView) findViewById(R.id.txtTvInfo);
		showingNow.setText("Now ("+new SimpleDateFormat("HH:mm").format(new Date().getTime())+")");
		showingNow.setClickable(true);
		showingNow.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				currentPage = 0;
				showingNow.setText("Now ("+new SimpleDateFormat("HH:mm").format(new Date().getTime())+")");
				if(adapter!=null){
					new LoadOperation(TVGuideActivity.this).execute(true);
					tvguide.clear();
					list.invalidateViews();
					adapter.notifyDataSetChanged();
				}
			}
		});

		//Buttons +1h | -1h
		final Button btnPlus1 = (Button) findViewById(R.id.btnPlus1);
		btnPlus1.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mHandler.removeCallbacksAndMessages(null);
				currentPage++;
	    		runOnUiThread(new Runnable(){
	    		    @Override
	    		    public void run(){
	    		    	//Current info
	    	    		if(currentPage == 0) {
	    	    			showingNow.setText("Now ("+new SimpleDateFormat("HH:mm").format(new Date().getTime())+")");
	    	    		} else {
		    	    		showingNow.setText(getTimeHeader(currentPage)); 		
	    	    		}
	    		    }
	    		});
				mHandler.postDelayed(new Runnable() {
		            @Override
					public void run() {
		            	if(adapter!=null){
		            		btnPlus1.setEnabled(false);
							new LoadOperation(TVGuideActivity.this).execute(true);
							tvguide.clear();
							list.invalidateViews();
							adapter.notifyDataSetChanged();
							btnPlus1.setEnabled(true);
						}
		            }
		        }, 900);    
			}	
		});	
		final Button btnMin1 = (Button) findViewById(R.id.btnMinus1);
		btnMin1.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mHandler.removeCallbacksAndMessages(null);
				currentPage--;
	    		runOnUiThread(new Runnable(){
	    		    @Override
	    		    public void run(){
	    		    	//Current info
	    	    		if(currentPage == 0) {
	    	    			showingNow.setText("Now ("+new SimpleDateFormat("HH:mm").format(new Date().getTime())+")");
	    	    		} else {
    	    				showingNow.setText(getTimeHeader(currentPage));
    	    			}
	    		    }
	    		});
				mHandler.postDelayed(new Runnable() {
		            @Override
					public void run() {
		            	if(adapter!=null){
		            		btnMin1.setEnabled(false);
							new LoadOperation(TVGuideActivity.this).execute(true);
							tvguide.clear();
							list.invalidateViews();
							adapter.notifyDataSetChanged();
							btnMin1.setEnabled(true);
						}
		            }
		        }, 900);    
			}
		});	
	}
	
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
    }
	
	private void registerClickCallback() {
        final ListView list = (ListView) findViewById(R.id.listTVGuide);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                TVGuide clickedChannel = tvguide.get(position);

                if(clickedChannel.getTitle().contains("Film")){
                    Intent intent = new Intent(TVGuideActivity.this, MainActivity.class);
                    intent.putExtra("search", clickedChannel.getTitle().substring(0, clickedChannel.getTitle().indexOf("(")));
                    startActivity(intent);
                    overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
                } else {
                    Toast.makeText(TVGuideActivity.this, "You can only download movies", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
	
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
	
	private void populateTVGuideList(String filter) throws IOException {
		
		Document doc = Jsoup.connect("http://tv.bartv.be/shows/cat/1/"+filter).ignoreContentType(true).timeout(10 * 1000).get();
    	Elements resultList = doc.select(".showitem");
    	Element tvshow = null;
    	int id = 0;
        for (int i=0;i<resultList.size();i++) {
        	tvshow = resultList.get(i);
        	id++;
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
        	int progress = Integer.parseInt(tvshow.select("div.progress").first().attr("class").substring(17));
        	
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


	private String getTimeHeader(int currentPage){
		String wordOfTheDay = "";
		Calendar c = Calendar.getInstance();
		int temp = c.get(Calendar.HOUR_OF_DAY) + currentPage;
		int diffDay = temp/24;
		if(temp < 0 && temp >= -24){
			wordOfTheDay = "Yesterday";
		} else if(diffDay == 0){
			wordOfTheDay = "Today";
		} else if(diffDay == 1){
			wordOfTheDay = "Tomorrow";
		} else {
			wordOfTheDay = "";
		}
		c.add(Calendar.HOUR, currentPage);
		SimpleDateFormat sfd = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm");
		SimpleDateFormat sfd2 = new SimpleDateFormat(" 'at' HH:mm");
		if(wordOfTheDay.equalsIgnoreCase("")){
			return sfd.format(c.getTime());
		} else {
			return wordOfTheDay + sfd2.format(c.getTime());
		}
		
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
			pb.getProgressDrawable().setColorFilter(Color.argb(250, 253, 154, 52), Mode.SRC_IN);
			if(currentShow.getProgress() < 0){
				pb.setProgress(0);
			} else {
				pb.setProgress(currentShow.getProgress() * 10);
			}
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
            try {
            	populateTVGuideList(""+currentPage);
            } catch (IOException e) {
                e.printStackTrace();
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
	        mProgressDialog.setMessage("Fetching channels and show information...");
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
	        	registerClickCallback();
            } else {
            	Toast.makeText(TVGuideActivity.this, "No channels found.", Toast.LENGTH_LONG).show();
            }
            mProgressDialog.dismiss();
        }
    }
}

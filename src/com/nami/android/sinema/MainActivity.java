package com.nami.android.sinema;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.unbescape.html.HtmlEscape;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coderplus.filepicker.FilePickerActivity;
import com.loopj.android.image.SmartImageView;


/**
 * TODO
 * Add a "Thinklist"
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainActivity extends ActionBarActivity {
	private List<Movie> movies = new ArrayList<Movie>();
	private String trailer;
	private Document doc;
	private ProgressDialog mProgressDialog;
	private SharedPreferences settings;
	private String genre;
	private String rating;
	private String quality;
	private String limit;
	private String client;
	private String order;
	private String sort;
	private String sync_path;
	private boolean should_update = true;
	private ArrayAdapter<Movie> adapter;
	ListView list;

	private static final int REQUEST_PICK_FILE = 999;
	EditText keywords;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle("Movies");
		int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
		TextView yourTextView = (TextView)findViewById(titleId); 
		yourTextView.setTextColor(Color.WHITE);
		settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		client = settings.getString("transfer_method_list", SinemaConstant.CLIENT);
		genre = settings.getString("genre_list", SinemaConstant.GENRE);
		sort = settings.getString("sort_list", SinemaConstant.SORT);
		quality = settings.getString("quality_list", SinemaConstant.QUALITY);
		rating = settings.getString("rating_list", SinemaConstant.RATING);
		limit = settings.getString("limit_list", SinemaConstant.LIMIT);
		order = settings.getString("order_list", SinemaConstant.ORDER);
		sync_path = settings.getString("sync_path_text", "");
		String search = getIntent().getStringExtra("search");
		keywords = (EditText) findViewById(R.id.txtKeywords);
		if(search != null){
			keywords.setText(search);
		}
		init();
		keywords.setOnKeyListener(new View.OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
		       if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
		    	   new LoadOperation(MainActivity.this).execute(true);
		    	   InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		    	   imm.hideSoftInputFromWindow(keywords.getWindowToken(), 0);
		    	   if(adapter!=null){
		    		   movies.clear();
		    		   list.invalidateViews();
		    		   adapter.notifyDataSetChanged();
		    	   }
		    	   return true;
		       }
		       return false;
		   }
		});
	}
	
	private void init(){
		client = settings.getString("transfer_method_list", SinemaConstant.CLIENT);
		genre = settings.getString("genre_list", SinemaConstant.GENRE);
		sort = settings.getString("sort_list", SinemaConstant.SORT);
		quality = settings.getString("quality_list", SinemaConstant.QUALITY);
		rating = settings.getString("rating_list", SinemaConstant.RATING);
		limit = settings.getString("limit_list", SinemaConstant.LIMIT);
		order = settings.getString("order_list", SinemaConstant.ORDER);
		sync_path = settings.getString("sync_path_text", "");
		if(should_update){
			should_update = false;
			new LoadOperation(MainActivity.this).execute(true);
			if(adapter!=null){
				movies.clear();
				list.invalidateViews();
				adapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		init();
	}

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startActivity(new Intent(MainActivity.this, SettingsActivity.class));
			should_update = true;
			overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
		} else if(id == R.id.action_tvguide) {
			startActivity(new Intent(MainActivity.this, TVGuideActivity.class));
			overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
		} else if(id == R.id.action_about) {
			startActivity(new Intent(MainActivity.this, AboutActivity.class));
			overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	/*
	 * 
	 * FUNCTIONS FOR LISTVIEW
	 * 
	 */
	
	private void populateMovieList(String filter) throws IOException {
		if(!keywords.getText().toString().equalsIgnoreCase("")){
			doc = Jsoup.connect(SinemaConstant.MOVIE_LIST_URL+"?sort="+SinemaConstant.SORT+"&rating="+rating+"&genre="+genre+"&quality="+quality+"&order="+order+"&sort="+sort+"&limit="+limit+"&keywords="+keywords.getText().toString().replace(" ", "%20")).ignoreContentType(true).timeout(10 * 1000).get();
		} else {
			doc = Jsoup.connect(SinemaConstant.MOVIE_LIST_URL+"?sort="+SinemaConstant.SORT+"&rating="+rating+"&genre="+genre+"&quality="+quality+"&order="+order+"&sort="+sort+"&limit="+limit).ignoreContentType(true).timeout(10 * 1000).get();
		}
    	Elements resultList = doc.select("item");
        Element movie = null;
        for (int i=0;i<resultList.size();i++) {
        	movie = resultList.get(i);
        	String id = HtmlEscape.unescapeHtml(movie.select("MovieID").html());
        	String imageLink = HtmlEscape.unescapeHtml(movie.select("CoverImage").html());
        	String title = HtmlEscape.unescapeHtml(movie.select("MovieTitleClean").html());
        	List<String>genre = new ArrayList<String>();
        	genre.add(HtmlEscape.unescapeHtml(movie.select("Genre").html()));
        	String year = HtmlEscape.unescapeHtml(movie.select("MovieYear").html());
        	float rating = Float.parseFloat(HtmlEscape.unescapeHtml(movie.select("MovieRating").html()));
        	String peers = HtmlEscape.unescapeHtml(movie.select("TorrentPeers").html());
        	String seeds = HtmlEscape.unescapeHtml(movie.select("TorrentSeeds").html());
        	String quality = HtmlEscape.unescapeHtml(movie.select("Quality").html());
        	String magnetLink = HtmlEscape.unescapeHtml(movie.select("TorrentMagnetUrl").html());
        	String torrentLink = HtmlEscape.unescapeHtml(movie.select("TorrentUrl").html());
        	String size = HtmlEscape.unescapeHtml(movie.select("Size").html());
        	String hash = HtmlEscape.unescapeHtml(movie.select("TorrentHash").html());
            Movie a = new Movie(id, imageLink, trailer, title, "", genre, year, size, rating, quality, peers, seeds, torrentLink, magnetLink, hash);
            movies.add(a);
           
        }
        // To dismiss the dialog
        mProgressDialog.dismiss();
    }

	private void populateListView() {
		adapter = new MyListAdapter();
		list = (ListView) findViewById(R.id.listMovies);
		adapter.setNotifyOnChange(true);
		list.setAdapter(adapter);
	}


	
	private class MyListAdapter extends ArrayAdapter<Movie> {
		public MyListAdapter() {
			super(MainActivity.this, R.layout.view_list_item, movies);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Make sure we have a view to work with
			View itemView = convertView;
			if (itemView == null) {
				itemView = getLayoutInflater().inflate(R.layout.view_list_item,
						parent, false);
			}
			// Find the result to work with
			final Movie currentMovie = movies.get(position);
			
			// Fill the view
			
			//Poster
			SmartImageView poster = (SmartImageView) itemView.findViewById(R.id.imgPoster);
			poster.setImageUrl(currentMovie.getImageLink());
			//Title
			TextView title = (TextView) itemView.findViewById(R.id.txtTitle);
			title.setText(currentMovie.getTitle());
			//Genre
			TextView genre = (TextView) itemView.findViewById(R.id.txtSetGenre);
			genre.setText(TextUtils.join(" | ", currentMovie.getGenre()));
			//Size
			TextView size = (TextView) itemView.findViewById(R.id.txtSize);
			size.setText(currentMovie.getSize());
			//Year
			TextView year = (TextView) itemView.findViewById(R.id.txtYear);
			year.setText(currentMovie.getYear());
			//Rating
			ProgressBar rating = (ProgressBar) itemView.findViewById(R.id.pbRating);
			rating.getProgressDrawable().setColorFilter(Color.argb(250, 44, 36, 84), Mode.SRC_IN);
			rating.setProgress((int) (currentMovie.getRating()*10));
			//Quality
			TextView quality = (TextView) itemView.findViewById(R.id.txtQuality);
			quality.setText(currentMovie.getQuality());
			//Seeds
			TextView txtRating = (TextView) itemView.findViewById(R.id.txtRating);
			txtRating.setText(""+currentMovie.getRating()+"/10");
			//Download torrent
			Button downloadbtn = (Button) itemView.findViewById(R.id.btnTorrent);
			downloadbtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					//new StartTorrentClient().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
					if(client.equalsIgnoreCase("remote")){
						new StartTransferMethod().execute(currentMovie.getMagnetLink());
					} else {
		                final DownloadTask downloadTask = new DownloadTask(MainActivity.this);
		                new StartTransferMethod().execute(currentMovie.getMagnetLink());
		                // execute this when the downloader must be fired
		                downloadTask.execute(currentMovie.getTitle(), currentMovie.getTorrentLink());
					}
				}
			});
			//Trailer
			Button trailerbtn = (Button) itemView.findViewById(R.id.btnTrailer);
			trailerbtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					new GetTrailerOp().execute(currentMovie.getId());
					currentMovie.setTrailer(trailer);
				}
			});
			return itemView;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK) {
			switch(requestCode) {
				case REQUEST_PICK_FILE:
					if(data.hasExtra(FilePickerActivity.EXTRA_FILE_PATH)) {
						// Get the file path
						@SuppressWarnings("unchecked")
						List<File> files = (List<File>)data.getSerializableExtra(FilePickerActivity.EXTRA_FILE_PATH);                   
						//Print the File/Folder  names          
						for(File file:files){
							if(file.isDirectory()) {
								//System.out.println("Directory : "+file.getAbsolutePath());
								settings.edit().putString("sync_path_text", file.getAbsolutePath()).apply();
							} else {
								//System.out.println("Directory : "+file.getAbsolutePath());
							}    
						}
					}
			}
		}
	}
	
	/*
	 * 
	 * 
	 * START BITTORRENT
	 * 
	 * 
	 */
	
	private class StartTransferMethod extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
        	if(client.equalsIgnoreCase("remote")){
        		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(params[0].toString()));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
        	}
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Intent i;
        	PackageManager manager;
    		manager = getPackageManager();
    		try {
    			if(client.equalsIgnoreCase("remote"))
    				i = manager.getLaunchIntentForPackage("com.bittorrent.android.btremote");
    			else
    				i = manager.getLaunchIntentForPackage("com.bittorrent.sync");

    		    if (i == null)
    		        throw new PackageManager.NameNotFoundException();
    		    i.addCategory(Intent.CATEGORY_LAUNCHER);
    		    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		    startActivity(i);
    		} catch (PackageManager.NameNotFoundException e) {
    			//BitTorrent Remote || BitTorrent Sync not installed
    			//Check and Create dropbox folder
    			if(client.equalsIgnoreCase("remote"))
    				Toast.makeText(MainActivity.this, "You must install BitTorrent Remote for this to work.", Toast.LENGTH_LONG).show();
    			else
    				Toast.makeText(MainActivity.this, "You must install BitTorrent Sync for this to work.", Toast.LENGTH_LONG).show();
    			cancel(true);
    		}
    		try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        @Override
		protected void onPostExecute(String result) {
			Toast.makeText(MainActivity.this, SinemaConstant.RETURN_MESSAGE, Toast.LENGTH_LONG).show();
        }
    }
	
	/*
	 * 
	 * CRAWL MOVIE DETAIL FOR TRAILER
	 * 
	 */
	
	private class GetTrailerOp extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
            	doc = Jsoup.connect(SinemaConstant.MOVIE_DETAIL_URL+"?id="+params[0].toString()).ignoreContentType(true).timeout(5 * 1000).get();
        	} catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
        	trailer = HtmlEscape.unescapeHtml(doc.select("YoutubeTrailerUrl").html());
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(trailer));
			startActivity(i);
			Toast.makeText(MainActivity.this, SinemaConstant.RETURN_MESSAGE, Toast.LENGTH_LONG).show();
        }
    }
	
	/*
	 * 
	 * LOADOPERATION
	 * POPULATING LIST
	 * 
	 * 
	 */
	private class LoadOperation extends AsyncTask<Boolean, Integer, String> {
		
        private Context context;
        private PowerManager.WakeLock mWakeLock;
        private String path;
        public LoadOperation(Context context) {
            this.context = context;
        }
        
        @Override
        protected String doInBackground(Boolean... params) {
                try {
                	populateMovieList("");
                	
                } catch (IOException e) {
                    e.printStackTrace();
                }	
        	return null;
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog = new ProgressDialog(MainActivity.this);
			mProgressDialog.setIndeterminate(true);
	        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        mProgressDialog.setCancelable(false);
	        mProgressDialog.setTitle("Loading");
    		if(keywords.getText().toString().isEmpty()){
	        	mProgressDialog.setMessage("Initializing...\n- Fetching movies\n- Fetching trailers\n- Fetching torrents");
    		} else {
    			mProgressDialog.setMessage("Searching for: " + keywords.getText().toString() + "\n\nCan't find your movie?\nTry searching by actor names");
    		}
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
            if(movies.size() > 0){
	        	populateListView();
	        	TextView setRating = (TextView) findViewById(R.id.txtSetRating);
	    		TextView setGenre  = (TextView) findViewById(R.id.txtSetGenre);
	    		TextView setQuality  = (TextView) findViewById(R.id.txtSetQuality);
	    		TextView setResult  = (TextView) findViewById(R.id.txtSetLimit);
	    		//TextView setOrder  = (TextView) findViewById(R.id.txtSetOrder);
	    		TextView setSort  = (TextView) findViewById(R.id.txtSetSort);
	    		//setOrder.setText(order);
	    		setSort.setText(sort);
	    		setRating.setText(rating+"+");
	    		setGenre.setText(genre);
	    		setQuality.setText(quality);
	    		setResult.setText("#"+movies.size());
	    		
				if(sync_path.equalsIgnoreCase("") && client.equalsIgnoreCase("sync")){
					//Select Sync Folder
					Intent intent = new Intent(MainActivity.this, FilePickerActivity.class);
					intent.putExtra(FilePickerActivity.EXTRA_SELECT_DIRECTORIES_ONLY, true);
					intent.putExtra(FilePickerActivity.EXTRA_SELECT_FILES_ONLY, false);
					intent.putExtra(FilePickerActivity.EXTRA_SELECT_MULTIPLE, false);
					intent.putExtra(FilePickerActivity.EXTRA_FILE_PATH, Environment.getExternalStorageDirectory().getAbsolutePath());
					startActivityForResult(intent, REQUEST_PICK_FILE);
					Toast.makeText(MainActivity.this, "Press and hold the file you selected for BitTorrent Sync.", Toast.LENGTH_LONG).show();
				}
            } else {
            	Toast.makeText(MainActivity.this, "No movies found.", Toast.LENGTH_LONG).show();
            }
            mProgressDialog.dismiss();
        }
    }

		
	/*
	 * 
	 * DOWNLOAD TASK
	 * 
	 * 
	 */
	
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;
        private String path;
        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[1]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();
                // download the file
                if (fileLength > 0) {
            		input = connection.getInputStream();
                    //String dir = SinemaConstant.DOWNLOAD_PATH;
                    //create folder
                    //File folder = new File(dir); //folder name
                    //folder.mkdirs();
                    path = sync_path+File.separator+ sUrl[0]+ ".torrent";
                    output = new FileOutputStream(path);
                    byte data[] = new byte[4096]; //fileLength
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        // allow canceling with back button
                        if (isCancelled()) {
                            input.close();
                            return null;
                        }
                        total += count;
                        // publishing the progress....
                        if (fileLength > 0) // only if total length is known
                            publishProgress((int) (total * 100 / fileLength));
                        output.write(data, 0, count);
                    }
                } else {
                    return "Unable to download this file.";
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
			mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setTitle("Downloading");
            mProgressDialog.setMessage("Downloading Torrent File");
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null){
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
                //Mail or Sync?
                //if(client.equalsIgnoreCase("email")){
                //	Intent m = new Intent(MainActivity.this, MailActivity.class);
                //	m.putExtra("path", path);
                //	m.putExtra("name", path.split(SinemaConstant.DOWNLOAD_PATH)[1].toString());
                //	startActivity(m);
                //}
            }
        }
    }
    
	

	
	protected boolean isRunningInForeground() {
	    ActivityManager manager = 
	         (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    List<ActivityManager.RunningTaskInfo> tasks = manager.getRunningTasks(1);
	    if (tasks.isEmpty()) {
	        return false;
	    }
	    String topActivityName = tasks.get(0).topActivity.getPackageName();
	    Toast.makeText(MainActivity.this, topActivityName, Toast.LENGTH_LONG).show();
	    return topActivityName.equalsIgnoreCase(getPackageName());
	}
	
    
}

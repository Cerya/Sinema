package com.nami.android.sinema;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.Toast;

public class OptionActivity extends Activity {
	private double newVersion = 0.0;
	private SlideButton slider;
	private ProgressDialog mProgressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_option);
		setTitle("Sinema");
		new DeleteTask().execute("");
		new UpdateTask().execute(""); 
		slider = (SlideButton) findViewById(R.id.unlockButton);
		slider.setProgress(50);
		if(isNetworkAvailable()){
			slider.setSlideButtonListener(new SlideButtonListener(){
				@Override
				public void handleSlide(boolean opt) {
					if(opt){ //Movies
						startActivity(new Intent(OptionActivity.this, MainActivity.class));
					} else { //TV Guide
						startActivity(new Intent(OptionActivity.this, TVGuideActivity.class));
					}
					overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
				}
				
			});
		} else {
			mProgressDialog = new ProgressDialog(OptionActivity.this);
			mProgressDialog.setTitle("Connection problem");
    		mProgressDialog.setMessage("Please check your internet connection");
			mProgressDialog.setCancelable(false);
			mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        dialog.dismiss();
			        finish();
			    }
			});
			mProgressDialog.show();
		}
	}
	
	/*
	 * 
	 * DELETE TASK
	 * 
	 * 
	 */
	
    private class DeleteTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            boolean deleted;
            int k = Integer.parseInt(String.valueOf(SinemaConstant.VERSION).replace(".", ""));
            for (int i = 0; i < k; i++) {
                int major = 0;
                int minor = 0;
                int l = String.valueOf(i).length();
                if (l == 1) {
                    major = 0;
                    minor = i;
                } else {
                    major = Integer.parseInt(String.valueOf(i).substring(0, l - 1));
                    minor = Integer.parseInt(String.valueOf(i).substring(l - 1, l));
                }
                File file = new File(SinemaConstant.DOWNLOAD_PATH + SinemaConstant.APPNAME + major + "." + minor + ".apk");
                if (file.exists()) {
                    deleted = file.delete();
                }
            }
            return null;
        }
    }
    private class UpdateTask extends AsyncTask<String, Void, String> {
        Boolean needsUpdate = false;

        @Override
        protected String doInBackground(String... params) {
            try {
            	needsUpdate = checkForUpdate();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(OptionActivity.this, "Checking for updates...", Toast.LENGTH_LONG).show();
        }
        
        
        @Override
        protected void onPostExecute(String result) {
            if (needsUpdate) {
                // instantiate it within the onCreate method
                final DownloadNewVersion downloadNewVersion = new DownloadNewVersion(OptionActivity.this);
                // execute this when the downloader must be fired
                downloadNewVersion.execute(SinemaConstant.DOWNLOAD_NEW_VERSION_PATH + newVersion + "/" +SinemaConstant.APPNAME + newVersion + ".apk");
            }
        }
    }
    
    
    private class DownloadNewVersion extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadNewVersion(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            mProgressDialog.setMessage("Downloading newer version");
            try {
                URL url = new URL(sUrl[0]);
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
                input = connection.getInputStream();
                output = new FileOutputStream(SinemaConstant.DOWNLOAD_PATH + SinemaConstant.APPNAME + newVersion + ".apk");
                if (fileLength > 0) {
                    byte data[] = new byte[fileLength];
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
                    return "Unable to download new version";
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
            mProgressDialog = new ProgressDialog(OptionActivity.this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setTitle("New Update");
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
            if (result != null) {
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
                
                //Uri packageURI = Uri.parse("package:" + MainActivity.class.getPackage().getName());
                //Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                //startActivity(uninstallIntent);
                
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(SinemaConstant.DOWNLOAD_PATH + SinemaConstant.APPNAME + newVersion + ".apk")), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        }
    }
    private boolean checkForUpdate() throws PackageManager.NameNotFoundException, IOException {
        /* Get current Version Number */
        //if(exists(MuzeConstants.VERSION_URL)){
        Document doc = Jsoup.connect(SinemaConstant.VERSION_URL).ignoreContentType(true).timeout(10 * 1000).get();
        Elements tagList = doc.select(".tag-name");
        if (SinemaConstant.VERSION < Double.valueOf(tagList.first().html())) {
            //Download new version
            newVersion = Double.valueOf(tagList.first().html());
            return true;
        }
        return false;
        //}
        //return false;
    }
	
	@Override
	public void onResume(){
		super.onResume();
		slider.setProgress(50);
	}
	
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
    }
    
	/*
	 * 
	 * 
	 * CHECK FUNCTIONS
	 * INTERNET
	 * ACTIVITY
	 * 
	 */
    
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

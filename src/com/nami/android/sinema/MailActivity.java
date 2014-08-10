package com.nami.android.sinema;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class MailActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_mail);
		Intent i = getIntent();
		String name = i.getStringExtra("name");
		String path = i.getStringExtra("path");
		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { SinemaConstant.MAIL_TO });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, name);
		Uri URI = Uri.parse("file://"+path); //"file://" // "android.resource://"
		if (URI != null) {
			emailIntent.putExtra(Intent.EXTRA_STREAM, URI);
		}
		startActivity(emailIntent);
		this.finish();
	}
	
}

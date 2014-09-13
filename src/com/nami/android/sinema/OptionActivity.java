package com.nami.android.sinema;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class OptionActivity extends Activity {

	private SlideButton slider;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_option);
		setTitle("Sinema");
		slider = (SlideButton) findViewById(R.id.unlockButton);
		slider.setProgress(50);
		slider.setSlideButtonListener(new SlideButtonListener(){
			@Override
			public void handleSlide(boolean opt) {
				if(opt){ //Movies
					startActivity(new Intent(OptionActivity.this, MainActivity.class));
				} else { //TV Guide
					startActivity(new Intent(OptionActivity.this, TVGuideActivity.class));
				}
			}
			
		});
	}
	
	@Override
	public void onResume(){
		super.onResume();
		slider.setProgress(50);
	}
}

package de.telekom.cldii.view.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import de.telekom.cldii.CldApplication;
import de.telekom.cldii.R;
import de.telekom.cldii.config.IActivityLifecycleListener;

/**
 * Activity that shows the splash screen for the application.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public class SplashscreenActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.setTheme(((CldApplication) getApplication()).getThemeResId());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splashscreen);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		getActivityLifecycleListener().onActivityStarted(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		getActivityLifecycleListener().onActivityStopped(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Thread timer = new Thread() {
			public void run() {
				try {
					sleep(1000);
				} catch (InterruptedException e) {
				} finally {
					Intent intent = new Intent(SplashscreenActivity.this, CheckTextToSpeechActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
					startActivity(intent);
				}
			}
		};
		timer.start(); 
	}
	
	private IActivityLifecycleListener getActivityLifecycleListener() {
		return (IActivityLifecycleListener) getApplication();
	}
}

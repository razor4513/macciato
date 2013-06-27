package de.telekom.cldii;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.fsck.k9.K9;
import com.fsck.k9.Preferences;
import com.fsck.k9.mail.internet.BinaryTempFileBody;

import de.telekom.cldii.config.CldApplicationConfiguration;
import de.telekom.cldii.config.CldApplicationPreferences;
import de.telekom.cldii.config.IActivityLifecycleListener;
import de.telekom.cldii.config.IApplicationConfiguration;
import de.telekom.cldii.config.IApplicationPreferences;
import de.telekom.cldii.config.IConfigurationManager;
import de.telekom.cldii.config.IPreferenceManager;
import de.telekom.cldii.config.ISchedulerManager;
import de.telekom.cldii.config.ITextToSpeechManager;
import de.telekom.cldii.controller.IControllerManager;
import de.telekom.cldii.controller.mail.IMailController;
import de.telekom.cldii.controller.mail.impl.MailController;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.contact.IContactDataProvider;
import de.telekom.cldii.data.contact.impl.ContactDataProvider;
import de.telekom.cldii.data.mail.IMailDataProvider;
import de.telekom.cldii.data.mail.impl.MailDataProvider;
import de.telekom.cldii.data.news.INewsDataProvider;
import de.telekom.cldii.data.news.impl.InitialNewsDataFactory;
import de.telekom.cldii.data.news.impl.SQLiteNewsDataProvider;
import de.telekom.cldii.data.sms.ISmsDataProvider;
import de.telekom.cldii.data.sms.impl.SmsDataProvider;
import de.telekom.cldii.service.imagedownload.NewsImageDownloadService;
import de.telekom.cldii.service.mail.MailUpdateScheduler;
import de.telekom.cldii.service.newsupdate.NewsUpdateScheduler;
import de.telekom.cldii.service.newsupdate.NewsUpdateService;
import de.telekom.cldii.statemachine.IStateMachineManager;
import de.telekom.cldii.statemachine.StateMachine;
import de.telekom.cldii.view.util.SplashscreenActivity;

/**
 * Application class for the CLD2 project. All data providers are lazy
 * initialized to speed up the start up of the application.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public class CldApplication extends Application implements IDataProviderManager, IConfigurationManager,
        ISchedulerManager, IPreferenceManager, IControllerManager, IActivityLifecycleListener, IStateMachineManager,
        ITextToSpeechManager, OnInitListener {

    private static final String TAG = "CldApplication";

    private INewsDataProvider newsDataProvider;
    private IMailDataProvider mailDataProvider;
    private ISmsDataProvider smsDataProvider;
    private IContactDataProvider contactDataProvider;
    private IApplicationConfiguration applicationConfiguration = new CldApplicationConfiguration();
    private NewsUpdateScheduler newsUpdateScheduler;
    private MailUpdateScheduler mailUpdateScheduler;
    private IApplicationPreferences applicationPreferences;
    private IMailController mailController;
    private StateMachine stateMachine;
    private TextToSpeech tts;
    private int runningActivities;
    private int themeResId = R.style.Theme_CLDII;
    private boolean ttsIsInitializing;
    private boolean resumingAfterCall = false;
    private boolean startedViaAppIcon = false;
    private boolean receiversRegistered;
    private BroadcastReceiver outgoingCallBroadcastReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Outgoing call");
            resumingAfterCall = true;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        // Custom ExceptionHandler just for testing purposes! NOT for productive enviroment
        // Thread.setDefaultUncaughtExceptionHandler(new CldExceptionHandler(this));

        // init data providers
        mailDataProvider = new MailDataProvider();
        mailDataProvider.onCreate(this, this);
        contactDataProvider = new ContactDataProvider();
        contactDataProvider.onCreate(this, this);
        smsDataProvider = new SmsDataProvider();
        smsDataProvider.onCreate(this, this);
        newsDataProvider = new SQLiteNewsDataProvider();
        newsDataProvider.onCreate(this, this);
        if (!getApplicationPreferences().isInitialApplicationDataCreated()) {
            InitialNewsDataFactory.createInitialNewsData(getApplicationContext(), newsDataProvider);
            getApplicationPreferences().setInitialApplicationDataCreated(true);
        }

        stateMachine = new StateMachine(getApplicationContext(), this);

        k9Setup();
    }

    /**
     * Method called when all activities of this app are stopped. That means the
     * application is executed in the background.
     */
    private synchronized void onApplicationSwitchToBackground() {

        TelephonyManager telMan = (TelephonyManager) getApplicationContext()
                .getSystemService(Context.TELEPHONY_SERVICE);

        switch (telMan.getCallState()) {
        case TelephonyManager.CALL_STATE_IDLE:
            Log.d(TAG, "App in background because of home- or backbutton");
            break;
        case TelephonyManager.CALL_STATE_RINGING:
            Log.d(TAG, "App in background because of incoming call");
            this.resumingAfterCall = true;
            break;
        case TelephonyManager.CALL_STATE_OFFHOOK:
            Log.d(TAG, "CALL_STATE_OFFHOOK");
            this.resumingAfterCall = true;
            break;
        }

        getNewsUpdateScheduler().unscheduleNewsUpdate();
        stopService(new Intent(this, NewsImageDownloadService.class));
        stopService(new Intent(this, NewsUpdateService.class));
        
        if (receiversRegistered) {
            unregisterReceiver(outgoingCallBroadcastReceiver);
            receiversRegistered = false;
        }
    }

    /**
     * Method called when an activity of the application gets active after all
     * activities have been stopped. That means the application was executed in
     * the background and is now opened again.
     */
    private synchronized void onApplicationSwitchToForeground() {

        Log.i(TAG, "Application resumed. Resuming data providers");
        if (newsDataProvider != null) {
            newsDataProvider.onResume();
        }
        if (mailDataProvider != null) {
            mailDataProvider.onResume();
        }
        if (smsDataProvider != null) {
            smsDataProvider.onResume();
        }
        if (contactDataProvider != null) {
            contactDataProvider.onResume();
        }

        getNewsUpdateScheduler().scheduleNewsUpdate();
        getMailUpdateScheduler().scheduleMailUpdate();
        
        IntentFilter intentToReceiveFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        this.registerReceiver(outgoingCallBroadcastReceiver, intentToReceiveFilter);

    }

    @Override
    public void onLowMemory() {
        // Propagate the low memory warning to the data providers
        if (newsDataProvider != null) {
            newsDataProvider.onLowMemoryWarning();
        }
        if (mailDataProvider != null) {
            mailDataProvider.onLowMemoryWarning();
        }
        if (smsDataProvider != null) {
            smsDataProvider.onLowMemoryWarning();
        }
        if (contactDataProvider != null) {
            contactDataProvider.onLowMemoryWarning();
        }
    };

    @Override
    public void onInit(int status) {
        tts.setOnUtteranceCompletedListener(stateMachine);

        if (status == TextToSpeech.SUCCESS)
            Log.i(TAG, "TTS initialized");
        ttsIsInitializing = false;
    }

    @Override
    public INewsDataProvider getNewsDataProvider() {
        return newsDataProvider;
    }

    @Override
    public IMailDataProvider getMailDataProvider() {
        return mailDataProvider;
    }

    @Override
    public ISmsDataProvider getSmsDataProvider() {
        return smsDataProvider;
    }

    @Override
    public IContactDataProvider getContactDataProvider() {
        return contactDataProvider;
    }

    @Override
    public IApplicationConfiguration getConfiguration() {
        return applicationConfiguration;
    }

    public int getThemeResId() {
        return themeResId;
    }

    public void setThemeResId(int themeResId) {
        this.themeResId = themeResId;
    }

    @Override
    public synchronized NewsUpdateScheduler getNewsUpdateScheduler() {
        if (newsUpdateScheduler == null) {
            newsUpdateScheduler = new NewsUpdateScheduler(getApplicationContext(), this);
        }
        return newsUpdateScheduler;
    }

    @Override
    public synchronized MailUpdateScheduler getMailUpdateScheduler() {
        if (mailUpdateScheduler == null) {
            mailUpdateScheduler = new MailUpdateScheduler(getApplicationContext(), this);
        }
        return mailUpdateScheduler;
    }

    @Override
    public synchronized IApplicationPreferences getApplicationPreferences() {
        if (applicationPreferences == null) {
            applicationPreferences = new CldApplicationPreferences();
            applicationPreferences.onCreate(getApplicationContext());
        }
        return applicationPreferences;
    }

    @Override
    public synchronized IMailController getMailController() {
        if (mailController == null) {
            mailController = new MailController(this);
        }
        return mailController;
    }

    @Override
    public StateMachine getStateMachine() {
        return stateMachine;
    }

    @Override
    public void setTextToSpeechReadyForInitialization(boolean isReady) {
        Log.d(TAG, "setTextToSpeechReadyForInitialization");
        if (tts != null) {
            tts.shutdown();
            tts = null;
        }
        if (isReady) {
            ttsIsInitializing = true;
            tts = new TextToSpeech(getApplicationContext(), this);
        }
    }

    @Override
    public TextToSpeech getTextToSpeech() {
        return tts;
    }

    @Override
    public boolean ttsIsInitializing() {
        return ttsIsInitializing;
    };

    private void k9Setup() {
        K9.app = this;

        Preferences prefs = Preferences.getPreferences(this);
        K9.loadPrefs(prefs);
        /*
         * We have to give MimeMessage a temp directory because
         * File.createTempFile(String, String) doesn't work in Android and
         * MimeMessage does not have access to a Context.
         */
        BinaryTempFileBody.setTempDirectory(getCacheDir());
        /*
         * Enable background sync of messages
         */
        K9.setServicesEnabled(this);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (runningActivities == 0) {
            onApplicationSwitchToForeground();

            if (activity instanceof SplashscreenActivity) {
                // start news update service for all outdated categories
                getApplicationContext().startService(
                        NewsUpdateService.updateAllOutdatedCategoriesIntent(getApplicationContext()));

                // check for new mails
                if ((System.currentTimeMillis() - getApplicationPreferences().getMailLastUpdateMillis()) > ApplicationConstants.MAIL_MIN_LASTUPDATE) {
                    getMailController().checkMails();
                }
                this.startedViaAppIcon = true;
            }
        }

        runningActivities++;
        Log.v(TAG, "Activity " + activity.getClass().getSimpleName() + " started. " + runningActivities
                + " running activities.");

    }

    @Override
    public void onActivityStopped(Activity activity) {
        runningActivities--;
        Log.v(TAG, "Activity " + activity.getClass().getSimpleName() + " stopped. " + runningActivities
                + " running activities.");
        if (runningActivities == 0) {
            Log.i(TAG, "No active activity left. Stopping application");
            onApplicationSwitchToBackground();
        }
    }

    /**
     * Returns the status how the app is started
     * 
     * @return true if app was started via app icon, false otherwise
     */
    public boolean isStartingViaAppIcon() {
        return startedViaAppIcon;
    }

    /**
     * Resets the status of how the app is started
     */
    public void resetStartedViaAppIcon() {
        this.startedViaAppIcon = false;
    }

    /**
     * Returns the status of resuming after a call
     * 
     * @return true if app came back from an call, false otherwise
     */
    public boolean isResumingAfterCall() {
        return resumingAfterCall;
    }

    /**
     * Resets the status of the resume after call flag
     */
    public void resetResumingAfterCall() {
        this.resumingAfterCall = false;
    }
}

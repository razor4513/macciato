package de.telekom.cldii.view.main;

import static de.telekom.cldii.ApplicationConstants.CATEGORY_CLEAN_FLAG;
import static de.telekom.cldii.ApplicationConstants.CATEGORY_UPDATED;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.gesture.GestureLibraries;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import de.telekom.cldii.CldApplication;
import de.telekom.cldii.R;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.statemachine.states.StateModelMainMenu;
import de.telekom.cldii.view.AbstractActivity;
import de.telekom.cldii.view.mail.MailListActivity;
import de.telekom.cldii.view.main.adapter.MainGridCellContent;
import de.telekom.cldii.view.main.adapter.MainGridViewPagerAdapter;
import de.telekom.cldii.view.news.NewsCategoriesActivity;
import de.telekom.cldii.view.phone.PhoneFavoritesActivity;
import de.telekom.cldii.view.sms.SmsListActivity;
import de.telekom.cldii.view.tutorial.TutorialActivity;
import de.telekom.cldii.view.util.LegalNoticeActivity;
import de.telekom.cldii.widget.grid.GridViewPager;

/**
 * Main activity and entry point of CLDII
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */
public class MainActivity extends AbstractActivity {
    /**
     * TAG for Log methods
     */
    private final String TAG = "MainActivity";

    private GridViewPager gridViewPager;

    private boolean newsCategoryReceiverRegistered;

    private boolean smsReceiverRegistered;

    private boolean mailReceiverRegistered;

    private final BroadcastReceiver newsCategoryUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CATEGORY_UPDATED)) {
                gridViewPager = (GridViewPager) findViewById(R.id.gridviewpager);
                if (intent.getBooleanExtra(CATEGORY_CLEAN_FLAG, false))
                    updateGridViewPager();
                else
                    gridViewPager.increaseCounterForContentItemWithId(MainGridCellContent.OptionType.TYPE_NEWS
                            .ordinal());
            }
        }
    };

    private final BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "SMS RECEIVED");
            gridViewPager = (GridViewPager) findViewById(R.id.gridviewpager);
            gridViewPager.increaseCounterForContentItemWithId(MainGridCellContent.OptionType.TYPE_SMS.ordinal());
        }
    };

    private final BroadcastReceiver newMailReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            gridViewPager.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        setTopBarName(getString(R.string.app_name));
        hideHomeButton();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateGridViewPager();
        IntentFilter categoryUpdateIntentFilter = new IntentFilter(CATEGORY_UPDATED);
        this.registerReceiver(newsCategoryUpdateReceiver, categoryUpdateIntentFilter);
        this.newsCategoryReceiverRegistered = true;
        IntentFilter smsReceivedIntentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        this.registerReceiver(smsReceiver, smsReceivedIntentFilter);
        this.smsReceiverRegistered = true;
        IntentFilter newMailReceiveFilter = new IntentFilter(getDataProviderManager().getMailDataProvider()
                .getNewMailIntentAction());
        this.registerReceiver(newMailReceiver, newMailReceiveFilter);
        this.mailReceiverRegistered = true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (this.newsCategoryReceiverRegistered) {
            unregisterReceiver(newsCategoryUpdateReceiver);
            newsCategoryReceiverRegistered = false;
        }

        if (this.smsReceiverRegistered) {
            unregisterReceiver(smsReceiver);
            smsReceiverRegistered = false;
        }
        if (this.mailReceiverRegistered) {
            unregisterReceiver(newMailReceiver);
            mailReceiverRegistered = false;
        }
    }

    private void toggleThemeAndRestart() {
        CldApplication app = (CldApplication) getApplication();
        switch (app.getThemeResId()) {
            case R.style.Theme_CLDII :
                app.setThemeResId(R.style.Theme_CLDII_Night);
                break;
            case R.style.Theme_CLDII_Night :
                app.setThemeResId(R.style.Theme_CLDII);
                break;
        }

        finish();
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        setMenuBackground();
        menu.findItem(R.id.dayNight).setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                toggleThemeAndRestart();
                return true;
            }
        });
        menu.findItem(R.id.tutorial).setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(MainActivity.this, TutorialActivity.class));
                return true;
            }
        });
        menu.findItem(R.id.legal_notice).setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(MainActivity.this, LegalNoticeActivity.class));
                return true;
            }
        });
        return true;
    }

    private void updateGridViewPager() {
        gridViewPager = (GridViewPager) findViewById(R.id.gridviewpager);
        if (!gridViewPager.hasAdapter()) {
            OnItemClickListener onItemClickListener = new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> a, View v, int position, long id) {

                    if (MainGridCellContent.OptionType.TYPE_PHONE.ordinal() == id) {
                        phoneButtonClicked();
                    } else if (MainGridCellContent.OptionType.TYPE_SMS.ordinal() == id) {
                        smsButtonClicked();
                    } else if (MainGridCellContent.OptionType.TYPE_EMAIL.ordinal() == id) {
                        emailButtonClicked();
                    } else if (MainGridCellContent.OptionType.TYPE_NEWS.ordinal() == id) {
                        newsButtonClicked();
                    }
                }
            };

            OnItemLongClickListener onItemLongClickListener = new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    Log.d(TAG, "Long click on element.");
                    return false;
                }
            };

            gridViewPager.setAdapter(new MainGridViewPagerAdapter(MainActivity.this, getDataProviderManager(),
                    onItemClickListener, onItemLongClickListener));
        } else {
            gridViewPager.notifyDataSetChanged();
        }
    }

    @Override
    public StateModel getStateModel() {
        return new StateModelMainMenu(MainActivity.this);
    }

    // following methods are public because they are called from StateMachine
    // states
    public void phoneButtonClicked() {
        showPrompt(getString(R.string.section_phone));
        Intent newIntent = new Intent(MainActivity.this, PhoneFavoritesActivity.class);
        startActivity(newIntent);
    }

    public void smsButtonClicked() {
        showPrompt(getString(R.string.section_sms));
        Intent newIntent = new Intent(MainActivity.this, SmsListActivity.class);
        startActivity(newIntent);
    }

    public void emailButtonClicked() {
        showPrompt(getString(R.string.section_email));
        Intent newIntent = new Intent(MainActivity.this, MailListActivity.class);
        startActivity(newIntent);
    }

    public void newsButtonClicked() {
        showPrompt(getString(R.string.section_news));
        Intent newIntent = new Intent(MainActivity.this, NewsCategoriesActivity.class);
        startActivity(newIntent);
    }

    @Override
    protected void initGestureLibrary() {
        gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.cldii_gestures);
        gestureLibrary.load();

    }

    /*
     * @Override public boolean onCreateOptionsMenu(Menu menu) { this.optionMenu
     * = menu;
     * 
     * // Inflate the currently selected menu XML resource. MenuInflater
     * inflater = getMenuInflater(); inflater.inflate(R.menu.main, menu);
     * 
     * return true; }
     * 
     * @Override public boolean onOptionsItemSelected(MenuItem item) { switch
     * (item.getItemId()) { case R.id.mainpreferences: startActivity(new
     * Intent(this, MainPreferenceActivity.class));
     * 
     * break;
     * 
     * default: showPrompt(getString(R.string.notavailable)); } return true; }
     */
}
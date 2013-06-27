package de.telekom.cldii.view.sms;

import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_SMSCONTENT;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_SMSNUMBER;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.gesture.GestureLibraries;
import android.gesture.Prediction;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.telekom.cldii.ApplicationConstants;
import de.telekom.cldii.R;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.view.AbstractActivity;
import de.telekom.cldii.view.sms.adapter.SmsDialogSelectNumberAdapter;

/**
 * Activity for composing/reply a Sms
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */

public class SmsComposeActivity extends AbstractActivity {
    private static final int MAX_SMS_CONTENT_CHARS = 1000;

    private final String TAG = "SmsComposeActivity";

    private ProgressDialog sendSmsProgressDialog;
    private String SENT = "SMS_SENT";
    private String DELIVERED = "SMS_DELIVERED";
    private boolean broadcastReceiversSet = false;

    private int sendSuccessful = 0;
    private int deliverySuccessful = 0;
    private int maxBroadcasts = 0;

    private View tempAlertDialogView;

    /**
     * BroadcastReceiver for "SMS_SENT" broadcast.
     */
    BroadcastReceiver sentBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            switch (getResultCode()) {
            case Activity.RESULT_OK:
                sendSuccessful++;
                Log.d(TAG, "sendSuccessful: " + sendSuccessful);
                break;
            default:
                onSmsSendFail();
                break;
            }
        }
    };

    /**
     * BroadcastReceiver for "SMS_DELIVERED" broadcast.
     */
    BroadcastReceiver deliveredBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {

            switch (getResultCode()) {
            case Activity.RESULT_OK:
                deliverySuccessful++;
                Log.d(TAG, "deliverySuccessful: " + deliverySuccessful);
                if (sendSuccessful >= 1 && deliverySuccessful == maxBroadcasts) {
                    onSmsSendSuccess();
                }
                break;

            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_compose);
        setTopBarName(getString(R.string.section_sms));
        hideSpeakButton();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(EXTRAS_KEY_SMSNUMBER)) {
                ((EditText) findViewById(R.id.recepientEditText)).setText(extras.getString(EXTRAS_KEY_SMSNUMBER));
                findViewById(R.id.smsContentEditText).requestFocus();
            }
            if (extras.containsKey(EXTRAS_KEY_SMSCONTENT)) {
                ((EditText) findViewById(R.id.smsContentEditText)).setText("\n\n"
                        + extras.getString(EXTRAS_KEY_SMSCONTENT));
                findViewById(R.id.recepientEditText).requestFocus();
            }
        }

        initListeners();

        sendSmsProgressDialog = new ProgressDialog(SmsComposeActivity.this);
        sendSmsProgressDialog.setTitle(getString(R.string.please_wait));
        sendSmsProgressDialog.setMessage(getString(R.string.sms_progress_sending));
    }

    @Override
    protected void onResume() {
        super.onResume();
        setBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unsetBroadcastReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unsetBroadcastReceiver();
    }

    private void initListeners() {
        final View sendButtonLayout = findViewById(R.id.smsSendButtonLayout);
        sendButtonLayout.setEnabled(false);
        sendButtonLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                sendSmsProgressDialog.show();
                String phoneNumber = ((EditText) findViewById(R.id.recepientEditText)).getText().toString();
                String message = ((EditText) findViewById(R.id.smsContentEditText)).getText().toString();
                sendSMS(phoneNumber, message);

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Looper.prepare();
                            Thread.sleep(ApplicationConstants.SMS_SEND_TIMEOUT);
                            SmsComposeActivity.this.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    if (sendSuccessful >= 1 && deliverySuccessful != maxBroadcasts) {
                                        onTimeout();
                                    }
                                }
                            });

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        View cancelButtonLayout = findViewById(R.id.smsCancelButtonLayout);
        cancelButtonLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.chooseContactButton).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                pickPhoneNumber();
            }
        });

        ((EditText) findViewById(R.id.recepientEditText)).addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && PhoneNumberUtils.isGlobalPhoneNumber(s.toString()))
                    sendButtonLayout.setEnabled(true);
                else
                    sendButtonLayout.setEnabled(false);
            }
        });

        final EditText contentEditText = (EditText) findViewById(R.id.smsContentEditText);
        contentEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(MAX_SMS_CONTENT_CHARS) });
        contentEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                final TextView sendButtonTextView = (TextView) findViewById(R.id.smsSendButton);
                if (s.length() > 0) {
                    sendButtonTextView.setText(getString(R.string.sms_detail_send) + " (" + s.length() + "/"
                            + MAX_SMS_CONTENT_CHARS + ")");
                    sendButtonLayout.setEnabled(true);
                } else {
                    sendButtonTextView.setText(getString(R.string.sms_detail_send));
                    sendButtonLayout.setEnabled(false);
                }
            }
        });
    }

    @Override
    protected void initGestureLibrary() {
        gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.cldii_gestures);
        gestureLibrary.load();

    }

    @Override
    public void onGesturePerformed(Prediction prediction) {
        // no gesture mode
    }

    @Override
    public StateModel getStateModel() {
        // no gesture mode
        return null;
    }

    private void pickPhoneNumber() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View selectNumberView = new SmsDialogSelectNumber(this, getDataProviderManager());
        builder.setTitle(getString(R.string.sms_selectNumber));
        builder.setView(selectNumberView);
        builder.setPositiveButton(R.string.confirm_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String number = ((SmsDialogSelectNumberAdapter) ((ListView) tempAlertDialogView
                        .findViewById(R.id.phoneNumberList)).getAdapter()).getNumber();
                if (number != null)
                    ((EditText) findViewById(R.id.recepientEditText)).setText(number);
            }
        });
        builder.setNegativeButton(R.string.confirm_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        this.tempAlertDialogView = selectNumberView;
        builder.create().show();
    }

    private void sendSMS(String phoneNumber, String message) {
        maxBroadcasts = 0;
        sendSuccessful = 0;
        deliverySuccessful = 0;

        SmsManager sms = SmsManager.getDefault();
        ArrayList<String> smsParts = sms.divideMessage(message);
        ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> deliveredIntents = new ArrayList<PendingIntent>();
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        for (int i = 0; i < smsParts.size(); i++) {
            sentIntents.add(sentPI);
            deliveredIntents.add(deliveredPI);
            maxBroadcasts++;
            Log.d(TAG, "maxBroadcasts: " + maxBroadcasts);
        }

        sms.sendMultipartTextMessage(phoneNumber, null, smsParts, sentIntents, deliveredIntents);
    }

    private void onSmsSendSuccess() {
        sendSmsProgressDialog.dismiss();
        Toast.makeText(getBaseContext(), getString(R.string.sms_prompt_smssendsuccessful), Toast.LENGTH_LONG).show();
        Log.d(TAG, "TOAST: " + getString(R.string.sms_prompt_smssendsuccessful));
        finish();
    }

    private void onSmsSendFail() {
        sendSmsProgressDialog.dismiss();
        Toast.makeText(getBaseContext(), getString(R.string.sms_prompt_smssendfailed), Toast.LENGTH_LONG).show();
        Log.d(TAG, "TOAST: " + getString(R.string.sms_prompt_smssendfailed));
    }

    private void onTimeout() {
        sendSmsProgressDialog.dismiss();
        if (sendSuccessful >= 1 && deliverySuccessful < maxBroadcasts) {
            Toast.makeText(getBaseContext(), getString(R.string.sms_prompt_timeout), Toast.LENGTH_LONG).show();
            Log.d(TAG, "TOAST (timeout): " + getString(R.string.sms_prompt_timeout));
            finish();
        } else if (sendSuccessful < 1) {
            Toast.makeText(getBaseContext(), getString(R.string.sms_prompt_smssendfailed), Toast.LENGTH_LONG).show();
            Log.d(TAG, "TOAST (timeout): " + getString(R.string.sms_prompt_smssendfailed));
        }
    }

    private void setBroadcastReceiver() {
        if (!broadcastReceiversSet) {
            registerReceiver(sentBroadcastReceiver, new IntentFilter(SENT));
            registerReceiver(deliveredBroadcastReceiver, new IntentFilter(DELIVERED));
            broadcastReceiversSet = true;
        }

    }

    private void unsetBroadcastReceiver() {
        if (broadcastReceiversSet) {
            unregisterReceiver(sentBroadcastReceiver);
            unregisterReceiver(deliveredBroadcastReceiver);
            broadcastReceiversSet = false;
        }
    }
}

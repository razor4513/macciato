package de.telekom.cldii.view.mail;

import static de.telekom.cldii.ApplicationConstants.EXTRAS_FLAG_FORWARD;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_MAILID;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_MAILRECIPIENT;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.gesture.GestureLibraries;
import android.gesture.Prediction;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import de.telekom.cldii.R;
import de.telekom.cldii.controller.IControllerManager;
import de.telekom.cldii.controller.mail.IMailController;
import de.telekom.cldii.data.mail.ICompactMail;
import de.telekom.cldii.data.mail.IFullMail;
import de.telekom.cldii.data.mail.IMailDataProvider.IMailLoadingListener;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.view.AbstractActivity;
import de.telekom.cldii.view.mail.adapter.MailDialogSelectAddressAdapter;

/**
 * Activity for composing/reply a mail
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */

public class MailComposeActivity extends AbstractActivity implements IMailLoadingListener {
    private View tempAlertDialogView;
    private String mailId;
    private ICompactMail mail;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mail_compose);
        setTopBarName(getString(R.string.section_email));
        hideSpeakButton();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(EXTRAS_KEY_MAILID)) {
                this.mailId = extras.getString(EXTRAS_KEY_MAILID);
                this.mail = getDataProviderManager().getMailDataProvider().getCompactMail(mailId);
                if (getIntent().getFlags() == EXTRAS_FLAG_FORWARD) {
                    ((EditText) findViewById(R.id.subjectEditText)).setText(getString(R.string.mail_compose_forward)
                            + " " + mail.getSubject());
                    getDataProviderManager().getMailDataProvider().getFullMail(mailId, this);
                    findViewById(R.id.recipientEditText).requestFocus();
                } else {
                    ((EditText) findViewById(R.id.recipientEditText)).setText(mail.getFromAdress());
                    ((EditText) findViewById(R.id.subjectEditText)).setText(getString(R.string.mail_compose_reply)
                            + " " + mail.getSubject());
                    findViewById(R.id.mailContentEditText).requestFocus();
                }
            } else if (extras.containsKey(EXTRAS_KEY_MAILRECIPIENT)) {
                ((EditText) findViewById(R.id.recipientEditText)).setText(extras.getString(EXTRAS_KEY_MAILRECIPIENT));
                findViewById(R.id.subjectEditText).requestFocus();
            }
        }

        initButtonListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public IMailController getMailController() {
        return ((IControllerManager) getApplication()).getMailController();
    }

    private void initButtonListeners() {
        final View sendButtonLayout = findViewById(R.id.mailSendButtonLayout);
        sendButtonLayout.setEnabled(false);
        sendButtonLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String recipient = ((EditText) findViewById(R.id.recipientEditText)).getText().toString();
                if (recipient.length() > 0) {
                    String subject = ((EditText) findViewById(R.id.subjectEditText)).getText().toString();
                    String text = ((EditText) findViewById(R.id.mailContentEditText)).getText().toString();

                    String cc = ((EditText) findViewById(R.id.bccEditText)).getText().toString();
                    List<String> ccList = null;
                    if (cc != null && cc.length() > 0) {
                        StringTokenizer tokenizer = new StringTokenizer(cc, ", ");
                        if (tokenizer.countTokens() > 0) {
                            ccList = new ArrayList<String>(tokenizer.countTokens());
                            while (tokenizer.hasMoreTokens()) {
                                String debugString = tokenizer.nextToken();
                                ccList.add(debugString);
                            }
                        }
                    }
                    if (getIntent().getFlags() == EXTRAS_FLAG_FORWARD) {
                        if (mailId != null) {
                            getMailController().sendMail(recipient, ccList, null, subject, text, mailId);
                        } else {
                            getMailController().sendMail(recipient, ccList, null, subject, text);
                        }
                        getMailController().changeAnsweredState(mail, true);
                    } else {
                        getMailController().sendMail(recipient, ccList, null, subject, text);
                        getMailController().changeAnsweredState(mail, true);
                    }
                }

                finish();
            }
        });

        View cancelButtonLayout = findViewById(R.id.mailCancelButtonLayout);
        cancelButtonLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.chooseRecipientButton).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                pickMailAddress(R.id.recipientEditText);
            }
        });

        findViewById(R.id.chooseBccButton).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                pickMailAddress(R.id.bccEditText);
            }
        });

        ((EditText) findViewById(R.id.recipientEditText)).addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0)
                    sendButtonLayout.setEnabled(true);
                else
                    sendButtonLayout.setEnabled(false);
            }
        });
        ((EditText) findViewById(R.id.mailContentEditText)).addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0)
                    sendButtonLayout.setEnabled(true);
                else
                    sendButtonLayout.setEnabled(false);
            }
        });

    }

    private void pickMailAddress(final int editTextId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View selectNumberView = new MailDialogSelectAddress(this, getDataProviderManager());
        builder.setTitle(getString(R.string.sms_selectNumber));
        builder.setView(selectNumberView);
        builder.setPositiveButton(R.string.confirm_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String address = ((MailDialogSelectAddressAdapter) ((ListView) tempAlertDialogView
                        .findViewById(R.id.phoneNumberList)).getAdapter()).getEmail();
                if (address != null)
                    ((EditText) findViewById(editTextId)).setText(address);
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

    @Override
    public void onMailLoaded(IFullMail loadedMail) {
        String mailText = loadedMail.getText();
        if (mailText.length() > 0)
            ((EditText) findViewById(R.id.mailContentEditText)).setText(Html.fromHtml(mailText));
    }

}

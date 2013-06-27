package de.telekom.cldii.statemachine.states;

import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_CALL;
import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_SWIPE_LEFTTORIGHT;
import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_SWIPE_RIGHTTOLEFT;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_MANUAL_LIST_BEGIN;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_MANUAL_LIST_END;
import static de.telekom.cldii.statemachine.StateMachineConstants.SECTION2EMAILLOOP;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_ASK_READLISTAGAIN;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_ASK_WANTGOTOMAINMENU;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_GOTO_MAINMENU;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_NO_ITEMS;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_OPTIONSMENU;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_OPTIONSMENU_BACKTOLIST;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_OPTIONSMENU_CALL;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_OPTIONSMENU_MAINMENU;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_OPTIONSMENU_NEXT;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_OPTIONSMENU_PREVIOUS;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_SLEEP;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_START;
import static de.telekom.cldii.statemachine.StateMachineConstants.TIME_TO_WAIT_BIG;
import static de.telekom.cldii.statemachine.StateMachineConstants.TIME_TO_WAIT_DONT_WAIT;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.widget.Toast;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.contact.Contact;
import de.telekom.cldii.data.mail.ICompactMail;
import de.telekom.cldii.data.mail.IFullMail;
import de.telekom.cldii.data.mail.IMailDataProvider.IMailLoadingListener;
import de.telekom.cldii.data.mail.impl.ReceivedDateComparator;
import de.telekom.cldii.statemachine.StateContent;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.view.mail.MailDetailsActivity;

public class StateModelMailDetails extends StateModel {

    /**
     * TAG for Log methods
     */
    protected static final String TAG = "StateModelEmailDetails";
    private ICompactMail currentItem;
    private String currentItemBeforeOptionsMenuId;

    public String getCurrentItemBeforeOptionsMenuId() {
        return currentItemBeforeOptionsMenuId;
    }

    private Contact currentContact = null;

    public StateModelMailDetails(final Context context, final IDataProviderManager dataProviderManager) {
        super(context);
        StateContent stateContent;
        final MailDetailsActivity globalModelContext = (MailDetailsActivity) context;

        final List<ICompactMail> itemsList = new ArrayList<ICompactMail>(globalModelContext.getDataProviderManager()
                .getMailDataProvider().getIncommingMails(new ReceivedDateComparator()));

        // STATE START -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlayMouth });
                int attributeResourceId = a.getResourceId(0, 0);
                setGestureOverlayIconId(attributeResourceId);
                a.recycle();
                super.executeInState();

                // Landmarking prompt
                getStateMachine().speakAndNextState(
                        context.getString(R.string.tts_email) + " " + context.getString(R.string.tts_detailview),
                        getStateId(), TIME_TO_WAIT_BIG);
            }
        };
        if (itemsList.size() == 0) {
            stateContent.setNextId(STATE_NO_ITEMS);
        } else {
            stateContent.setNextId(SECTION2EMAILLOOP + itemsList.get(globalModelContext.getCurrentIndex()).getId());
        }

        this.addStateContent(STATE_START, stateContent);

        // STATE SECTION2TOX LOOP-------------
        for (final ICompactMail item : itemsList) {

            stateContent = new StateContent(context) {

                @Override
                public void reactOnTap() {
                    super.reactOnTap();

                    String tempNextId = getNextId();
                    setNextId(STATE_OPTIONSMENU);
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                }

                @Override
                public void reactOnGesture(String gestureName) {
                    super.reactOnGesture(gestureName);

                    int currentIndex = itemsList.indexOf(item);

                    if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {
                        // PREVIOUS NEWS
                        if (currentIndex == 0) {
                            String tempNextId = getNextId();
                            setNextId(STATE_MANUAL_LIST_BEGIN);
                            getStateMachine().nextState(getStateId(), false);
                            setNextId(tempNextId);
                        } else {
                            if (currentIndex > 0) {
                                String tempNextId = getNextId();
                                setNextId(SECTION2EMAILLOOP + itemsList.get(currentIndex - 1).getId());
                                getStateMachine().nextState(getStateId(), false);
                                setNextId(tempNextId);
                            }
                        }
                    } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                        // NEXT NEWS
                        if (currentIndex != itemsList.size() - 1) {
                            getStateMachine().nextState(getStateId(), false);
                        } else {
                            String tempNextId = getNextId();
                            setNextId(STATE_MANUAL_LIST_END);
                            getStateMachine().nextState(getStateId(), false);
                            setNextId(tempNextId);
                        }
                    } else if (gestureName.equals(GESTURE_CALL)) {
                        Contact tempContact = globalModelContext.getDataProviderManager().getContactDataProvider()
                                .getContactForEmail(item.getFromAdress());
                        if (tempContact != null) {
                            String phoneNumber = globalModelContext.getDataProviderManager().getContactDataProvider()
                                    .getDefaultPhoneForContact(tempContact);
                            if (phoneNumber == null || phoneNumber.length() == 0
                                    || !PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
                                Toast.makeText(context, context.getString(R.string.tts_no_phonenumber),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                callPhoneNumber(phoneNumber);
                            }
                        } else {
                            Toast.makeText(context, context.getString(R.string.tts_no_phonenumber), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                }

                @Override
                public void executeInState() {
                    TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlayMouth });
                    int attributeResourceId = a.getResourceId(0, 0);
                    setGestureOverlayIconId(attributeResourceId);
                    a.recycle();
                    super.executeInState();

                    globalModelContext.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            globalModelContext.getDetailMailItem(item.getId());
                        }
                    });

                    currentItem = item;

                    globalModelContext.getDataProviderManager().getMailDataProvider()
                            .getFullMail(item.getId(), new IMailLoadingListener() {

                                @Override
                                public void onMailLoaded(IFullMail loadedMail) {
                                    String senderName = loadedMail.getFromAdress();
                                    Contact contact = globalModelContext.getDataProviderManager()
                                            .getContactDataProvider().getContactForEmail(senderName);
                                    if (contact != null) {
                                        senderName = contact.getDisplayName();
                                    }

                                    // Filter clean text for tts
                                    Document doc = Jsoup.parse(loadedMail.getText());
                                    String readText = doc.text();
                                    getStateMachine().speakAndNextState(
                                            context.getString(R.string.tts_from) + " " + senderName + ": "
                                                    + loadedMail.getSubject() + ": " + readText, getStateId(),
                                            TIME_TO_WAIT_BIG);
                                }
                            });

                }

            };
            if (itemsList.get(itemsList.size() - 1) == item) {
                stateContent.setNextId(STATE_MANUAL_LIST_END);
            } else {
                stateContent.setNextId(SECTION2EMAILLOOP + itemsList.get(itemsList.indexOf(item) + 1).getId());
            }
            this.addStateContent(SECTION2EMAILLOOP + item.getId(), stateContent);
        }

        // STATES OPTIONSMENU -------------
        // Optionsmenu landmark
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();
                setNextId(STATE_OPTIONSMENU_CALL);

                currentItemBeforeOptionsMenuId = SECTION2EMAILLOOP + currentItem.getId();

                if (currentItem != null) {
                    currentContact = globalModelContext.getDataProviderManager().getContactDataProvider()
                            .getContactForEmail(currentItem.getFromAdress());
                }

                if (currentContact == null
                        || globalModelContext.getDataProviderManager().getContactDataProvider()
                                .getDefaultPhoneForContact(currentContact) == null) {
                    setNextId(STATE_OPTIONSMENU_NEXT);
                }
                getStateMachine().speakAndNextState(context.getString(R.string.tts_optionsmenu), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId(STATE_OPTIONSMENU_CALL);
        this.addStateContent(STATE_OPTIONSMENU, stateContent);

        // Call
        stateContent = new StateContent(context) {

            @Override
            public void reactOnTap() {
                super.reactOnTap();
                if (currentContact != null
                        && globalModelContext.getDataProviderManager().getContactDataProvider()
                                .getDefaultPhoneForContact(currentContact) != null) {
                    String number = globalModelContext.getDataProviderManager().getContactDataProvider()
                            .getDefaultPhoneForContact(currentContact);
                    callPhoneNumber(number);
                }
            }

            @Override
            public void executeInState() {
                super.executeInState();

                getStateMachine().speakAndNextState(context.getString(R.string.tts_optionsmenu_call), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId(STATE_OPTIONSMENU_NEXT);
        this.addStateContent(STATE_OPTIONSMENU_CALL, stateContent);

        // Next
        stateContent = new StateContent(context) {

            @Override
            public void reactOnTap() {
                super.reactOnTap();

                int currentIndex = itemsList.indexOf(currentItem);
                Log.d(TAG, "currentIndex:" + currentIndex);

                if (currentIndex != itemsList.size() - 1) {
                    String tempNextId = getNextId();
                    setNextId(SECTION2EMAILLOOP + itemsList.get(currentIndex + 1).getId());
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                } else {
                    if (itemsList.size() > 0) {
                        String tempNextId = getNextId();
                        setNextId(SECTION2EMAILLOOP + itemsList.get(0).getId());
                        getStateMachine().nextState(getStateId(), false);
                        setNextId(tempNextId);
                    }
                }
            }

            @Override
            public void executeInState() {
                super.executeInState();

                getStateMachine().speakAndNextState(context.getString(R.string.tts_optionsmenu_next), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId(STATE_OPTIONSMENU_PREVIOUS);
        this.addStateContent(STATE_OPTIONSMENU_NEXT, stateContent);

        // Previous
        stateContent = new StateContent(context) {

            @Override
            public void reactOnTap() {
                super.reactOnTap();

                int currentIndex = itemsList.indexOf(currentItem);
                Log.d(TAG, "currentIndex:" + currentIndex);

                if (currentIndex == 0) {
                    String tempNextId = getNextId();
                    setNextId(SECTION2EMAILLOOP + itemsList.get(itemsList.size() - 1).getId());
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                } else {
                    if (currentIndex > 0) {
                        String tempNextId = getNextId();
                        setNextId(SECTION2EMAILLOOP + itemsList.get(currentIndex - 1).getId());
                        getStateMachine().nextState(getStateId(), false);
                        setNextId(tempNextId);
                    }
                }
            }

            @Override
            public void executeInState() {
                super.executeInState();

                getStateMachine().speakAndNextState(context.getString(R.string.tts_optionsmenu_previous), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId(STATE_OPTIONSMENU_BACKTOLIST);
        this.addStateContent(STATE_OPTIONSMENU_PREVIOUS, stateContent);

        // Back to mail list
        stateContent = new StateContent(context) {

            @Override
            public void reactOnTap() {
                super.reactOnTap();
                ((Activity) context).finish();
            }

            @Override
            public void executeInState() {
                super.executeInState();

                getStateMachine().speakAndNextState(context.getString(R.string.tts_optionsmenu_backtomaillist),
                        getStateId(), TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId(STATE_OPTIONSMENU_MAINMENU);
        this.addStateContent(STATE_OPTIONSMENU_BACKTOLIST, stateContent);

        // main menu
        stateContent = new StateContent(context) {

            @Override
            public void reactOnTap() {
                super.reactOnTap();

                stateMachine.goBackToMainMenu();
            }

            @Override
            public void executeInState() {
                super.executeInState();
                setNextId(SECTION2EMAILLOOP + currentItem.getId());

                getStateMachine().speakAndNextState(context.getString(R.string.tts_optionsmenu_mainmenu), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        this.addStateContent(STATE_OPTIONSMENU_MAINMENU, stateContent);

        // STATE NO_NEWS -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();
                getStateMachine().speakAndNextState(context.getString(R.string.tts_no_email), getStateId(),
                        TIME_TO_WAIT_DONT_WAIT);
            }
        };
        stateContent.setNextId(STATE_GOTO_MAINMENU);
        addStateContent(STATE_NO_ITEMS, stateContent);

        // STATE STATE_GOTO_MAINMENU -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();
                stateMachine.goBackToMainMenu();
            }
        };
        addStateContent(STATE_GOTO_MAINMENU, stateContent);

        // STATE STATE_SLEEP -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlayMouthCrossed });
                int attributeResourceId = a.getResourceId(0, 0);
                setGestureOverlayIconId(attributeResourceId);
                a.recycle();
                super.executeInState();
            }

            @Override
            public void reactOnTap() {
                super.reactOnTap();
                getStateMachine().resetRepeatCounter();
                if (getStateMachine().getPreSleepStateId() != STATE_START) {
                    getStateMachine().getStateModel().getStateMap().get(STATE_START)
                            .setNextId(getStateMachine().getPreSleepStateId());
                } else {
                    if (itemsList.size() != 0) {
                        getStateMachine().getStateModel().getStateMap().get(STATE_START)
                                .setNextId(SECTION2EMAILLOOP + itemsList.get(0).getId());
                    }
                }

                getStateMachine().start();
            }

        };
        stateContent.setGestureOverlayStringId(R.string.gesture_overlay_silence);
        addStateContent(STATE_SLEEP, stateContent);

        // STATE MANUAL_LIST_BEGIN -------------
        stateContent = new StateContent(context) {

            @Override
            public void reactOnGesture(String gestureName) {
                super.reactOnGesture(gestureName);

                if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {

                    String tempNextId = getNextId();
                    setNextId(STATE_MANUAL_LIST_END);
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);

                } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                    getStateMachine().nextState(getStateId(), false);
                }
            }

            @Override
            public void executeInState() {
                super.executeInState();

                getStateMachine().speakAndNextState(context.getString(R.string.tts_listbegin), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };

        if (itemsList != null || itemsList.size() != 0) {
            stateContent.setNextId(SECTION2EMAILLOOP + itemsList.get(0).getId());
        }

        TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlayMouth });
        int attributeResourceId = a.getResourceId(0, 0);
        stateContent.setGestureOverlayIconId(attributeResourceId);
        a.recycle();

        this.addStateContent(STATE_MANUAL_LIST_BEGIN, stateContent);

        // STATE MANUAL_LIST_END -------------
        stateContent = new StateContent(context) {

            @Override
            public void reactOnGesture(String gestureName) {
                super.reactOnGesture(gestureName);

                if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {

                    if (itemsList != null || itemsList.size() != 0) {
                        String tempNextId = getNextId();
                        setNextId(SECTION2EMAILLOOP + itemsList.get(itemsList.size() - 1).getId());
                        getStateMachine().nextState(getStateId(), false);
                        setNextId(tempNextId);
                    }

                } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                    String tempNextId = getNextId();
                    setNextId(STATE_MANUAL_LIST_BEGIN);
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                }
            }

            @Override
            public void executeInState() {
                super.executeInState();

                getStateMachine().speakAndNextState(context.getString(R.string.tts_listend), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId(STATE_ASK_WANTGOTOMAINMENU);

        a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlayMouth });
        attributeResourceId = a.getResourceId(0, 0);
        stateContent.setGestureOverlayIconId(attributeResourceId);
        a.recycle();

        this.addStateContent(STATE_MANUAL_LIST_END, stateContent);

        // STATE STATE_ASK_WANTGOTOMAINMENU -------------
        stateContent = new StateContent(context) {

            @Override
            public void reactOnTap() {
                super.reactOnTap();
                stateMachine.goBackToMainMenu();
            }

            @Override
            public void executeInState() {
                super.executeInState();

                getStateMachine().speakAndNextState(context.getString(R.string.tts_backtomainmenu), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId(STATE_ASK_READLISTAGAIN);
        this.addStateContent(STATE_ASK_WANTGOTOMAINMENU, stateContent);

        // STATE STATE_ASK_READLISTAGAIN -------------
        stateContent = new StateContent(context) {

            @Override
            public void reactOnTap() {
                super.reactOnTap();
                getStateMachine().resetRepeatCounter();
                getStateMachine().start();
            }

            @Override
            public void executeInState() {
                super.executeInState();

                getStateMachine().speakAndNextState(context.getString(R.string.tts_orshallireadthelistagain),
                        getStateId(), TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId(STATE_SLEEP);
        this.addStateContent(STATE_ASK_READLISTAGAIN, stateContent);

        // STATE STATE_SLEEP -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlayMouthCrossed });
                int attributeResourceId = a.getResourceId(0, 0);
                setGestureOverlayIconId(attributeResourceId);
                a.recycle();
                super.executeInState();
            }

            @Override
            public void reactOnTap() {
                super.reactOnTap();
                getStateMachine().nextState(getStateId(), false);
            }
        };
        stateContent.setGestureOverlayStringId(R.string.gesture_overlay_silence);
        stateContent.setNextId(STATE_ASK_WANTGOTOMAINMENU);
        addStateContent(STATE_SLEEP, stateContent);
    }

    private void callPhoneNumber(String phoneNumber) {
        if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
            context.startActivity(callIntent);
        }
    }

}

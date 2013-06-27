package de.telekom.cldii.statemachine.states;

import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_CALL;
import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_SWIPE_LEFTTORIGHT;
import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_SWIPE_RIGHTTOLEFT;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_MANUAL_LIST_BEGIN;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_MANUAL_LIST_END;
import static de.telekom.cldii.statemachine.StateMachineConstants.SECTION2SMSLOOP;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_ASK_READLISTAGAIN;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_ASK_WANTGOTOMAINMENU;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_GOTO_MAINMENU;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_LISTEND;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_NO_ITEMS;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_OPTIONSMENU;
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
import de.telekom.cldii.data.sms.SmsItem;
import de.telekom.cldii.statemachine.StateContent;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.util.SignalTonePlayer;
import de.telekom.cldii.view.AbstractActivity;
import de.telekom.cldii.view.sms.SmsDetailsActivity;

public class StateModelSms extends StateModel {

    /**
     * TAG for Log methods
     */
    protected static final String TAG = "StateModelSmsList";

    private SmsItem currentItem;
    private String currentItemBeforeOptionsMenuId;

    public String getCurrentItemBeforeOptionsMenuId() {
        return currentItemBeforeOptionsMenuId;
    }

    private Context context;
    private SmsModelType type;

    public static enum SmsModelType {
        LIST, DETAIL
    }

    public StateModelSms(final Context context, final IDataProviderManager dataProviderManager, final SmsModelType type) {
        super(context);
        this.context = context;
        this.type = type;

        final AbstractActivity globalModelContext = (AbstractActivity) context;

        fillStateContents(context, dataProviderManager, type, globalModelContext);

    }

    private void fillStateContents(final Context context, final IDataProviderManager dataProviderManager,
            final SmsModelType type, final AbstractActivity globalModelContext) {
        StateContent stateContent;
        final List<SmsItem> smsItemsList = new ArrayList<SmsItem>(globalModelContext.getDataProviderManager()
                .getSmsDataProvider().getSmsItemsOrderedByDate());

        // STATE START -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();

                // Landmarking prompt
                getStateMachine()
                        .speakAndNextState(context.getString(R.string.tts_sms), getStateId(), TIME_TO_WAIT_BIG);
            }
        };

        if (smsItemsList == null || smsItemsList.size() == 0) {
            stateContent.setNextId(STATE_NO_ITEMS);
        } else {
            // we have to check for type because we are
            // using the same model for list and details
            if (this.type == SmsModelType.DETAIL) {
                stateContent.setNextId(SECTION2SMSLOOP
                        + smsItemsList.get(((SmsDetailsActivity) globalModelContext).getCurrentSmsIndex()).getSmsId());
            } else {
                if (currentItem != null) {
                    stateContent.setNextId(SECTION2SMSLOOP + currentItem.getSmsId());
                } else {
                    stateContent.setNextId(SECTION2SMSLOOP + smsItemsList.get(0).getSmsId());
                }
            }
        }

        if (type == SmsModelType.DETAIL) {
            TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlayMouth });
            int attributeResourceId = a.getResourceId(0, 0);
            stateContent.setGestureOverlayIconId(attributeResourceId);
            a.recycle();
        }

        this.addStateContent(STATE_START, stateContent);

        for (final SmsItem smsItem : smsItemsList) {

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

                    // we have to check for type because we are
                    // using the same model for list and details
                    if (type == SmsModelType.DETAIL) {
                        ((SmsDetailsActivity) context).getDetailSmsItem(smsItem.getSmsId());
                    }

                    int currentIndex = smsItemsList.indexOf(smsItem);
                    Log.d(TAG, "currentIndex:" + currentIndex);

                    if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {
                        // PREVIOUS SMS
                        if (currentIndex == 0) {
                            String tempNextId = getNextId();
                            setNextId(STATE_MANUAL_LIST_BEGIN);
                            getStateMachine().nextState(getStateId(), false);
                            setNextId(tempNextId);
                        } else {
                            if (currentIndex > 0) {
                                String tempNextId = getNextId();
                                setNextId(SECTION2SMSLOOP + smsItemsList.get(currentIndex - 1).getSmsId());
                                getStateMachine().nextState(getStateId(), false);
                                setNextId(tempNextId);
                            }
                        }
                    } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                        // NEXT SMS
                        if (currentIndex != smsItemsList.size() - 1) {
                            getStateMachine().nextState(getStateId(), false);
                        } else {
                            String tempNextId = getNextId();
                            setNextId(STATE_MANUAL_LIST_END);
                            getStateMachine().nextState(getStateId(), false);
                            setNextId(tempNextId);
                        }
                    } else if (gestureName.equals(GESTURE_CALL)) {
                        String phoneNumber = smsItem.getSenderPhoneNumber();

                        if (phoneNumber.length() == 0 || !PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
                            Toast.makeText(context, context.getString(R.string.tts_no_phonenumber), Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            callPhoneNumber(phoneNumber);
                        }
                    }
                }

                @Override
                public void executeInState() {
                    super.executeInState();

                    currentItem = smsItem;

                    if (type == SmsModelType.DETAIL) {
                        globalModelContext.runOnUiThread(new Runnable() {
                            public void run() {
                                ((SmsDetailsActivity) globalModelContext).getDetailSmsItem(smsItem.getSmsId());
                            }
                        });
                    }

                    Contact contact = dataProviderManager.getContactDataProvider().getContactForPhoneNumber(
                            smsItem.getSenderPhoneNumber());
                    String contactName = null;
                    if (contact != null) {
                        contactName = contact.getDisplayName();
                    }
                    if (contactName == null) {
                        contactName = smsItem.getSenderPhoneNumber();
                        if (contactName.length() > 0 && PhoneNumberUtils.isDialable(contactName.toCharArray()[0])) {
                            contactName = dividePhoneNumberForReading(contactName);
                        }
                    }
                    getStateMachine().speakAndNextState(
                            context.getString(R.string.tts_from) + " " + contactName + ": " + smsItem.getContent(),
                            getStateId(), TIME_TO_WAIT_BIG);
                }

                /**
                 * Inserts space char after every char.
                 * 
                 * @param phoneNumber
                 * @return string divided by spaces
                 */
                private String dividePhoneNumberForReading(String phoneNumber) {
                    return phoneNumber.replaceAll(".(?!$)", "$0 ");
                }

            };
            if (smsItemsList.get(smsItemsList.size() - 1) == smsItem) {
                stateContent.setNextId(STATE_MANUAL_LIST_END);
            } else {
                stateContent
                        .setNextId(SECTION2SMSLOOP + smsItemsList.get(smsItemsList.indexOf(smsItem) + 1).getSmsId());
            }

            if (type == SmsModelType.DETAIL) {
                TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlayMouth });
                int attributeResourceId = a.getResourceId(0, 0);
                stateContent.setGestureOverlayIconId(attributeResourceId);
                a.recycle();
            }

            this.addStateContent(SECTION2SMSLOOP + smsItem.getSmsId(), stateContent);
        }

        // STATES OPTIONSMENU -------------
        // Optionsmenu landmark
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();
                setNextId(STATE_OPTIONSMENU_CALL);

                currentItemBeforeOptionsMenuId = SECTION2SMSLOOP + currentItem.getSmsId();

                String phoneNumber = currentItem.getSenderPhoneNumber();

                if (!PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
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
                String phoneNumber = currentItem.getSenderPhoneNumber();
                callPhoneNumber(phoneNumber);
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

                int currentIndex = smsItemsList.indexOf(currentItem);
                Log.d(TAG, "currentIndex:" + currentIndex);

                if (currentIndex != smsItemsList.size() - 1) {
                    String tempNextId = getNextId();
                    setNextId(SECTION2SMSLOOP + smsItemsList.get(currentIndex + 1).getSmsId());
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                } else {
                    if (smsItemsList.size() > 0) {
                        String tempNextId = getNextId();
                        setNextId(SECTION2SMSLOOP + smsItemsList.get(0).getSmsId());
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

                int currentIndex = smsItemsList.indexOf(currentItem);
                Log.d(TAG, "currentIndex:" + currentIndex);

                if (currentIndex == 0) {
                    String tempNextId = getNextId();
                    setNextId(SECTION2SMSLOOP + smsItemsList.get(smsItemsList.size() - 1).getSmsId());
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                } else {
                    if (currentIndex > 0) {
                        String tempNextId = getNextId();
                        setNextId(SECTION2SMSLOOP + smsItemsList.get(currentIndex - 1).getSmsId());
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
        stateContent.setNextId(STATE_OPTIONSMENU_MAINMENU);
        this.addStateContent(STATE_OPTIONSMENU_PREVIOUS, stateContent);

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
                setNextId(SECTION2SMSLOOP + currentItem.getSmsId());

                getStateMachine().speakAndNextState(context.getString(R.string.tts_optionsmenu_mainmenu), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        this.addStateContent(STATE_OPTIONSMENU_MAINMENU, stateContent);

        // STATE STATE_LISTEND -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();

                if (getStateMachine().getRepeatCounter() == 0) {

                    // build new model (maybe there is a new sms while reading
                    // the list the first time)
                    StateModelSms.this.getStateMap().clear();
                    fillStateContents(context, dataProviderManager, type, globalModelContext);

                    if (smsItemsList.size() != 0) {
                        setNextId(SECTION2SMSLOOP + smsItemsList.get(0).getSmsId());
                    }
                    getStateMachine().increaseRepeatCounter();
                } else {
                    this.setNextId(STATE_SLEEP);
                }

                getStateMachine().speakAndNextState(SignalTonePlayer.SIGNAL_TONE_LISTEND, getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        addStateContent(STATE_LISTEND, stateContent);

        // STATE NO_NEWS -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();
                getStateMachine().speakAndNextState(context.getString(R.string.sms_no_sms), getStateId(),
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

        if (smsItemsList != null || smsItemsList.size() != 0) {
            stateContent.setNextId(SECTION2SMSLOOP + smsItemsList.get(0).getSmsId());
        }

        if (type == SmsModelType.DETAIL) {
            TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlayMouth });
            int attributeResourceId = a.getResourceId(0, 0);
            stateContent.setGestureOverlayIconId(attributeResourceId);
            a.recycle();
        }

        this.addStateContent(STATE_MANUAL_LIST_BEGIN, stateContent);

        // STATE MANUAL_LIST_END -------------
        stateContent = new StateContent(context) {

            @Override
            public void reactOnGesture(String gestureName) {
                super.reactOnGesture(gestureName);

                if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {

                    if (smsItemsList != null || smsItemsList.size() != 0) {
                        String tempNextId = getNextId();
                        setNextId(SECTION2SMSLOOP + smsItemsList.get(smsItemsList.size() - 1).getSmsId());
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

        if (type == SmsModelType.DETAIL) {
            TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlayMouth });
            int attributeResourceId = a.getResourceId(0, 0);
            stateContent.setGestureOverlayIconId(attributeResourceId);
            a.recycle();
        }

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

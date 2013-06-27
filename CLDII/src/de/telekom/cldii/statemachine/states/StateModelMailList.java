package de.telekom.cldii.statemachine.states;

import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_MAILID;
import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_SWIPE_LEFTTORIGHT;
import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_SWIPE_RIGHTTOLEFT;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_MANUAL_LIST_BEGIN;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_MANUAL_LIST_END;
import static de.telekom.cldii.statemachine.StateMachineConstants.SECTION2EMAILLOOP;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_ASK_READLISTAGAIN;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_ASK_WANTGOTOMAINMENU;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_GOTO_MAINMENU;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_NO_ITEMS;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_SLEEP;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_START;
import static de.telekom.cldii.statemachine.StateMachineConstants.TIME_TO_WAIT_BIG;
import static de.telekom.cldii.statemachine.StateMachineConstants.TIME_TO_WAIT_DONT_WAIT;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.Log;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.contact.Contact;
import de.telekom.cldii.data.mail.ICompactMail;
import de.telekom.cldii.data.mail.impl.ReceivedDateComparator;
import de.telekom.cldii.statemachine.StateContent;
import de.telekom.cldii.statemachine.StateModel;

import de.telekom.cldii.view.mail.MailDetailsActivity;
import de.telekom.cldii.view.mail.MailListActivity;

public class StateModelMailList extends StateModel {

    /**
     * TAG for Log methods
     */
    protected static final String TAG = "StateModelMailList";

    public StateModelMailList(final Context context, final IDataProviderManager dataProviderManager) {
        super(context);
        StateContent stateContent;
        final MailListActivity globalModelContext = (MailListActivity) context;

        final List<ICompactMail> itemsList = new ArrayList<ICompactMail>(globalModelContext.getDataProviderManager()
                .getMailDataProvider().getIncommingMails(new ReceivedDateComparator()));

        // STATE START -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();

                // Landmarking prompt
                getStateMachine().speakAndNextState(context.getString(R.string.tts_email), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };

        if (itemsList == null || itemsList.size() == 0) {
            stateContent.setNextId(STATE_NO_ITEMS);
        } else {
            stateContent.setNextId(SECTION2EMAILLOOP + itemsList.get(0).getId());
        }

        this.addStateContent(STATE_START, stateContent);

        // STATE SECTION2TOX LOOP-------------
        if (itemsList != null) {
            for (final ICompactMail newsItem : itemsList) {
                final String subject = newsItem.getSubject();

                stateContent = new StateContent(context) {

                    @Override
                    public void reactOnTap() {
                        super.reactOnTap();

                        Intent newsDetails = new Intent(context, MailDetailsActivity.class);
                        newsDetails.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        newsDetails.putExtra(EXTRAS_KEY_MAILID, newsItem.getId());
                        context.startActivity(newsDetails);
                    }

                    @Override
                    public void reactOnGesture(String gestureName) {
                        super.reactOnGesture(gestureName);

                        int currentIndex = itemsList.indexOf(newsItem);
                        Log.d(TAG, "currentIndex:" + currentIndex);

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
                                getStateMachine().nextState(getNextId(), true);
                            } else {
                                if (currentIndex > 0) {
                                    String tempNextId = getNextId();
                                    setNextId(STATE_MANUAL_LIST_END);
                                    getStateMachine().nextState(getStateId(), false);
                                    setNextId(tempNextId);
                                }
                            }
                        }
                    }

                    @Override
                    public void executeInState() {
                        TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlaySpeechBalloon });
                        int attributeResourceId = a.getResourceId(0, 0);
                        setGestureOverlayIconId(attributeResourceId);
                        a.recycle();
                        super.executeInState();

                        String senderName = newsItem.getFromAdress();
                        Contact contact = globalModelContext.getDataProviderManager().getContactDataProvider()
                                .getContactForEmail(senderName);
                        if (contact != null) {
                            senderName = contact.getDisplayName();
                        }

                        getStateMachine().speakAndNextState(
                                context.getString(R.string.tts_from) + " " + senderName + ": " + subject, getStateId(),
                                TIME_TO_WAIT_BIG);
                    }

                };

                if (itemsList.get(itemsList.size() - 1) == newsItem) {
                    stateContent.setNextId(STATE_MANUAL_LIST_END);
                } else {
                    stateContent.setNextId(SECTION2EMAILLOOP + itemsList.get(itemsList.indexOf(newsItem) + 1).getId());
                }
                this.addStateContent(SECTION2EMAILLOOP + newsItem.getId(), stateContent);
            }
        }

        // STATE NO_MAILS -------------
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
}

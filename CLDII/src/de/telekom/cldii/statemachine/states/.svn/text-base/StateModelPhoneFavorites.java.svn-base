package de.telekom.cldii.statemachine.states;

import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_SWIPE_LEFTTORIGHT;
import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_SWIPE_RIGHTTOLEFT;
import static de.telekom.cldii.statemachine.StateMachineConstants.SECTION3FAVORITELOOP;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_NO_FAVS;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_SEQUENCE_1_1;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_SEQUENCE_4;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_SEQUENCE_5;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_SEQUENCE_6;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_SLEEP;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_START;
import static de.telekom.cldii.statemachine.StateMachineConstants.TIME_TO_WAIT_BIG;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.Log;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.contact.Contact;
import de.telekom.cldii.statemachine.StateContent;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.view.phone.PhoneAddressbookListActivity;
import de.telekom.cldii.view.phone.PhoneFavoritesActivity;

public class StateModelPhoneFavorites extends StateModel {

    /**
     * TAG for Log methods
     */
    protected static final String TAG = "StateModelPhoneFavorites";

    public StateModelPhoneFavorites(final Context context, IDataProviderManager dataProviderManager) {
        super(context);
        final List<Contact> favsItemsList = new ArrayList<Contact>(dataProviderManager.getContactDataProvider()
                .getFavoriteContacts());

        StateContent stateContent;

        // STATE START -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();

                // Landmarking prompt
                getStateMachine().speakAndNextState(context.getString(R.string.section_phonefavs), getStateId(),
                        TIME_TO_WAIT_BIG);
            }

        };
        if (favsItemsList == null || favsItemsList.isEmpty()) {
            stateContent.setNextId(STATE_NO_FAVS);
        } else {
            stateContent.setNextId(STATE_SEQUENCE_1_1);
        }

        this.addStateContent(STATE_START, stateContent);

        // STATE STATE_SEQUENCE_1.1 - READ LIST?
        stateContent = new StateContent(context) {

            @Override
            public void reactOnTap() {
                super.reactOnTap();

                String tempNextId = getNextId();
                setNextId(SECTION3FAVORITELOOP + favsItemsList.get(0).getId());
                getStateMachine().nextState(getStateId(), false);
                setNextId(tempNextId);
            }

            @Override
            public void reactOnGesture(String gestureName) {
                super.reactOnGesture(gestureName);

                if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {
                    // PREVIOUS
                    String tempNextId = getNextId();
                    setNextId(STATE_SEQUENCE_6);
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                    // NEXT
                    getStateMachine().nextState(getStateId(), false);
                }
            }

            @Override
            public void executeInState() {
                TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlaySpeechBalloon });
                int attributeResourceId = a.getResourceId(0, 0);
                setGestureOverlayIconId(attributeResourceId);
                a.recycle();
                super.executeInState();

                getStateMachine().speakAndNextState(
                        context.getResources().getString(R.string.tts_phone_you_have) + " " + favsItemsList.size()
                                + " " + context.getResources().getString(R.string.tts_phone_favorites) + " "
                                + context.getResources().getString(R.string.tts_phone_shall_i_read), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId(STATE_SEQUENCE_5);
        this.addStateContent(STATE_SEQUENCE_1_1, stateContent);

        // STATE STATE_SEQUENCE_5 - GO TO ADDRESSBOOK?
        stateContent = new StateContent(context) {

            @Override
            public void reactOnTap() {
                ((PhoneFavoritesActivity) context).addressBookButtonClicked();
                super.reactOnTap();
            }

            @Override
            public void reactOnGesture(String gestureName) {
                super.reactOnGesture(gestureName);

                if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {
                    // PREVIOUS
                    String tempNextId = getNextId();
                    setNextId(STATE_SEQUENCE_1_1);
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                    // NEXT
                    getStateMachine().nextState(getStateId(), false);
                }
            }

            @Override
            public void executeInState() {
                TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlaySpeechBalloon });
                int attributeResourceId = a.getResourceId(0, 0);
                setGestureOverlayIconId(attributeResourceId);
                a.recycle();
                super.executeInState();

                getStateMachine().speakAndNextState(
                        context.getResources().getString(R.string.tts_phone_ortoaddressbook), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId(STATE_SEQUENCE_6);
        this.addStateContent(STATE_SEQUENCE_5, stateContent);

        // STATE STATE_SEQUENCE_6 - BACK TO MAIN MENU?
        stateContent = new StateContent(context) {

            @Override
            public void reactOnTap() {
                stateMachine.goBackToMainMenu();
                super.reactOnTap();
            }

            @Override
            public void reactOnGesture(String gestureName) {
                super.reactOnGesture(gestureName);

                if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {
                    // PREVIOUS
                    String tempNextId = getNextId();
                    setNextId(STATE_SEQUENCE_5);
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                    // NEXT
                    String tempNextId = getNextId();
                    setNextId(STATE_SEQUENCE_1_1);
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                }
            }

            @Override
            public void executeInState() {
                TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlaySpeechBalloon });
                int attributeResourceId = a.getResourceId(0, 0);
                setGestureOverlayIconId(attributeResourceId);
                a.recycle();
                super.executeInState();

                getStateMachine().speakAndNextState(
                        context.getResources().getString(R.string.tts_phone_orbacktomainmenu), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId(STATE_SLEEP);
        this.addStateContent(STATE_SEQUENCE_6, stateContent);

        // SECTION3FAVORITELOOP -------------
        for (final Contact favsItem : favsItemsList) {

            stateContent = new StateContent(context) {

                @Override
                public void reactOnTap() {
                    super.reactOnTap();

                    ((PhoneFavoritesActivity) context)
                            .callDefaultNumberOfContactWithId(Long.parseLong(favsItem.getId()));
                }

                @Override
                public void reactOnGesture(String gestureName) {
                    super.reactOnGesture(gestureName);

                    int currentIndex = favsItemsList.indexOf(favsItem);
                    Log.d(TAG, "currentIndex:" + currentIndex);

                    if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {

                        // PREVIOUS
                        if (currentIndex == 0) {
                            String tempNextId = getNextId();
                            setNextId("INFAVS" + STATE_SEQUENCE_6);
                            getStateMachine().nextState(getStateId(), false);
                            setNextId(tempNextId);
                        } else {
                            if (currentIndex > 0) {
                                String tempNextId = getNextId();
                                setNextId(SECTION3FAVORITELOOP + favsItemsList.get(currentIndex - 1).getId());
                                getStateMachine().nextState(getStateId(), false);
                                setNextId(tempNextId);
                            }
                        }

                    } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                        // NEXT
                        if (currentIndex != favsItemsList.size() - 1) {
                            String tempNextId = getNextId();
                            setNextId(SECTION3FAVORITELOOP + favsItemsList.get(currentIndex + 1).getId());
                            getStateMachine().nextState(getStateId(), false);
                            setNextId(tempNextId);
                        } else {
                            if (favsItemsList.size() > 0) {
                                String tempNextId = getNextId();
                                setNextId("INFAVS" + STATE_SEQUENCE_4);
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

                    String speakText = "";
                    if (favsItemsList.indexOf(favsItem) == 0) {
                        speakText = context.getResources().getString(R.string.tts_phone_shallicallcontact1) + " "
                                + favsItem.getDisplayName() + " "
                                + context.getResources().getString(R.string.tts_phone_shallicallcontact2);
                    } else {
                        speakText = context.getResources().getString(R.string.tts_phone_orwith) + " "
                                + favsItem.getDisplayName() + " ?";
                    }

                    getStateMachine().speakAndNextState(speakText, getStateId(), TIME_TO_WAIT_BIG);
                }

            };
            if (favsItemsList.get(favsItemsList.size() - 1) == favsItem) {
                stateContent.setNextId("INFAVS" + STATE_SEQUENCE_4);
            } else {
                stateContent.setNextId(SECTION3FAVORITELOOP
                        + favsItemsList.get(favsItemsList.indexOf(favsItem) + 1).getId());
            }
            this.addStateContent(SECTION3FAVORITELOOP + favsItem.getId(), stateContent);
        }

        // STATE_SEQUENCE_4 - READ AGAIN? -------------
        stateContent = new StateContent(context) {

            @Override
            public void reactOnTap() {
                String tempNextId = getNextId();
                setNextId(SECTION3FAVORITELOOP + favsItemsList.get(0).getId());
                getStateMachine().nextState(getStateId(), false);
                setNextId(tempNextId);
                super.reactOnTap();
            }

            @Override
            public void reactOnGesture(String gestureName) {
                super.reactOnGesture(gestureName);

                if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {
                    // PREVIOUS
                    String tempNextId = getNextId();
                    setNextId(SECTION3FAVORITELOOP + favsItemsList.get(favsItemsList.size() - 1).getId());
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                    // NEXT
                    getStateMachine().nextState(getStateId(), false);
                }
            }

            @Override
            public void executeInState() {
                TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlaySpeechBalloon });
                int attributeResourceId = a.getResourceId(0, 0);
                setGestureOverlayIconId(attributeResourceId);
                a.recycle();
                super.executeInState();

                getStateMachine().speakAndNextState(context.getResources().getString(R.string.tts_phone_readfavsagain),
                        getStateId(), TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId(STATE_SEQUENCE_5);
        addStateContent(STATE_SEQUENCE_4, stateContent);

        // STATE NO_FAVS -------------
        stateContent = new StateContent(context) {

            @Override
            public void reactOnTap() {
                super.reactOnTap();

                Intent intent = new Intent(context, PhoneAddressbookListActivity.class);
                context.startActivity(intent);
            }

            @Override
            public void executeInState() {
                super.executeInState();
                getStateMachine().speakAndNextState(context.getResources().getString(R.string.tts_phone_nofavs),
                        getStateId(), TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId(STATE_SEQUENCE_6);
        addStateContent(STATE_NO_FAVS, stateContent);

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

        // STATE_SEQUENCE_4 - READ AGAIN? ------------- IN FAVORITES
        stateContent = new StateContent(context) {

            @Override
            public void reactOnTap() {
                String tempNextId = getNextId();
                setNextId(SECTION3FAVORITELOOP + favsItemsList.get(0).getId());
                getStateMachine().nextState(getStateId(), false);
                setNextId(tempNextId);
                super.reactOnTap();
            }

            @Override
            public void reactOnGesture(String gestureName) {
                super.reactOnGesture(gestureName);

                if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {
                    // PREVIOUS
                    String tempNextId = getNextId();
                    setNextId(SECTION3FAVORITELOOP + favsItemsList.get(favsItemsList.size() - 1).getId());
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                    // NEXT
                    getStateMachine().nextState(getStateId(), false);
                }
            }

            @Override
            public void executeInState() {
                TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlaySpeechBalloon });
                int attributeResourceId = a.getResourceId(0, 0);
                setGestureOverlayIconId(attributeResourceId);
                a.recycle();
                super.executeInState();

                getStateMachine().speakAndNextState(context.getResources().getString(R.string.tts_phone_readfavsagain),
                        getStateId(), TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId("INFAVS" + STATE_SEQUENCE_5);
        addStateContent("INFAVS" + STATE_SEQUENCE_4, stateContent);

        // STATE STATE_SEQUENCE_5 - GO TO ADDRESSBOOK? IN FAVORITES
        stateContent = new StateContent(context) {

            @Override
            public void reactOnTap() {
                ((PhoneFavoritesActivity) context).addressBookButtonClicked();
                super.reactOnTap();
            }

            @Override
            public void reactOnGesture(String gestureName) {
                super.reactOnGesture(gestureName);

                if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {
                    // PREVIOUS
                    String tempNextId = getNextId();
                    setNextId("INFAVS" + STATE_SEQUENCE_4);
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                    // NEXT
                    getStateMachine().nextState(getStateId(), false);
                }
            }

            @Override
            public void executeInState() {
                TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlaySpeechBalloon });
                int attributeResourceId = a.getResourceId(0, 0);
                setGestureOverlayIconId(attributeResourceId);
                a.recycle();
                super.executeInState();

                getStateMachine().speakAndNextState(
                        context.getResources().getString(R.string.tts_phone_ortoaddressbook), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId("INFAVS" + STATE_SEQUENCE_6);
        this.addStateContent("INFAVS" + STATE_SEQUENCE_5, stateContent);

        // STATE STATE_SEQUENCE_6 - BACK TO MAIN MENU? IN FAVORITES
        stateContent = new StateContent(context) {

            @Override
            public void reactOnTap() {
                stateMachine.goBackToMainMenu();
                super.reactOnTap();
            }

            @Override
            public void reactOnGesture(String gestureName) {
                super.reactOnGesture(gestureName);

                if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {
                    // PREVIOUS
                    String tempNextId = getNextId();
                    setNextId("INFAVS" + STATE_SEQUENCE_5);
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                    // NEXT
                    String tempNextId = getNextId();
                    setNextId(SECTION3FAVORITELOOP + favsItemsList.get(0).getId());
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                }
            }

            @Override
            public void executeInState() {
                TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlaySpeechBalloon });
                int attributeResourceId = a.getResourceId(0, 0);
                setGestureOverlayIconId(attributeResourceId);
                a.recycle();
                super.executeInState();

                getStateMachine().speakAndNextState(
                        context.getResources().getString(R.string.tts_phone_orbacktomainmenu), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId(STATE_SLEEP);
        this.addStateContent("INFAVS" + STATE_SEQUENCE_6, stateContent);
    }
}

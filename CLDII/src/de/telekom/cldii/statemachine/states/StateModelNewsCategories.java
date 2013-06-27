package de.telekom.cldii.statemachine.states;

import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_CATEGORYID;
import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_SWIPE_LEFTTORIGHT;
import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_SWIPE_RIGHTTOLEFT;
import static de.telekom.cldii.statemachine.StateMachineConstants.SECTION2CATEGORYLOOP;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_GOTO_MAINMENU;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_LISTEND;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_NO_ITEMS;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_SLEEP;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_START;
import static de.telekom.cldii.statemachine.StateMachineConstants.TIME_TO_WAIT_BIG;
import static de.telekom.cldii.statemachine.StateMachineConstants.TIME_TO_WAIT_DONT_WAIT;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.Log;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.news.NewsCategory;
import de.telekom.cldii.statemachine.StateContent;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.util.SignalTonePlayer;
import de.telekom.cldii.view.news.NewsListActivity;

public class StateModelNewsCategories extends StateModel {

    /**
     * TAG for Log methods
     */
    protected static final String TAG = "StateModelNewsCategories";

    public StateModelNewsCategories(final Context context, IDataProviderManager dataProviderManager) {
        super(context);
        StateContent stateContent;

        final ArrayList<NewsCategory> itemsList = new ArrayList<NewsCategory>(dataProviderManager.getNewsDataProvider()
                .getNewsCategoriesOrderedByName());

        // STATE START -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();

                // Landmarking prompt
                getStateMachine().speakAndNextState(context.getString(R.string.tts_news_categories), getStateId(),
                        TIME_TO_WAIT_BIG);
            }

        };
        if (itemsList == null || itemsList.size() == 0) {
            stateContent.setNextId(STATE_NO_ITEMS);
        } else {
            stateContent.setNextId(SECTION2CATEGORYLOOP + itemsList.get(0).getText());
        }
        this.addStateContent(STATE_START, stateContent);

        // STATE SECTION2TOX LOOP-------------
        for (final NewsCategory newsCategory : itemsList) {
            final String categoryName = newsCategory.getText();

            stateContent = new StateContent(context) {

                @Override
                public void reactOnGesture(String gestureName) {
                    super.reactOnGesture(gestureName);

                    int currentIndex = itemsList.indexOf(newsCategory);
                    Log.d(TAG, "currentIndex:" + currentIndex);

                    if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {

                        // PREVIOUS NEWS
                        if (currentIndex == 0) {
                            String tempNextId = getNextId();
                            setNextId(SECTION2CATEGORYLOOP + itemsList.get(itemsList.size() - 1).getText());
                            getStateMachine().nextState(getStateId(), false);
                            setNextId(tempNextId);
                        } else {
                            if (currentIndex > 0) {
                                String tempNextId = getNextId();
                                setNextId(SECTION2CATEGORYLOOP + itemsList.get(currentIndex - 1).getText());
                                getStateMachine().nextState(getStateId(), false);
                                setNextId(tempNextId);
                            }
                        }

                    } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                        // NEXT NEWS
                        if (currentIndex != itemsList.size() - 1) {
                            String tempNextId = getNextId();
                            setNextId(SECTION2CATEGORYLOOP + itemsList.get(currentIndex + 1).getText());
                            getStateMachine().nextState(getStateId(), false);
                            setNextId(tempNextId);
                        } else {
                            if (itemsList.size() > 0) {
                                String tempNextId = getNextId();
                                setNextId(SECTION2CATEGORYLOOP + itemsList.get(0).getText());
                                getStateMachine().nextState(getStateId(), false);
                                setNextId(tempNextId);
                            }
                        }
                    }
                }

                @Override
                public void reactOnTap() {
                    super.reactOnTap();

                    Intent intent = new Intent(context, NewsListActivity.class);
                    intent.putExtra(EXTRAS_KEY_CATEGORYID, Long.valueOf(newsCategory.getId()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    context.startActivity(intent);
                }

                @Override
                public void executeInState() {
                    TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlaySpeechBalloon });
                    int attributeResourceId = a.getResourceId(0, 0);
                    setGestureOverlayIconId(attributeResourceId);
                    a.recycle();
                    super.executeInState();

                    getStateMachine().speakAndNextState(categoryName, getStateId(), TIME_TO_WAIT_BIG);
                }

            };
            if (itemsList.get(itemsList.size() - 1) == newsCategory) {
                stateContent.setNextId(STATE_LISTEND);
            } else {
                stateContent.setNextId(SECTION2CATEGORYLOOP
                        + itemsList.get(itemsList.indexOf(newsCategory) + 1).getText());
            }
            this.addStateContent(SECTION2CATEGORYLOOP + categoryName, stateContent);
        }

        // STATE STATE_LISTEND -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                if (getStateMachine().getRepeatCounter() == 0) {
                    if (itemsList.size() != 0) {
                        setNextId(SECTION2CATEGORYLOOP + itemsList.get(0).getText());
                    }
                    getStateMachine().increaseRepeatCounter();
                } else {
                    this.setNextId(STATE_SLEEP);
                }

                super.executeInState();
                getStateMachine().speakAndNextState(SignalTonePlayer.SIGNAL_TONE_LISTEND, getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        addStateContent(STATE_LISTEND, stateContent);

        // STATE NO_CATEGORIES -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();
                getStateMachine().speakAndNextState(context.getString(R.string.news_no_category), getStateId(),
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
    }

}

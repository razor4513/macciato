package de.telekom.cldii.statemachine.states;

import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_CATEGORYID;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_CATEGORYNAME;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_NEWSID;
import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_SWIPE_LEFTTORIGHT;
import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_SWIPE_RIGHTTOLEFT;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_MANUAL_LIST_BEGIN;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_MANUAL_LIST_END;
import static de.telekom.cldii.statemachine.StateMachineConstants.SECTION2NEWSLOOP;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_GOTO_MAINMENU;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_NO_NEWS;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_SLEEP;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_START;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_ASK_READLISTAGAIN;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_ASK_WANTGOTOMAINMENU;
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
import de.telekom.cldii.data.news.NewsItem;
import de.telekom.cldii.statemachine.StateContent;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.view.news.NewsDetailsActivity;
import de.telekom.cldii.view.news.NewsListActivity;

public class StateModelNewsList extends StateModel {

    /**
     * TAG for Log methods
     */
    protected static final String TAG = "StateModelNewsList";

    public StateModelNewsList(final Context context, final IDataProviderManager dataProviderManager) {
        super(context);
        StateContent stateContent;
        final NewsListActivity globalModelContext = (NewsListActivity) context;

        final List<NewsItem> newsItemsList = new ArrayList<NewsItem>(globalModelContext.getNewsItemList());

        // STATE START -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();

                // Landmarking prompt
                String newsCategoryName = dataProviderManager.getNewsDataProvider().getNewsCategoryNameById(
                        globalModelContext.getCategoryId());
                getStateMachine().speakAndNextState(context.getString(R.string.tts_category) + " " + newsCategoryName,
                        getStateId(), TIME_TO_WAIT_BIG);
            }
        };

        if (newsItemsList == null || newsItemsList.size() == 0) {
            stateContent.setNextId(STATE_NO_NEWS);
        } else {
            stateContent.setNextId(SECTION2NEWSLOOP + newsItemsList.get(0).getNewsId());
        }

        this.addStateContent(STATE_START, stateContent);

        // STATE SECTION2TOX LOOP-------------
        if (newsItemsList != null) {
            for (final NewsItem newsItem : newsItemsList) {
                final String newsTitle = newsItem.getTitle();

                stateContent = new StateContent(context) {

                    @Override
                    public void reactOnTap() {
                        super.reactOnTap();

                        Intent newsDetails = new Intent(context, NewsDetailsActivity.class);
                        newsDetails.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        newsDetails.putExtra(EXTRAS_KEY_NEWSID, Long.valueOf(newsItem.getNewsId()));
                        newsDetails.putExtra(EXTRAS_KEY_CATEGORYID, globalModelContext.getCategoryId());
                        newsDetails.putExtra(EXTRAS_KEY_CATEGORYNAME, globalModelContext.getCategoryName());
                        context.startActivity(newsDetails);
                    }

                    @Override
                    public void reactOnGesture(String gestureName) {
                        super.reactOnGesture(gestureName);

                        int currentIndex = newsItemsList.indexOf(newsItem);
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
                                    setNextId(SECTION2NEWSLOOP + newsItemsList.get(currentIndex - 1).getNewsId());
                                    getStateMachine().nextState(getStateId(), false);
                                    setNextId(tempNextId);
                                }
                            }

                        } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                            // NEXT NEWS
                            if (currentIndex != newsItemsList.size() - 1) {
                                getStateMachine().nextState(getNextId(), true);
                            } else {
                                String tempNextId = getNextId();
                                setNextId(STATE_MANUAL_LIST_END);
                                getStateMachine().nextState(getStateId(), false);
                                setNextId(tempNextId);
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

                        getStateMachine().speakAndNextState(newsTitle, getStateId(), TIME_TO_WAIT_BIG);
                    }

                };
                if (newsItemsList.get(newsItemsList.size() - 1) == newsItem) {
                    stateContent.setNextId(STATE_MANUAL_LIST_END);
                } else {
                    stateContent.setNextId(SECTION2NEWSLOOP
                            + newsItemsList.get(newsItemsList.indexOf(newsItem) + 1).getNewsId());
                }
                this.addStateContent(SECTION2NEWSLOOP + newsItem.getNewsId(), stateContent);
            }
        }

        // STATE NO_NEWS -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();
                getStateMachine().speakAndNextState(context.getString(R.string.news_no_news), getStateId(),
                        TIME_TO_WAIT_DONT_WAIT);
            }
        };
        stateContent.setNextId(STATE_GOTO_MAINMENU);
        addStateContent(STATE_NO_NEWS, stateContent);

        // STATE STATE_GOTO_MAINMENU -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();
                globalModelContext.finish();
            }
        };
        addStateContent(STATE_GOTO_MAINMENU, stateContent);

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

        if (newsItemsList != null || newsItemsList.size() != 0) {
            stateContent.setNextId(SECTION2NEWSLOOP + newsItemsList.get(0).getNewsId());
        }

        this.addStateContent(STATE_MANUAL_LIST_BEGIN, stateContent);

        // STATE MANUAL_LIST_END -------------
        stateContent = new StateContent(context) {

            @Override
            public void reactOnGesture(String gestureName) {
                super.reactOnGesture(gestureName);

                if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {

                    if (newsItemsList != null || newsItemsList.size() != 0) {
                        String tempNextId = getNextId();
                        setNextId(SECTION2NEWSLOOP + newsItemsList.get(newsItemsList.size() - 1).getNewsId());
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
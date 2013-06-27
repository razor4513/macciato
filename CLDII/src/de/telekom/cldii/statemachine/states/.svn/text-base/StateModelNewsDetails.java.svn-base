package de.telekom.cldii.statemachine.states;

import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_SWIPE_LEFTTORIGHT;
import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_SWIPE_RIGHTTOLEFT;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_MANUAL_LIST_BEGIN;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_MANUAL_LIST_END;
import static de.telekom.cldii.statemachine.StateMachineConstants.SECTION2NEWSLOOP;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_ASK_READLISTAGAIN;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_ASK_WANTGOTOMAINMENU;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_GOTO_MAINMENU;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_NO_NEWS;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_OPTIONSMENU;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_OPTIONSMENU_BACKTOLIST;
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
import android.content.res.TypedArray;
import android.util.Log;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.news.NewsItem;
import de.telekom.cldii.statemachine.StateContent;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.view.news.NewsDetailsActivity;

public class StateModelNewsDetails extends StateModel {

    /**
     * TAG for Log methods
     */
    protected static final String TAG = "StateModelNewsDetails";

    private NewsItem currentItem;
    private String currentItemBeforeOptionsMenuId;

    public String getCurrentItemBeforeOptionsMenuId() {
        return currentItemBeforeOptionsMenuId;
    }

    public StateModelNewsDetails(final Context context, final IDataProviderManager dataProviderManager) {
        super(context);
        StateContent stateContent;
        final NewsDetailsActivity globalModelContext = (NewsDetailsActivity) context;

        final List<NewsItem> newsItemsList = new ArrayList<NewsItem>(globalModelContext.getNewsList());

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
                String newsCategoryName = dataProviderManager.getNewsDataProvider().getNewsCategoryNameById(
                        globalModelContext.getIncomingCategoryId());
                getStateMachine()
                        .speakAndNextState(
                                globalModelContext.getString(R.string.tts_category) + newsCategoryName
                                        + globalModelContext.getString(R.string.tts_detailview), getStateId(),
                                TIME_TO_WAIT_BIG);
            }
        };
        if (newsItemsList.size() == 0) {
            stateContent.setNextId(STATE_NO_NEWS);
        } else {
            stateContent.setNextId(SECTION2NEWSLOOP
                    + newsItemsList.get(globalModelContext.getCurrentNewsIndex()).getNewsId());
        }

        this.addStateContent(STATE_START, stateContent);

        // STATE SECTION2TOX LOOP-------------
        for (final NewsItem newsItem : newsItemsList) {
            final String newsTitle = newsItem.getTitle();

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

                    ((NewsDetailsActivity) globalModelContext).getDetailNewsItem(newsItem.getNewsId());

                    int currentIndex = globalModelContext.getCurrentNewsIndex();

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
                            getStateMachine().nextState(getStateId(), false);
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
                    TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlayMouth });
                    int attributeResourceId = a.getResourceId(0, 0);
                    setGestureOverlayIconId(attributeResourceId);
                    a.recycle();
                    super.executeInState();

                    currentItem = newsItem;

                    // Filter clean text for tts
                    String newsContent = "";
                    Document doc = null;
                    if (newsItem.getContent() == null || newsItem.getContent().length() < 1) {
                        if (newsItem.getSummary() != null) {
                            doc = Jsoup.parse(newsItem.getSummary());
                        }
                    } else {
                        doc = Jsoup.parse(newsItem.getContent());

                    }
                    if (doc != null) {
                        doc.select("div").remove();
                        newsContent = doc.text();
                    }

                    getStateMachine().speakAndNextState(newsTitle + ": " + newsContent, getStateId(), TIME_TO_WAIT_BIG);
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

        // STATES OPTIONSMENU -------------
        // Optionsmenu landmark
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();

                currentItemBeforeOptionsMenuId = SECTION2NEWSLOOP + currentItem.getNewsId();

                getStateMachine().speakAndNextState(context.getString(R.string.tts_optionsmenu), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId(STATE_OPTIONSMENU_NEXT);
        this.addStateContent(STATE_OPTIONSMENU, stateContent);

        // Next
        stateContent = new StateContent(context) {

            @Override
            public void reactOnTap() {
                super.reactOnTap();

                int currentIndex = newsItemsList.indexOf(currentItem);
                Log.d(TAG, "currentIndex:" + currentIndex);

                if (currentIndex != newsItemsList.size() - 1) {
                    String tempNextId = getNextId();
                    setNextId(SECTION2NEWSLOOP + newsItemsList.get(currentIndex + 1).getNewsId());
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                } else {
                    if (newsItemsList.size() > 0) {
                        String tempNextId = getNextId();
                        setNextId(SECTION2NEWSLOOP + newsItemsList.get(0).getNewsId());
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

                int currentIndex = newsItemsList.indexOf(currentItem);
                Log.d(TAG, "currentIndex:" + currentIndex);

                // PREVIOUS NEWS
                if (currentIndex > 0) {
                    String tempNextId = getNextId();
                    setNextId(SECTION2NEWSLOOP + newsItemsList.get(currentIndex - 1).getNewsId());
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                } else {
                    String tempNextId = getNextId();
                    setNextId(SECTION2NEWSLOOP + newsItemsList.get(newsItemsList.size() - 1).getNewsId());
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
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

        // Back to news list
        stateContent = new StateContent(context) {

            @Override
            public void reactOnTap() {
                super.reactOnTap();
                ((Activity) context).finish();
            }

            @Override
            public void executeInState() {
                super.executeInState();

                getStateMachine().speakAndNextState(context.getString(R.string.tts_optionsmenu_backtonewslist),
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
                setNextId(SECTION2NEWSLOOP + currentItem.getNewsId());

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
                    if (newsItemsList.size() != 0) {
                        getStateMachine().getStateModel().getStateMap().get(STATE_START)
                                .setNextId(SECTION2NEWSLOOP + newsItemsList.get(0).getNewsId());
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

        if (newsItemsList != null || newsItemsList.size() != 0) {
            stateContent.setNextId(SECTION2NEWSLOOP + newsItemsList.get(0).getNewsId());
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
}

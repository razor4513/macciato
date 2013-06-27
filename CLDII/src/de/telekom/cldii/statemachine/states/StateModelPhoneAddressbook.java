package de.telekom.cldii.statemachine.states;

import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_SWIPE_LEFTTORIGHT;
import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_SWIPE_RIGHTTOLEFT;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_GOTO_MAINMENU;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_LISTEND;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_MANUAL_LIST_BEGIN;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_NO_ITEMS;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_PHONE_READ_CONTACTSFORLETTER;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_PHONE_READ_LETTERSLIST;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_PHONE_READ_PHONESFORCONTACT;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_SLEEP;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_START;
import static de.telekom.cldii.statemachine.StateMachineConstants.TIME_TO_WAIT_BIG;
import static de.telekom.cldii.statemachine.StateMachineConstants.TIME_TO_WAIT_DONT_WAIT;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.contact.Contact;
import de.telekom.cldii.data.contact.Contact.Phone;
import de.telekom.cldii.data.contact.IContactDataProvider;
import de.telekom.cldii.statemachine.StateContent;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.view.AbstractActivity;

public class StateModelPhoneAddressbook extends StateModel {
    /**
     * TAG for Log methods
     */
    protected static final String TAG = "StateModelPhoneAddressbook";

    private List<String> indexCharList;
    private List<Contact> contactPerLetterList;
    private List<Phone> numbersPerContactList;

    public StateModelPhoneAddressbook(final AbstractActivity activity, final IDataProviderManager dataProviderManager) {
        super(activity);
        StateContent stateContent;

        // ================ LETTERLIST START =================================

        indexCharList = new ArrayList<String>(dataProviderManager.getContactDataProvider().getIndexChars());

        // STATE START -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();

                // Landmarking prompt
                getStateMachine().speakAndNextState(context.getString(R.string.tts_addressbook), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };

        if (indexCharList == null || indexCharList.size() == 0) {
            stateContent.setNextId("LOL_" + STATE_NO_ITEMS);
        } else {
            stateContent.setNextId("LOL_" + STATE_PHONE_READ_LETTERSLIST + indexCharList.get(0));
        }

        this.addStateContent(STATE_START, stateContent);

        // STATE READ INDEX CHAR LIST
        if (indexCharList != null) {
            for (final String letter : indexCharList) {

                stateContent = new StateContent(context) {

                    @Override
                    public void executeInState() {
                        super.executeInState();

                        String letterString = "";
                        if (letter.equals("?")) {
                            letterString = context.getString(R.string.tts_questionmark);
                        } else {
                            letterString = (String) letter;
                        }

                        getStateMachine().speakAndNextState(letterString, getStateId(), TIME_TO_WAIT_BIG);
                    }

                    @Override
                    public void reactOnTap() {
                        super.reactOnTap();

                        addContactsForLetterStatesToMap(dataProviderManager.getContactDataProvider(),
                                dataProviderManager.getContactDataProvider().getContactsForIndexChar(letter), letter);

                        String tempNextId = getNextId();
                        setNextId("CFL_" + STATE_START);
                        getStateMachine().nextState(getStateId(), false);
                        setNextId(tempNextId);
                    }

                    @Override
                    public void reactOnGesture(String gestureName) {
                        super.reactOnGesture(gestureName);

                        int currentIndex = indexCharList.indexOf(letter);
                        Log.d(TAG, "currentIndex:" + currentIndex);

                        if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {

                            // PREVIOUS
                            if (currentIndex == 0) {
                                String tempNextId = getNextId();
                                setNextId("LOL_" + STATE_LISTEND);
                                getStateMachine().nextState(getStateId(), false);
                                setNextId(tempNextId);
                            } else {
                                if (currentIndex > 0) {
                                    String tempNextId = getNextId();
                                    setNextId("LOL_" + STATE_PHONE_READ_LETTERSLIST
                                            + indexCharList.get(currentIndex - 1));
                                    getStateMachine().nextState(getStateId(), false);
                                    setNextId(tempNextId);
                                }
                            }

                        } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                            // NEXT
                            if (currentIndex != indexCharList.size() - 1) {
                                getStateMachine().nextState(getNextId(), true);
                            } else {
                                String tempNextId = getNextId();
                                setNextId("LOL_" + STATE_LISTEND);
                                getStateMachine().nextState(getStateId(), false);
                                setNextId(tempNextId);
                            }
                        }
                    }
                };

                if (indexCharList.get(indexCharList.size() - 1) == letter) {
                    stateContent.setNextId("LOL_" + STATE_LISTEND);
                } else {
                    stateContent.setNextId("LOL_" + STATE_PHONE_READ_LETTERSLIST
                            + indexCharList.get(indexCharList.indexOf(letter) + 1));
                }
                this.addStateContent("LOL_" + STATE_PHONE_READ_LETTERSLIST + letter, stateContent);
            }
        }

        // STATE "LOL_"+STATE_LISTEND -------------
        stateContent = new StateContent(context) {

            @Override
            public void reactOnTap() {
                super.reactOnTap();

                ((Activity) context).finish();
            }

            @Override
            public void reactOnGesture(String gestureName) {
                super.reactOnGesture(gestureName);

                if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {

                    // PREVIOUS
                    String tempNextId = getNextId();
                    setNextId("LOL_" + STATE_PHONE_READ_LETTERSLIST + indexCharList.get(indexCharList.size() - 1));
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);

                } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                    // NEXT
                    String tempNextId = getNextId();
                    setNextId("LOL_" + STATE_PHONE_READ_LETTERSLIST + indexCharList.get(0));
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                }
            }

            @Override
            public void executeInState() {
                super.executeInState();

                getStateMachine().speakAndNextState(context.getString(R.string.tts_phone_ordoyouwanttogoback),
                        getStateId(), TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId("LOL_" + STATE_SLEEP);
        addStateContent("LOL_" + STATE_LISTEND, stateContent);

        // STATE NO_LETTERS -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();
                getStateMachine().speakAndNextState(context.getString(R.string.phone_no_contacts), getStateId(),
                        TIME_TO_WAIT_DONT_WAIT);
            }
        };
        stateContent.setNextId("LOL_" + STATE_GOTO_MAINMENU);
        addStateContent("LOL_" + STATE_NO_ITEMS, stateContent);

        // STATE "LOL_"+STATE_GOTO_MAINMENU -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();
                stateMachine.goBackToMainMenu();
            }
        };
        addStateContent("LOL_" + STATE_GOTO_MAINMENU, stateContent);

        // STATE "LOL_"+STATE_SLEEP -------------
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
                setNextId(STATE_START);
                getStateMachine().nextState(getStateId(), false);
            }
        };
        stateContent.setGestureOverlayStringId(R.string.gesture_overlay_silence);
        addStateContent("LOL_" + STATE_SLEEP, stateContent);

        // Common state sleep for passing the model validation
        stateContent = new StateContent(context) {
        };
        addStateContent(STATE_SLEEP, stateContent);

        // ================ LETTERLIST END =================================

    }

    private void addContactsForLetterStatesToMap(final IContactDataProvider contactDataProvider, List<Contact> list,
            final String letterstring) {
        // ================ CONTACTS BY LETTER START =================

        contactPerLetterList = new ArrayList<Contact>(list);

        // STATE START -------------
        StateContent stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();

                // Landmarking prompt
                getStateMachine().speakAndNextState(
                        context.getString(R.string.tts_phone_contactswithfirstletter) + " " + letterstring,
                        getStateId(), TIME_TO_WAIT_BIG);
            }
        };

        if (contactPerLetterList == null || contactPerLetterList.size() == 0) {
            stateContent.setNextId("CFL_" + letterstring + "_" + STATE_NO_ITEMS);
        } else {
            stateContent.setNextId("CFL_" + letterstring + "_" + STATE_PHONE_READ_CONTACTSFORLETTER
                    + contactPerLetterList.get(0).getId());
        }

        this.addStateContent("CFL_" + STATE_START, stateContent);

        // STATE READ INDEX CHAR LIST
        if (contactPerLetterList != null) {
            for (final Contact contact : contactPerLetterList) {

                stateContent = new StateContent(context) {

                    @Override
                    public void executeInState() {
                        super.executeInState();

                        getStateMachine().speakAndNextState(contact.getDisplayName(),
                                "CFL_" + letterstring + "_" + STATE_PHONE_READ_CONTACTSFORLETTER + contact.getId(),
                                TIME_TO_WAIT_BIG);
                    }

                    @Override
                    public void reactOnTap() {
                        super.reactOnTap();

                        Contact contactToClickOn = contact;

                        Log.e(TAG, "Tap on " + contactToClickOn.getDisplayName());
                        List<Phone> phoneObjectList = contactDataProvider.getPhoneForContact(contactToClickOn);
                        if (phoneObjectList != null) {
                            if (phoneObjectList.size() > 1) {
                                addNumbersForContactStatesToMap(contactDataProvider, contact);

                                String tempNextId = getNextId();
                                setNextId("NFC_" + STATE_START);
                                getStateMachine().nextState(getStateId(), false);
                                setNextId(tempNextId);
                            } else if (phoneObjectList.size() == 1) {
                                callPhoneNumber(phoneObjectList.get(0).getNumber());
                            }
                        }
                    }

                    @Override
                    public void reactOnGesture(String gestureName) {
                        super.reactOnGesture(gestureName);

                        int currentIndex = contactPerLetterList.indexOf(contact);
                        Log.d(TAG, "currentIndex:" + currentIndex);

                        if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {

                            // PREVIOUS
                            if (currentIndex == 0) {
                                String tempNextId = getNextId();
                                setNextId("CFL_" + letterstring + "_" + STATE_LISTEND);
                                getStateMachine().nextState(getStateId(), false);
                                setNextId(tempNextId);
                            } else {
                                if (currentIndex > 0) {
                                    String tempNextId = getNextId();
                                    setNextId("CFL_" + letterstring + "_" + STATE_PHONE_READ_CONTACTSFORLETTER
                                            + contactPerLetterList.get(currentIndex - 1).getId());
                                    getStateMachine().nextState(getStateId(), false);
                                    setNextId(tempNextId);
                                }
                            }

                        } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                            // NEXT
                            if (currentIndex != contactPerLetterList.size() - 1) {
                                getStateMachine().nextState(getNextId(), true);
                            } else {
                                String tempNextId = getNextId();
                                setNextId("CFL_" + letterstring + "_" + STATE_LISTEND);
                                getStateMachine().nextState(getStateId(), false);
                                setNextId(tempNextId);
                            }
                        }
                    }
                };

                if (contactPerLetterList.get(contactPerLetterList.size() - 1) == contact) {
                    stateContent.setNextId("CFL_" + letterstring + "_" + STATE_LISTEND);
                } else {
                    stateContent.setNextId("CFL_" + letterstring + "_" + STATE_PHONE_READ_CONTACTSFORLETTER
                            + contactPerLetterList.get(contactPerLetterList.indexOf(contact) + 1).getId());
                }
                this.addStateContent(
                        "CFL_" + letterstring + "_" + STATE_PHONE_READ_CONTACTSFORLETTER + contact.getId(),
                        stateContent);
            }
        }

        // STATE "CFL_"+STATE_LISTEND -------------
        stateContent = new StateContent(context) {

            @Override
            public void reactOnTap() {
                super.reactOnTap();

                String tempNextId = getNextId();
                setNextId(STATE_START);
                getStateMachine().nextState(getStateId(), false);
                setNextId(tempNextId);
            }

            @Override
            public void reactOnGesture(String gestureName) {
                super.reactOnGesture(gestureName);

                if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {
                    // PREVIOUS
                    String tempNextId = getNextId();
                    setNextId("CFL_" + letterstring + "_" + STATE_PHONE_READ_CONTACTSFORLETTER
                            + contactPerLetterList.get(contactPerLetterList.size() - 1).getId());
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                    // NEXT
                    String tempNextId = getNextId();
                    setNextId("CFL_" + letterstring + "_" + STATE_PHONE_READ_CONTACTSFORLETTER
                            + contactPerLetterList.get(0).getId());
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                }
            }

            @Override
            public void executeInState() {
                super.executeInState();

                getStateMachine().speakAndNextState(context.getString(R.string.tts_phone_ordoyouwanttogoback),
                        getStateId(), TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId("CFL_" + letterstring + "_" + STATE_SLEEP);
        addStateContent("CFL_" + letterstring + "_" + STATE_LISTEND, stateContent);

        // STATE NO_LETTERS -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();
                getStateMachine().speakAndNextState(context.getString(R.string.phone_no_contacts), getStateId(),
                        TIME_TO_WAIT_DONT_WAIT);
            }
        };
        stateContent.setNextId("CFL_" + letterstring + "_" + STATE_GOTO_MAINMENU);
        addStateContent("CFL_" + letterstring + "_" + STATE_NO_ITEMS, stateContent);

        // STATE "CFL_"+STATE_GOTO_MAINMENU -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();
                stateMachine.goBackToMainMenu();
            }
        };
        addStateContent("CFL_" + letterstring + "_" + STATE_GOTO_MAINMENU, stateContent);

        // STATE "CFL_" + STATE_SLEEP -------------
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
                setNextId("CFL_" + STATE_START);
                getStateMachine().nextState(getStateId(), false);
            }
        };
        stateContent.setGestureOverlayStringId(R.string.gesture_overlay_silence);
        addStateContent("CFL_" + letterstring + "_" + STATE_SLEEP, stateContent);

        // ================ CONTACTS BY LETTER END =================
    }

    private void addNumbersForContactStatesToMap(final IContactDataProvider contactDataProvider, final Contact contact) {
        // ================ NUMBERS BY CONTACT =================

        numbersPerContactList = new ArrayList<Phone>(contactDataProvider.getPhoneForContact(contact));

        // STATE START -------------
        StateContent stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();

                // Landmarking prompt
                getStateMachine().speakAndNextState(
                        context.getString(R.string.tts_numbersof) + " " + contact.getDisplayName(), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };

        if (numbersPerContactList == null || numbersPerContactList.size() == 0) {
            stateContent.setNextId("NFC_" + STATE_NO_ITEMS);
        } else {
            stateContent.setNextId("NFC_" + STATE_PHONE_READ_PHONESFORCONTACT
                    + numbersPerContactList.get(0).getNumber());
        }

        this.addStateContent("NFC_" + STATE_START, stateContent);

        // STATE READ INDEX CHAR LIST
        if (numbersPerContactList != null) {
            for (final Phone phone : numbersPerContactList) {
                final int currentIndex = numbersPerContactList.indexOf(phone);

                stateContent = new StateContent(context) {

                    @Override
                    public void executeInState() {
                        super.executeInState();

                        String typeText = "";
                        switch (phone.getType()) {
                        case HOME:
                            typeText = context.getString(R.string.tts_phone_number_home);
                            break;

                        case HOME_FAX:
                            typeText = context.getString(R.string.tts_phone_number_homefax);
                            break;

                        case MOBILE:
                            typeText = context.getString(R.string.tts_phone_number_mobile);
                            break;

                        case OTHER:
                            typeText = context.getString(R.string.tts_phone_number_other);
                            break;

                        case WORK:
                            typeText = context.getString(R.string.tts_phone_number_work);
                            break;

                        case WORK_FAX:
                            typeText = context.getString(R.string.tts_phone_number_workfax);
                            break;

                        case WORK_MOBILE:
                            typeText = context.getString(R.string.tts_phone_number_workmobile);
                            break;

                        default:
                            typeText = context.getString(R.string.tts_phone_number_unknown);
                            break;
                        }

                        if (currentIndex == 0) {
                            getStateMachine().speakAndNextState(
                                    String.format(context.getString(R.string.tts_phone_doyouwanttocall), typeText),
                                    getStateId(), TIME_TO_WAIT_BIG);
                        } else {
                            getStateMachine().speakAndNextState(
                                    String.format(context.getString(R.string.tts_phone_or), typeText), getStateId(),
                                    TIME_TO_WAIT_BIG);
                        }
                    }

                    @Override
                    public void reactOnTap() {
                        super.reactOnTap();

                        callPhoneNumber(phone.getNumber());
                    }

                    @Override
                    public void reactOnGesture(String gestureName) {
                        super.reactOnGesture(gestureName);

                        int currentIndex = numbersPerContactList.indexOf(phone);
                        Log.d(TAG, "currentIndex:" + currentIndex);

                        if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {

                            // PREVIOUS NEWS
                            if (currentIndex == 0) {
                                String tempNextId = getNextId();
                                setNextId("NFC_" + STATE_LISTEND);
                                getStateMachine().nextState(getStateId(), false);
                                setNextId(tempNextId);
                            } else {
                                if (currentIndex > 0) {
                                    String tempNextId = getNextId();
                                    setNextId("NFC_" + STATE_PHONE_READ_PHONESFORCONTACT
                                            + numbersPerContactList.get(currentIndex - 1).getNumber());
                                    getStateMachine().nextState(getStateId(), false);
                                    setNextId(tempNextId);
                                }
                            }

                        } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                            // NEXT NEWS
                            if (currentIndex != numbersPerContactList.size() - 1) {
                                getStateMachine().nextState(getNextId(), true);
                            } else {
                                String tempNextId = getNextId();
                                setNextId("NFC_" + STATE_LISTEND);
                                getStateMachine().nextState(getStateId(), false);
                                setNextId(tempNextId);
                            }
                        }
                    }
                };

                if (numbersPerContactList.get(numbersPerContactList.size() - 1) == phone) {
                    stateContent.setNextId("NFC_" + STATE_LISTEND);
                } else {
                    stateContent.setNextId("NFC_" + STATE_PHONE_READ_PHONESFORCONTACT
                            + numbersPerContactList.get(numbersPerContactList.indexOf(phone) + 1).getNumber());
                }
                this.addStateContent("NFC_" + STATE_PHONE_READ_PHONESFORCONTACT + phone.getNumber(), stateContent);
            }
        }

        // STATE "NFC_"+STATE_LISTEND -------------
        stateContent = new StateContent(context) {

            @Override
            public void reactOnTap() {
                super.reactOnTap();

                String tempNextId = getNextId();
                setNextId("CFL_" + STATE_START);
                getStateMachine().nextState(getStateId(), false);
                setNextId(tempNextId);
            }

            @Override
            public void reactOnGesture(String gestureName) {
                super.reactOnGesture(gestureName);

                if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {
                    // PREVIOUS
                    String tempNextId = getNextId();
                    setNextId("NFC_" + STATE_PHONE_READ_PHONESFORCONTACT
                            + numbersPerContactList.get(numbersPerContactList.size() - 1).getNumber());
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                    // NEXT
                    String tempNextId = getNextId();
                    setNextId("NFC_" + STATE_PHONE_READ_PHONESFORCONTACT + numbersPerContactList.get(0).getNumber());
                    getStateMachine().nextState(getStateId(), false);
                    setNextId(tempNextId);
                }
            }

            @Override
            public void executeInState() {
                super.executeInState();

                getStateMachine().speakAndNextState(context.getString(R.string.tts_phone_ordoyouwanttogoback),
                        getStateId(), TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId("NFC_" + STATE_SLEEP);
        addStateContent("NFC_" + STATE_LISTEND, stateContent);

        // STATE NO_LETTERS -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();
                getStateMachine().speakAndNextState(context.getString(R.string.phone_no_contacts), getStateId(),
                        TIME_TO_WAIT_DONT_WAIT);
            }
        };
        stateContent.setNextId("NFC_" + STATE_GOTO_MAINMENU);
        addStateContent("NFC_" + STATE_NO_ITEMS, stateContent);

        // STATE "NFC_"+STATE_GOTO_MAINMENU -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();
                stateMachine.goBackToMainMenu();
            }
        };
        addStateContent("NFC_" + STATE_GOTO_MAINMENU, stateContent);

        // STATE "CFL_" + STATE_SLEEP -------------
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
                setNextId("NFC_" + STATE_START);
                getStateMachine().nextState(getStateId(), false);
            }
        };
        stateContent.setGestureOverlayStringId(R.string.gesture_overlay_silence);
        addStateContent("NFC_" + STATE_SLEEP, stateContent);

        // ================ NUMBERS BY CONTACT END =================
    }

    private void callPhoneNumber(String phoneNumber) {
        if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
            context.startActivity(callIntent);
        }
    }
}

package de.telekom.cldii.view.phone;

import static de.telekom.cldii.ApplicationConstants.STARTSYNCING;
import static de.telekom.cldii.ApplicationConstants.BASICINFOSYNCED;
import static de.telekom.cldii.ApplicationConstants.CONTACTS_SYNCED_ACTION;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.gesture.GestureLibraries;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import de.telekom.cldii.R;
import de.telekom.cldii.data.contact.Contact;
import de.telekom.cldii.data.contact.Contact.Phone;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.statemachine.states.StateModelPhoneFavorites;
import de.telekom.cldii.view.AbstractActivity;
import de.telekom.cldii.view.phone.adapter.PhoneDialogAddFavoritesAdapter;
import de.telekom.cldii.view.phone.adapter.PhoneGridViewPagerAdapter;
import de.telekom.cldii.view.util.CallSystemIntent;
import de.telekom.cldii.widget.grid.GridViewPager;

/**
 * Activity to show a gridview holding favorite contacts
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */

public class PhoneFavoritesActivity extends AbstractActivity {
    private static final String TAG = "PhoneFavoriteActivity";
    private GridViewPager gridViewPager;

    private ProgressBar syncSpinner;

    private BroadcastReceiver contactSyncReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("type")) {
                String type = intent.getExtras().getString("type");

                if (type.equals(BASICINFOSYNCED)) {
                    Log.i(TAG, "contactSyncReceiver: BASICINFOSYNCED");
                    syncSpinner.setVisibility(ProgressBar.GONE);
                } else if (type.equals(STARTSYNCING)) {
                    Log.i(TAG, "contactSyncReceiver: STARTSYNCING");
                    syncSpinner.setVisibility(ProgressBar.VISIBLE);
                }

                updateGridViewPager();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_fav);
        setTopBarName(getString(R.string.phone_section_phonefavorites));
        syncSpinner = (ProgressBar) findViewById(R.id.syncSpinner);
        showBottomBar();

        initButtonListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(contactSyncReceiver, new IntentFilter(CONTACTS_SYNCED_ACTION));

        if (getDataProviderManager().getContactDataProvider().isBasicInfoSyncRunning()) {
            syncSpinner.setVisibility(ProgressBar.VISIBLE);
        } else {
            syncSpinner.setVisibility(ProgressBar.GONE);
        }

        updateGridViewPager();
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.unregisterReceiver(contactSyncReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showItemLongClickMenu(final long id) {
        Contact contact = getDataProviderManager().getContactDataProvider().getContactForId(id);
        final List<Phone> phoneNumbers = getDataProviderManager().getContactDataProvider().getPhoneForContact(contact);
        final String[] menuItems = { getString(R.string.phone_remove_favorite),
                getString(R.string.contextmenu_editcontact), getString(R.string.phone_change_defaultnumber) };
        int checked = 0;
        final List<CharSequence> itemsList = new ArrayList<CharSequence>(menuItems.length + phoneNumbers.size());
        final List<CharSequence> defaultNumberDialogItems = new ArrayList<CharSequence>(phoneNumbers.size());
        for (int i = 0; i < menuItems.length; i++) {
            itemsList.add(menuItems[i]);
        }
        for (int i = 0; i < phoneNumbers.size(); i++) {
            String number = String.format(getString(R.string.phone_calling), phoneNumbers.get(i).getNumber());
            if (phoneNumbers.get(i).isDefault()) {
                number += " " + getString(R.string.phone_defaultnumber);
                checked = i;
                defaultNumberDialogItems.add(phoneNumbers.get(i).getNumber() + " "
                        + getString(R.string.phone_defaultnumber));
            } else
                defaultNumberDialogItems.add(phoneNumbers.get(i).getNumber());

            if (!itemsList.contains(number)) {
                itemsList.add(number);
            }
        }
        final int finalChecked = checked;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_options));
        builder.setCancelable(true);
        builder.setItems(itemsList.toArray(new CharSequence[] {}), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                final Contact contact = getDataProviderManager().getContactDataProvider().getContactForId(id);

                switch (item) {
                case 0:
                    getDataProviderManager().getContactDataProvider().changeFavoriteState(contact, false);
                    updateGridViewPager();
                    break;
                case 1:
                    if (contact != null) {
                        CallSystemIntent.callEditContactIntent(PhoneFavoritesActivity.this,
                                contact != null ? Long.valueOf(contact.getId()) : -1, "tel", getDataProviderManager()
                                        .getContactDataProvider().getDefaultPhoneForContact(contact),
                                R.id.requestCode_sms_showOrCreateContactResult);
                    }
                    break;
                case 2:
                    AlertDialog.Builder builder = new AlertDialog.Builder(PhoneFavoritesActivity.this);
                    builder.setTitle(getString(R.string.phone_change_defaultnumber));
                    builder.setCancelable(true);

                    builder.setSingleChoiceItems(defaultNumberDialogItems.toArray(new CharSequence[] {}), finalChecked,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getDataProviderManager().getContactDataProvider().setDefaultPhoneForContact(
                                            contact, phoneNumbers.get(which));
                                    dialog.dismiss();
                                }
                            });
                    builder.create().show();
                    break;
                default:
                    String number = itemsList.get(item).toString().replaceAll("[^\\+0-9]", "");
                    if (number != null && number.length() > 0) {
                        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
                        startActivity(callIntent);
                    }
                    break;
                }
            }
        });
        builder.create().show();

    }

    private void updateGridViewPager() {
        gridViewPager = (GridViewPager) findViewById(R.id.gridviewpager);
        if (!gridViewPager.hasAdapter()) {
            Log.d(TAG, "updateGridViewPager: set new adapter");
            OnItemClickListener onItemClickListener = new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                    callDefaultNumberOfContactWithId(id);
                }
            };

            OnItemClickListener onPlusButtonClickListener = new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    addContactToFavorites();
                    Log.v(TAG, "Add Favorite");
                }
            };

            OnItemLongClickListener onItemLongClickListener = new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    showItemLongClickMenu(arg3);
                    return true;
                }
            };

            gridViewPager.setAdapter(new PhoneGridViewPagerAdapter(PhoneFavoritesActivity.this,
                    getDataProviderManager(), onItemClickListener, onItemLongClickListener, onPlusButtonClickListener));
        } else {
            Log.d(TAG, "updateGridViewPager: notifyDataSetChanged");
            gridViewPager.notifyDataSetChanged();
        }
    }

    public void callDefaultNumberOfContactWithId(long id) {
        Contact contact = getDataProviderManager().getContactDataProvider().getContactForId(id);
        String number = getDataProviderManager().getContactDataProvider().getDefaultPhoneForContact(contact);
        if (number != null && number.length() > 0) {
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
            startActivity(callIntent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initButtonListeners() {
        View dialpadButton = findViewById(R.id.leftButtonLayout);
        dialpadButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialpadButtonClicked();
            }
        });

        View addressBookButton = findViewById(R.id.rightButtonLayout);
        addressBookButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                addressBookButtonClicked();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == R.id.requestCode_sms_showOrCreateContactResult) {
            if (getDataProviderManager().getContactDataProvider().isBasicInfoSyncRunning()) {
                syncSpinner.setVisibility(ProgressBar.VISIBLE);
            } else {
                syncSpinner.setVisibility(ProgressBar.GONE);
            }

            // updateGridViewPager();
        }
    }

    public void addressBookButtonClicked() {
        Intent intent = new Intent(this, PhoneAddressbookListActivity.class);
        startActivity(intent);
    }

    private void dialpadButtonClicked() {
        Intent dial = new Intent();
        dial.setAction("android.intent.action.DIAL");
        startActivity(dial);
    }

    private void showBottomBar() {
        setBottomView(R.layout.global_bottombar);
    }

    @Override
    protected void initGestureLibrary() {
        gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.cldii_gestures);
        gestureLibrary.load();

    }

    @Override
    public StateModel getStateModel() {
        return new StateModelPhoneFavorites(PhoneFavoritesActivity.this, getDataProviderManager());
    }

    private void addContactToFavorites() {
        if (getDataProviderManager().getContactDataProvider().getPhoneContacts().size() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final View addFavoritesView = new PhoneDialogAddFavoriteLayout(this, getDataProviderManager());
            builder.setTitle(getString(R.string.dialog_phone_add_favorites));
            builder.setIcon(android.R.drawable.ic_menu_edit);
            builder.setView(addFavoritesView);

            builder.setPositiveButton(R.string.confirm_ok, new DialogInterface.OnClickListener() {

                @SuppressWarnings("unchecked")
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    List<Contact> markAsFavoriteContacts = ((PhoneDialogAddFavoritesAdapter) ((ListView) addFavoritesView
                            .findViewById(R.id.favoritesList)).getAdapter()).getMarkAsFavoritesList();
                    List<Contact> unmarkAsFavoriteContacts = ((PhoneDialogAddFavoritesAdapter) ((ListView) addFavoritesView
                            .findViewById(R.id.favoritesList)).getAdapter()).getUnmarkAsFavoritesList();

                    new SaveSelectedFavorites().execute(markAsFavoriteContacts, unmarkAsFavoriteContacts);

                    dialog.dismiss();
                }

            });

            builder.setNegativeButton(R.string.confirm_cancel, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            final AlertDialog alertDialog = builder.create();

            alertDialog.show();
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        } else {
            showPrompt(getString(R.string.phone_no_numbers));
        }
    }

    /**
     * Executes the mark/unmark as favorites process for contacts. Method
     * execute needs two lists containing contacts as parameters: List of
     * contacts to mark as favorite and list of contacts to unmark as favorite.
     * 
     * @author Sebastian Stallenberger, jambit GmbH
     * 
     */
    private class SaveSelectedFavorites extends AsyncTask<List<Contact>, Void, Boolean> {
        ProgressDialog progress;

        public SaveSelectedFavorites() {
            progress = new ProgressDialog(PhoneFavoritesActivity.this);
            progress.setMessage(getString(R.string.dialog_phone_save_favorites));
        }

        @Override
        protected void onPreExecute() {
            progress.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(List<Contact>... params) {
            boolean success = true;

            if (params[0] != null && params[1] != null) {
                List<Contact> markAsFavoriteContacts = params[0];
                List<Contact> unmarkAsFavoriteContacts = params[1];

                for (Contact contact : markAsFavoriteContacts) {
                    if (!getDataProviderManager().getContactDataProvider().changeFavoriteState(contact, true)) {
                        success = false;
                    }
                }

                for (Contact contact : unmarkAsFavoriteContacts) {
                    if (!getDataProviderManager().getContactDataProvider().changeFavoriteState(contact, false)) {
                        success = false;
                    }
                }
            } else {
                success = false;
            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                Log.d(TAG, "Fehler beim Speichern der Favoriten.");
                showPrompt(getString(R.string.dialog_phone_favorites_save_fail));
            }

            progress.dismiss();
            updateGridViewPager();

            gridViewPager.getViewPager().setCurrentItem(gridViewPager.getViewPager().getAdapter().getCount() - 1);

            super.onPostExecute(result);
        }
    }
}
package de.telekom.cldii.view.phone.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.telekom.cldii.R;
import de.telekom.cldii.data.contact.Contact;
import de.telekom.cldii.data.contact.IContactDataProvider;

/**
 * An extension of a BaseAdapter for the contacts ListView of the add favorites
 * dialog.
 * 
 * @author Sebastian Stalleberger, jambit GmbH
 * 
 */
public class PhoneDialogAddFavoritesAdapter extends BaseAdapter {
    IContactDataProvider dataProvider;
    private List<Contact> markAsFavoriteList;
    private List<Contact> unmarkAsFavoriteList;
    private Activity actitivy;

    public PhoneDialogAddFavoritesAdapter(IContactDataProvider dataProvider, Activity a) {
        super();
        this.dataProvider = dataProvider;
        this.markAsFavoriteList = new ArrayList<Contact>();
        this.unmarkAsFavoriteList = new ArrayList<Contact>();

        this.actitivy = a;
    }

    private List<Contact> getContactsToDisplay() {
        List<Contact> phoneContacts = new ArrayList<Contact>();
        for (Contact c : dataProvider.getPhoneContacts()) {
            if (c.hasPhone() && c.getDisplayName() != null) {
                phoneContacts.add(c);
            }
        }
        return phoneContacts;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return getContactsToDisplay().size();
    }

    @Override
    public Object getItem(int position) {
        return getContactsToDisplay().get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView returnView;

        Contact contact = (Contact) this.getItem(position);

        if (convertView != null) {
            returnView = (TextView) convertView;
        } else {
            final TextView textView = (TextView) this.actitivy.getLayoutInflater().inflate(
                    R.layout.phone_dialog_addfavorites_listentry, null);

            returnView = textView;
        }

        returnView.setText(contact.getDisplayName());

        if (contact.isFavorite()) {
            returnView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.d_checkbox_checked, 0);
        } else {
            returnView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.d_checkbox, 0);
        }

        if (markAsFavoriteList.contains(contact)) {
            returnView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.d_checkbox_checked, 0);
        }

        if (unmarkAsFavoriteList.contains(contact)) {
            returnView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.d_checkbox, 0);
        }

        return returnView;
    }

    /**
     * Toggles the selection state of a list item and fills the mark/unmark
     * lists.
     * 
     * @param position
     *            index of the list item
     */
    public void toggleSelection(Contact contact) {
        if (contact.isFavorite()) {
            if (unmarkAsFavoriteList.contains(contact)) {
                unmarkAsFavoriteList.remove(contact);
            } else {
                unmarkAsFavoriteList.add(contact);
            }
        } else {
            if (markAsFavoriteList.contains(contact)) {
                markAsFavoriteList.remove(contact);
            } else {
                markAsFavoriteList.add(contact);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Returns a list of contacts to mark as favorites.
     * 
     * @return
     */
    public List<Contact> getMarkAsFavoritesList() {
        return this.markAsFavoriteList;
    }

    /**
     * Returns a list of contacts to unmark as favorites.
     * 
     * @return
     */
    public List<Contact> getUnmarkAsFavoritesList() {
        return this.unmarkAsFavoriteList;
    }

}

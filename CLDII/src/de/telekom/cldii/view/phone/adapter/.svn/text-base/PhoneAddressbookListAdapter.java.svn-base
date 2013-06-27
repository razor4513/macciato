package de.telekom.cldii.view.phone.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.contact.Contact;

/**
 * A CursorAdapter for list of favorite contacts including contact image
 * 
 * @author Sebastian Stallenberger, jambit GmbH
 * 
 */

public class PhoneAddressbookListAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private IDataProviderManager dataProviderManager;
    private ContactLoader lazyLoader;
    private String filterString = "";

    private List<PhoneAddressbookListAdapterItem> listData;

    public PhoneAddressbookListAdapter(Context context, IDataProviderManager dataProviderManager,
            LayoutInflater layoutInflater) {
        super();

        this.layoutInflater = layoutInflater;
        this.dataProviderManager = dataProviderManager;
        this.lazyLoader = new ContactLoader(dataProviderManager, true);
        this.listData = new ArrayList<PhoneAddressbookListAdapter.PhoneAddressbookListAdapterItem>();

        populateListData(dataProviderManager.getContactDataProvider().getPhoneContacts());
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position).getData();
    }

    @Override
    public long getItemId(int position) {
        Object contact = listData.get(position).getData();
        if (contact instanceof Contact) {
            return (Long.valueOf(((Contact) contact).getId()));
        }
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PhoneAddressbookListAdapterItem item = listData.get(position);

        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.phone_list_entry, null);

            holder = new ViewHolder();
            holder.separatorLetterTextView = (TextView) convertView.findViewById(R.id.contactSeparatorLetter);
            holder.contactNameTextView = (TextView) convertView.findViewById(R.id.contactName);
            holder.contactNumberView = (TextView) convertView.findViewById(R.id.smsContentShort);

            holder.contactImageView = (ImageView) convertView.findViewById(R.id.contactThumbnail);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        switch (item.getItemType()) {
        case ENTRY:
            holder.separatorLetterTextView.setVisibility(View.GONE);
            convertView.findViewById(R.id.contactListEntry).setVisibility(View.VISIBLE);
            if (position > 0 && listData.get(position - 1).getItemType() == ListItemTypes.ENTRY)
                convertView.findViewById(R.id.contactsListDivider).setVisibility(View.VISIBLE);
            else
                convertView.findViewById(R.id.contactsListDivider).setVisibility(View.GONE);
            
            Contact contactItem = (Contact) item.getData();
            String currentDisplayName = contactItem.getDisplayName();
            if (currentDisplayName == null) {
                currentDisplayName = "";
            }

            // get and set set data
            holder.contactNameTextView.setText(currentDisplayName);

            if (holder.contactImageView != null) {
                lazyLoader.displayImage(Long.valueOf(contactItem.getId()), holder.contactImageView,
                        holder.contactNumberView);
            }

            break;
        case LETTER:
            holder.separatorLetterTextView.setVisibility(View.VISIBLE);
            convertView.findViewById(R.id.contactListEntry).setVisibility(View.GONE);
            convertView.findViewById(R.id.contactsListDivider).setVisibility(View.GONE);
            
            holder.separatorLetterTextView.setText((String) item.getData());
            break;
        }

        return convertView;

    }

    private void populateListData(List<Contact> contactItemList) {
        this.listData.clear();
        for (int i = 0; contactItemList != null && i < contactItemList.size(); i++) {
            // check if separator shall be visible
            String previousLetterString = "";
            String letterString = "";
            String currentDisplayName = contactItemList.get(i).getDisplayName();

            if (i > 0) {
                if (contactItemList.get(i - 1).getDisplayName() != null) {
                    previousLetterString = contactItemList.get(i - 1).getDisplayName()
                            .substring(0, contactItemList.get(i - 1).getDisplayName().length() > 0 ? 1 : 0)
                            .toUpperCase();
                }
            }

            letterString = currentDisplayName.substring(0, currentDisplayName.length() > 0 ? 1 : 0).toUpperCase();

            if (!letterString.equals(previousLetterString)) {
                listData.add(new PhoneAddressbookListAdapterItem(ListItemTypes.LETTER, letterString));
            }
            listData.add(new PhoneAddressbookListAdapterItem(ListItemTypes.ENTRY, contactItemList.get(i)));
        }

    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public List<Contact> getNameFilteredList(String filterString) {
        List<Contact> nameFilteredList = new ArrayList<Contact>(dataProviderManager.getContactDataProvider()
                .getPhoneContacts());

        for (Contact contact : new ArrayList<Contact>(nameFilteredList)) {
            // BAD
            if (contact.getDisplayName() == null
                    || !contact.getDisplayName().toLowerCase().contains(filterString.toLowerCase())) {
                nameFilteredList.remove(contact);
            }
        }

        populateListData(nameFilteredList);
        notifyDataSetChanged();

        return nameFilteredList;
    }

    public String getFilterString() {
        return filterString;
    }

    public void setFilterString(String filterString) {
        this.filterString = filterString;
        getNameFilteredList(filterString);
    }

    static class ViewHolder {
        TextView separatorLetterTextView;
        TextView contactNameTextView;
        TextView contactNumberView;

        ImageView contactImageView;
    }

    private enum ListItemTypes {
        LETTER, ENTRY
    }

    /**
     * An Object to that stores information for an
     * {@link PhoneAddressbookListAdapter}
     * 
     * @author Jun Chen, jambit GmbH
     * 
     */
    private class PhoneAddressbookListAdapterItem {
        private final ListItemTypes itemType;

        private final Object data;

        public PhoneAddressbookListAdapterItem(ListItemTypes itemType, Object data) {
            this.itemType = itemType;
            this.data = data;
        }

        /**
         * Returns the list item type
         * 
         * @return list item type
         */
        public ListItemTypes getItemType() {
            return this.itemType;
        }

        /**
         * Returns the data of item type
         * 
         * @return data of the item type
         */
        public Object getData() {
            return this.data;
        }

    }

}

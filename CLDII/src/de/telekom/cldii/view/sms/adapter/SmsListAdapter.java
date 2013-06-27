package de.telekom.cldii.view.sms.adapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.telekom.cldii.ApplicationConstants;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.contact.Contact;
import de.telekom.cldii.data.sms.SmsItem;
import de.telekom.cldii.widget.TextViewMultilineEllipse;

/**
 * A BaseAdapter for sms list
 * 
 * @author Sebastian Stallenberger, jambit GmbH
 * 
 */

public class SmsListAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private IDataProviderManager dataProviderManager;
    private Context context;
    private ContactLoader imageLoader;
    private LruCache<Contact, Bitmap> imageCache;
    private List<SmsListAdapterItem> listData;

    public SmsListAdapter(Context context, IDataProviderManager dataProviderManager, LayoutInflater layoutInflater) {
        super();

        this.context = context;
        this.layoutInflater = layoutInflater;
        this.dataProviderManager = dataProviderManager;
        this.imageCache = new LruCache<Contact, Bitmap>(ApplicationConstants.SMS_BITMAP_ITEM_CACHESIZE);
        this.imageLoader = new ContactLoader(dataProviderManager, imageCache);
        this.listData = new ArrayList<SmsListAdapter.SmsListAdapterItem>();

        populateListData(dataProviderManager.getSmsDataProvider().getSmsItemsOrderedByDate());
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
        Object item = listData.get(position).getData();
        if (item instanceof SmsItem)
            return ((SmsItem) item).getSmsId();
        else
            return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SmsListAdapterItem item = listData.get(position);

        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.sms_list_entry, null);

            holder = new ViewHolder();
            holder.smsSeparatorDateTextView = (TextView) convertView.findViewById(R.id.smsSeparatorDate);
            holder.contactNameTextView = (TextView) convertView.findViewById(R.id.contactName);
            View smsListEntry = convertView.findViewById(R.id.smsListEntry);
            holder.smsContentShortTextView = (TextViewMultilineEllipse) smsListEntry.findViewById(R.id.smsContentShort);
            holder.smsTimeTextView = (TextView) convertView.findViewById(R.id.smsTime);
            holder.contactImageView = (ImageView) convertView.findViewById(R.id.contactThumbnail);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.smsSeparatorDateTextView.setText(null);
        holder.contactNameTextView.setText(null);
        holder.smsContentShortTextView.setText(null);
        holder.smsTimeTextView.setText(null);
        holder.contactImageView.setImageDrawable(null);

        switch (item.getItemType()) {
        case ENTRY:
            holder.smsSeparatorDateTextView.setVisibility(View.GONE);
            convertView.findViewById(R.id.smsListEntry).setVisibility(View.VISIBLE);
            if (position > 0 && listData.get(position - 1).getItemType() == ListItemTypes.ENTRY)
                convertView.findViewById(R.id.smsListDivider).setVisibility(View.VISIBLE);
            else
                convertView.findViewById(R.id.smsListDivider).setVisibility(View.GONE);

            SmsItem smsItem = (SmsItem) item.getData();
            // check if message is read and set style
            if (smsItem.isRead()) {
                android.content.res.TypedArray styled = context
                        .obtainStyledAttributes(new int[] { R.attr.list_text_old });
                holder.smsContentShortTextView.setTextColor(styled.getColor(0, 0));
                holder.smsContentShortTextView.setTypeface(Typeface.DEFAULT);
                styled.recycle();
            } else {
                android.content.res.TypedArray styled = context
                        .obtainStyledAttributes(new int[] { R.attr.list_text_new });
                holder.smsContentShortTextView.setTextColor(styled.getColor(0, 0));
                holder.smsContentShortTextView.setTypeface(Typeface.DEFAULT_BOLD);
                styled.recycle();
            }

            Contact contact = dataProviderManager.getContactDataProvider().getContactForPhoneNumber(
                    smsItem.getSenderPhoneNumber());

            // get and set set data
            holder.contactNameTextView.setText((contact != null ? contact.getDisplayName() : smsItem
                    .getSenderPhoneNumber()));

            if (holder.contactImageView != null) {
                if (contact == null || imageCache.get(contact) == null) {
                    imageLoader.displayImage(contact, holder.contactImageView);
                } else {
                    holder.contactImageView.setImageBitmap(imageCache.get(contact));
                }
            }

            if (smsItem.getContent() != null && holder.smsContentShortTextView != null) {
                holder.smsContentShortTextView.setText(smsItem.getContent());
            }

            if (smsItem.getDate() != null && holder.smsTimeTextView != null) {
                String dateFormat = "HH:mm";
                SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                holder.smsTimeTextView.setText(sdf.format(smsItem.getDate()));
            }

            break;
        case DATE:
            holder.smsSeparatorDateTextView.setVisibility(View.VISIBLE);
            convertView.findViewById(R.id.smsListDivider).setVisibility(View.GONE);
            convertView.findViewById(R.id.smsListEntry).setVisibility(View.GONE);

            holder.smsSeparatorDateTextView.setText((String) item.getData());
            break;
        }

        return convertView;
    }

    public void clearImageCache() {
        imageCache.evictAll();
    }

    private void populateListData(List<SmsItem> smsItemList) {
        this.listData.clear();
        for (int i = 0; i < smsItemList.size(); i++) {
            // check if separator shall be visible
            String previousDateString = "";
            String dateString = "";
            DateFormat dateFormatSeparator = DateFormat.getDateInstance(DateFormat.FULL);
            if (i > 0) {
                previousDateString = dateFormatSeparator.format(smsItemList.get(i - 1).getDate());
            }

            dateString = dateFormatSeparator.format(smsItemList.get(i).getDate());

            if (!dateString.equals(previousDateString)) {
                this.listData.add(new SmsListAdapterItem(ListItemTypes.DATE, dateString));
            }

            this.listData.add(new SmsListAdapterItem(ListItemTypes.ENTRY, smsItemList.get(i)));
        }
    }

    @Override
    public void notifyDataSetChanged() {
        populateListData(dataProviderManager.getSmsDataProvider().getSmsItemsOrderedByDate());
        super.notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView smsSeparatorDateTextView;
        TextView contactNameTextView;
        TextViewMultilineEllipse smsContentShortTextView;

        TextView smsTimeTextView;

        ImageView contactImageView;
    }

    private enum ListItemTypes {
        DATE, ENTRY
    }

    /**
     * An Object to that stores information for an {@link SmsListAdapter}
     * 
     * @author Jun Chen, jambit GmbH
     * 
     */
    private class SmsListAdapterItem {
        private final ListItemTypes itemType;

        private final Object data;

        public SmsListAdapterItem(ListItemTypes itemType, Object data) {
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

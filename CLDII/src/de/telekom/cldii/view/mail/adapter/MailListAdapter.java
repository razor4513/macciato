package de.telekom.cldii.view.mail.adapter;

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
import de.telekom.cldii.data.mail.ICompactMail;
import de.telekom.cldii.data.mail.impl.ReceivedDateComparator;
import de.telekom.cldii.view.sms.adapter.ContactLoader;
import de.telekom.cldii.widget.TextViewMultilineEllipse;

/**
 * A BaseAdapter for displaying a list of mails
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */

public class MailListAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private Context context;
    private IDataProviderManager dataProviderManager;
    private ContactLoader imageLoader;

    private List<MailListAdapterItem> listData;
    private LruCache<Contact, Bitmap> imageCache;

    public MailListAdapter(Context context, IDataProviderManager dataProviderManager, LayoutInflater layoutInflater) {
        super();
        this.context = context;
        this.layoutInflater = layoutInflater;
        this.dataProviderManager = dataProviderManager;
        this.imageCache = new LruCache<Contact, Bitmap>(ApplicationConstants.MAIL_BITMAP_ITEM_CACHESIZE);
        this.imageLoader = new ContactLoader(dataProviderManager, imageCache);
        this.listData = new ArrayList<MailListAdapter.MailListAdapterItem>();

        populateListData(dataProviderManager.getMailDataProvider().getIncommingMails(new ReceivedDateComparator()));
    }

    @Override
    public int getCount() {
        return this.listData.size();
    }

    @Override
    public Object getItem(int position) {
        return this.listData.get(position).getData();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getEmailId(int position) {
        Object item = listData.get(position).getData();
        if (item instanceof ICompactMail)
            return ((ICompactMail) item).getId();
        else
            return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MailListAdapterItem item = listData.get(position);

        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.mail_list_entry, null);

            holder = new ViewHolder();
            holder.mailSeparatorDateTextView = (TextView) convertView.findViewById(R.id.mailSeparatorDate);
            holder.contactNameTextView = (TextView) convertView.findViewById(R.id.contactName);
            holder.mailContentShortTextView = (TextViewMultilineEllipse) convertView
                    .findViewById(R.id.mailContentShort);
            holder.mailTimeTextView = (TextView) convertView.findViewById(R.id.mailTime);
            holder.replyIcon = (ImageView) convertView.findViewById(R.id.replyImage);
            holder.contactImageView = (ImageView) convertView.findViewById(R.id.contactThumbnail);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.contactImageView.setImageDrawable(null);

        switch (item.getItemType()) {
        case ENTRY:
            holder.mailSeparatorDateTextView.setVisibility(View.GONE);
            convertView.findViewById(R.id.mailListEntry).setVisibility(View.VISIBLE);
            if (position > 0 && listData.get(position - 1).getItemType() == ListItemTypes.ENTRY)
                convertView.findViewById(R.id.mailListDivider).setVisibility(View.VISIBLE);
            else
                convertView.findViewById(R.id.mailListDivider).setVisibility(View.GONE);

            ICompactMail mailItem = (ICompactMail) item.getData();
            // check if message is read and set style
            if (mailItem.answered())
                holder.replyIcon.setVisibility(View.VISIBLE);
            else
                holder.replyIcon.setVisibility(View.GONE);

            // check if message is read and set style
            if (mailItem.read()) {
                android.content.res.TypedArray styled = context
                        .obtainStyledAttributes(new int[] { R.attr.header_title });
                // holder.contactNameTextView.setTextColor(styled.getColor(0,
                // 0));
                // holder.contactNameTextView.setTypeface(Typeface.DEFAULT);
                // holder.mailTimeTextView.setTextColor(styled.getColor(0, 0));
                // holder.mailTimeTextView.setTypeface(Typeface.DEFAULT);
                holder.mailContentShortTextView.setTextColor(styled.getColor(0, 0));
                holder.mailContentShortTextView.setTypeface(Typeface.DEFAULT);
                styled.recycle();
            } else {
                android.content.res.TypedArray styled = context
                        .obtainStyledAttributes(new int[] { R.attr.list_text_new });
                // holder.contactNameTextView.setTextColor(styled.getColor(0,
                // 0));
                // holder.contactNameTextView.setTypeface(Typeface.DEFAULT_BOLD);
                // holder.mailTimeTextView.setTextColor(styled.getColor(0, 0));
                // holder.mailTimeTextView.setTypeface(Typeface.DEFAULT_BOLD);
                holder.mailContentShortTextView.setTextColor(styled.getColor(0, 0));
                holder.mailContentShortTextView.setTypeface(Typeface.DEFAULT_BOLD);
                styled.recycle();

            }

            Contact contact = dataProviderManager.getContactDataProvider().getContactForEmail(mailItem.getFromAdress());

            // get and set set data
            holder.contactNameTextView.setText((contact != null ? contact.getDisplayName() : mailItem.getFromAdress()));

            if (holder.contactImageView != null) {
                if (contact == null || imageCache.get(contact) == null) {
                    imageLoader.displayImage(contact, holder.contactImageView);
                } else {
                    holder.contactImageView.setImageBitmap(imageCache.get(contact));
                }
            }

            if (mailItem.getSubject() != null && holder.mailContentShortTextView != null) {
                holder.mailContentShortTextView.setText(mailItem.getSubject());
            }

            if (mailItem.getRecievedDate() != null && holder.mailTimeTextView != null) {
                String dateFormat = "HH:mm";
                SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                holder.mailTimeTextView.setText(sdf.format(mailItem.getRecievedDate()));
            }

            break;
        case DATE:
            holder.mailSeparatorDateTextView.setVisibility(View.VISIBLE);
            convertView.findViewById(R.id.mailListEntry).setVisibility(View.GONE);
            convertView.findViewById(R.id.mailListDivider).setVisibility(View.GONE);
            holder.mailSeparatorDateTextView.setText((String) item.getData());
            break;
        }

        return convertView;
    }

    private void populateListData(List<ICompactMail> mailItemList) {
        this.listData.clear();
        for (int i = 0; i < mailItemList.size(); i++) {
            // check if separator shall be visible
            String previousDateString = "";
            String dateString = "";
            DateFormat dateFormatSeparator = DateFormat.getDateInstance(DateFormat.FULL);
            if (i > 0) {
                previousDateString = dateFormatSeparator.format(mailItemList.get(i - 1).getRecievedDate());
            }

            dateString = dateFormatSeparator.format(mailItemList.get(i).getRecievedDate());

            if (!dateString.equals(previousDateString)) {
                this.listData.add(new MailListAdapterItem(ListItemTypes.DATE, dateString));
            }
            this.listData.add(new MailListAdapterItem(ListItemTypes.ENTRY, mailItemList.get(i)));
        }
    }

    public void refreshListView() {
        populateListData(dataProviderManager.getMailDataProvider().getIncommingMails(new ReceivedDateComparator()));
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView mailSeparatorDateTextView;
        TextView contactNameTextView;
        TextViewMultilineEllipse mailContentShortTextView;

        ImageView replyIcon;
        TextView mailTimeTextView;

        ImageView contactImageView;
    }

    private enum ListItemTypes {
        DATE, ENTRY
    }

    /**
     * An Object to that stores information for an {@link MailListAdapter}
     * 
     * @author Jun Chen, jambit GmbH
     * 
     */
    private class MailListAdapterItem {
        private final ListItemTypes itemType;

        private final Object data;

        public MailListAdapterItem(ListItemTypes itemType, Object data) {
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

    public void clearImageCache() {
        imageCache.evictAll();
    }

}

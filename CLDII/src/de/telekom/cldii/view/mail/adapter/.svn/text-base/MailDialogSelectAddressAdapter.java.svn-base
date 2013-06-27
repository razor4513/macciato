package de.telekom.cldii.view.mail.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.telekom.cldii.R;
import de.telekom.cldii.data.contact.EmailNamePair;
import de.telekom.cldii.data.contact.IContactDataProvider;

/**
 * A BaseAdapter responsible to create the list view displaying a list of emails
 * inside of a alert dialog
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */
public class MailDialogSelectAddressAdapter extends BaseAdapter {
    private LayoutInflater viewInflater;
    private int checked = -1;
    private List<EmailNamePair> emailAddressList;

    public MailDialogSelectAddressAdapter(IContactDataProvider dataProvider, Activity a) {
        super();
        // cache because list in background can change while loading ->
        // indexOutOfBounds
        emailAddressList = new ArrayList<EmailNamePair>(dataProvider.getEmailAddresses());
        this.viewInflater = a.getLayoutInflater();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return emailAddressList.size();
    }

    @Override
    public Object getItem(int position) {
        return emailAddressList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = viewInflater.inflate(R.layout.sms_dialog_selectnumber_listentry, null);

            viewHolder = new ViewHolder();
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.contactNameTextView);
            viewHolder.infoTextView = (TextView) convertView.findViewById(R.id.contactInfoTextView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (position == checked)
            viewHolder.infoTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.d_checkbox_checked, 0);
        else
            viewHolder.infoTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.d_checkbox, 0);

        EmailNamePair pair = emailAddressList.get(position);
        viewHolder.nameTextView.setText(pair.getName());
        viewHolder.infoTextView.setText(pair.getEmail());

        return convertView;
    }

    public void setChecked(int position) {
        this.checked = position;
        notifyDataSetChanged();
    }

    public String getEmail() {
        if (checked >= 0 && checked < emailAddressList.size())
            return emailAddressList.get(checked).getEmail();
        else
            return null;
    }

    static class ViewHolder {
        TextView nameTextView;
        TextView infoTextView;
    }

}

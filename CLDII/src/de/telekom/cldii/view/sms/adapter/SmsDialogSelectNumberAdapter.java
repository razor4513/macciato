package de.telekom.cldii.view.sms.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.telekom.cldii.R;
import de.telekom.cldii.data.contact.IContactDataProvider;
import de.telekom.cldii.data.contact.PhoneNamePair;

/**
 * A BaseAdapter responsible to create the entries of a listview displaying sms
 * phone numbers
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */
public class SmsDialogSelectNumberAdapter extends BaseAdapter {
    private LayoutInflater viewInflater;
    private int checked = -1;
    private List<PhoneNamePair> phoneNumbersList;

    public SmsDialogSelectNumberAdapter(IContactDataProvider dataProvider, Activity a) {
        super();
        // cache because list in background can change while loading ->
        // indexOutOfBounds
        phoneNumbersList = new ArrayList<PhoneNamePair>(dataProvider.getPhoneNumbers());
        this.viewInflater = a.getLayoutInflater();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return phoneNumbersList.size();
    }

    @Override
    public Object getItem(int position) {
        return phoneNumbersList.get(position);
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

        PhoneNamePair pair = phoneNumbersList.get(position);
        viewHolder.nameTextView.setText(pair.getName());
        viewHolder.infoTextView.setText(pair.getPhone());

        return convertView;
    }

    public void setChecked(int position) {
        this.checked = position;
        notifyDataSetChanged();
    }

    public String getNumber() {
        if (checked >= 0 && checked < phoneNumbersList.size())
            return phoneNumbersList.get(checked).getPhone();
        else
            return null;
    }
    
    static class ViewHolder {
        TextView nameTextView;
        TextView infoTextView;
    }
}

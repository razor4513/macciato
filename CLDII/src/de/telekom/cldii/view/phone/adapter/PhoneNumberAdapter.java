package de.telekom.cldii.view.phone.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.telekom.cldii.R;
import de.telekom.cldii.data.contact.Contact.Phone;

public class PhoneNumberAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private List<Phone> phoneNumberList;

    public PhoneNumberAdapter(Context context, LayoutInflater inflater, List<Phone> phoneNumberList) {
        this.context = context;
        this.phoneNumberList = phoneNumberList;
        this.inflater = inflater;
    }

    @Override
    public int getCount() {
        if (this.phoneNumberList != null) {
            return this.phoneNumberList.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return this.phoneNumberList.get(position).getNumber();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout returnLayout = (LinearLayout) inflater.inflate(R.layout.phone_list_detail_phonenumberlayout, null);
        TextView phoneNumberTypeTextView = (TextView) returnLayout.findViewById(R.id.numberTypeTextView);
        String typeText = "";
        switch (phoneNumberList.get(position).getType()) {
        case HOME:
            typeText = context.getString(R.string.phone_number_home);
            break;

        case HOME_FAX:
            typeText = context.getString(R.string.phone_number_homefax);
            break;

        case MOBILE:
            typeText = context.getString(R.string.phone_number_mobile);
            break;

        case OTHER:
            typeText = context.getString(R.string.phone_number_other);
            break;

        case WORK:
            typeText = context.getString(R.string.phone_number_work);
            break;

        case WORK_FAX:
            typeText = context.getString(R.string.phone_number_workfax);
            break;

        case WORK_MOBILE:
            typeText = context.getString(R.string.phone_number_workmobile);
            break;

        default:
            typeText = context.getString(R.string.phone_number_unknown);
            break;
        }
        phoneNumberTypeTextView.setText(typeText + ":");
        TextView phoneNumberTextView = (TextView) returnLayout.findViewById(R.id.numberTextView);
        phoneNumberTextView.setText(phoneNumberList.get(position).getNumber());

        return returnLayout;
    }

}

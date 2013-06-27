package de.telekom.cldii.view.phone;

import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_CONTACT_ID;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import de.telekom.cldii.CldApplication;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.contact.Contact;
import de.telekom.cldii.view.phone.adapter.PhoneNumberAdapter;

/**
 * Dialog styled activity displaying all phone numbers of a contact
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */

public class PhoneAddressbookDetailsActivity extends Activity {
    private long contactId;
    private ListView contactNumbersListView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTheme(((CldApplication) getApplication()).getThemeResId());
        setContentView(R.layout.phone_list_details);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(EXTRAS_KEY_CONTACT_ID)) {
            this.contactId = extras.getLong(EXTRAS_KEY_CONTACT_ID);
        } else {
            finish();
        }
        Contact contact = ((IDataProviderManager) getApplication()).getContactDataProvider().getContactForId(contactId);
        if (contact != null) {
            TextView contactName = (TextView) findViewById(R.id.contactNameTextView);
            contactName.setText(contact.getDisplayName());
        }

        contactNumbersListView = (ListView) findViewById(R.id.contactNumbersListView);
        contactNumbersListView.setAdapter(new PhoneNumberAdapter(this, getLayoutInflater(),
                ((IDataProviderManager) getApplication()).getContactDataProvider().getPhoneForContact(contact)));

        contactNumbersListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                callPhoneNumber((String) arg0.getItemAtPosition(arg2));
            }

        });
    }

    private void callPhoneNumber(String phoneNumber) {
        if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
            startActivityForResult(callIntent, R.id.requestCode_phone_addressbook_returnfromcall);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == R.id.requestCode_phone_addressbook_returnfromcall) {
            finish();
        }
    }
}

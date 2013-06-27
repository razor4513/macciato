package de.telekom.cldii.view.phone;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.contact.Contact;
import de.telekom.cldii.view.phone.adapter.PhoneDialogAddFavoritesAdapter;

/**
 * This view is designed to be filled into an alert dialog in order to add
 * contacts to favorites.
 * 
 * @author Sebastian Stallenberger, jambit GmbH
 * 
 */
public class PhoneDialogAddFavoriteLayout extends LinearLayout {
    private View layout;
    private Context context;
    private IDataProviderManager dataProviderManager;

    public PhoneDialogAddFavoriteLayout(Context context, IDataProviderManager dataProviderManager) {
        super(context);
        this.context = context;
        this.dataProviderManager = dataProviderManager;

        inflateLayout();
    }

    /* Private method to inflate the xml */
    private final void inflateLayout() {
        LayoutInflater inflater = LayoutInflater.from(context);
        this.layout = inflater.inflate(R.layout.phone_dialog_addfavorites, null, false);
        this.addView(layout);

        initListeners();
    }

    private void initListeners() {
        final ListView listView = (ListView) this.layout.findViewById(R.id.favoritesList);

        listView.setAdapter(new PhoneDialogAddFavoritesAdapter(dataProviderManager.getContactDataProvider(),
                (Activity) context));
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
                PhoneDialogAddFavoritesAdapter adapter = (PhoneDialogAddFavoritesAdapter) arg0.getAdapter();
                Contact contact = (Contact) adapter.getItem(index);
                adapter.toggleSelection(contact);
            }
        });

    }
}

package de.telekom.cldii.view.phone.adapter;

import android.graphics.Bitmap;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.contact.Contact;
import de.telekom.cldii.widget.grid.AbstractGridCellContent;
import de.telekom.cldii.widget.grid.IGridCellContent;

/**
 * Cell content for a single contact.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public class PhoneGridCellContent extends AbstractGridCellContent implements IGridCellContent {
    
    /**
     * Holds a Contact object from the addressbook "lib"
     */
    private Contact contact;
    private IDataProviderManager dataProviderManager;
    private Bitmap contactImage;
    private Bitmap defaultBitmap;
    /**
     * Creates a new PhoneContact from a Contact object
     * @param contact a Contact object
     */
    public PhoneGridCellContent(Contact contact, IDataProviderManager dataProviderManager, Bitmap defaultBitmap) {
        this.contact = contact;
        this.dataProviderManager = dataProviderManager;
        contactImage = null;
        this.defaultBitmap = defaultBitmap;
    }

    @Override
    public String getText() {
        return contact.getDisplayName();
    }

    @Override
    public Bitmap getImage() {
    	if(contactImage == null) {
    		contactImage = dataProviderManager.getContactDataProvider().getContactPhoto(contact);
    		if (contactImage == null)
    		    contactImage = defaultBitmap;
    	}
    	
        return contactImage;
    }

    @Override
    public long getId() {
        return Long.parseLong(contact.getId());
    }

    @Override
    public Integer getIndicatorCount() {
        // Phone contacts have no indicator
        return null;
    }

    @Override
    public int getLayoutId() {
        return R.layout.phone_fav_entry;
    }

}

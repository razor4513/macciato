package de.telekom.cldii.view.main.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.widget.grid.AbstractGridCellContent;
import de.telekom.cldii.widget.grid.IGridCellContent;

/**
 * Factory for the grid cell contents of the main view.
 * 
 * @author Christoph HŸbner
 */
public class MainGridCellContent extends AbstractGridCellContent implements IGridCellContent {

    /**
     * "Identifier"
     */
    public static enum OptionType { TYPE_PHONE, TYPE_SMS, TYPE_EMAIL, TYPE_NEWS };
        
    /**
     * Stores the text of the item
     */
    private String text;
    
    /**
     * Stores the image of the item
     */
    private Bitmap image;
    
    /**
     * Stores the type of the item
     */
    private OptionType type;
    
    private Integer indicatorCount;
    
    /**
     * Creates a new StartScreenOption
     * @param text  The menu text
     * @param image The image
     * @param type    The type of the menu item
     */
    protected MainGridCellContent(String text, Bitmap image, OptionType type, Integer indicatorCount) {
        this.text = text;
        this.image = image;
        this.type = type;
        this.indicatorCount = indicatorCount;
    }
    
    /**
     * The type of this item
     * @return type
     */
    public OptionType getType() {
        return type;
    }
    
    /* (non-Javadoc)
     * @see de.telekom.cldii.data.IPageContentItem#getText()
     */
    @Override
    public String getText() {
        return text;
    }

    /* (non-Javadoc)
     * @see de.telekom.cldii.data.IPageContentItem#getImage()
     */
    @Override
    public Bitmap getImage() {
        return image;
    }

    /* (non-Javadoc)
     * @see de.telekom.cldii.data.IPageContentItem#getId()
     */
    @Override
    public long getId() {
        return type.ordinal();
    }

    /* (non-Javadoc)
     * @see de.telekom.cldii.data.IPageContentItem#getIndicatorCount()
     */
    @Override
    public Integer getIndicatorCount() {
        // Standard Options have no count
        return indicatorCount;
    }
    
    @Override
    public int getLayoutId() {
        return R.layout.main_menu_plate;
    } 
    
    /**
     * Factory method to get a option item depending to the option type
     * @param type option type
     * @return according option item
     */
    public static MainGridCellContent getOption(OptionType type, Context context, IDataProviderManager dataProviderManager, OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener) {
        
        int textId = 0; 
        int imageId = 0;
        Integer indicatorCount = null;
        
        switch(type) {
        case TYPE_PHONE:
            textId = R.string.section_phone;
            imageId = R.drawable.icon_call;
            break;
            
        case TYPE_EMAIL:
            textId = R.string.section_email;
            imageId = R.drawable.icon_mail;
            indicatorCount = dataProviderManager.getMailDataProvider().getUnreadMails();
            break;

        case TYPE_SMS:
            textId = R.string.section_sms;
            imageId = R.drawable.icon_sms;
            indicatorCount = dataProviderManager.getSmsDataProvider().getUnreadSmsCount();
            break;
            
        case TYPE_NEWS:
            textId = R.string.section_news;
            imageId = R.drawable.icon_news;
            indicatorCount = dataProviderManager.getNewsDataProvider().getNumberOfUnreadNewsItems();
        }
        
        MainGridCellContent cellContent = new MainGridCellContent(context.getString(textId), BitmapFactory.decodeResource(context.getResources(), imageId), type, indicatorCount);
        cellContent.setOnItemClickListener(onItemClickListener);
        cellContent.setOnItemLongClickListener(onItemLongClickListener);
        return cellContent;
    }

}

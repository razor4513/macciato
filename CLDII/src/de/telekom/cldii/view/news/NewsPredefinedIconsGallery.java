package de.telekom.cldii.view.news;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import de.telekom.cldii.ApplicationConstants;
import de.telekom.cldii.R;

/**
 * Dialog Activity that shows all predefined icons in a {@link GridView} for
 * selection as a category icon
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */
public class NewsPredefinedIconsGallery extends Activity {
    private GridView gridView;
    private Integer[] iconsResourceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_dialog_predefinedgallery);

        iconsResourceList = ApplicationConstants.PREDEFINED_ICONS;

        this.gridView = (GridView) findViewById(R.id.predefinedIconsGridView);
        this.gridView.setAdapter(new IconAdapter(this));
        this.gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Intent result = new Intent();
                result.setData(Uri.parse("android.resource://" + getPackageName() + "/" + arg3));
                setResult(R.id.requestCode_predefinedGallery, result);
                finish();
            }
        });
    }

    /**
     * Private extension of a {@link BaseAdapter} for displaying predefined
     * icons in a {@link GridView}
     * 
     * @author Jun Chen, jambit GmbH
     * 
     */
    private class IconAdapter extends BaseAdapter {
        private Context context;

        public IconAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return iconsResourceList.length;
        }

        @Override
        public Object getItem(int position) {
            return iconsResourceList[position];
        }

        @Override
        public long getItemId(int position) {
            return iconsResourceList[position];
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(80, 80));
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(iconsResourceList[position]);
            return imageView;
        }

    }
}

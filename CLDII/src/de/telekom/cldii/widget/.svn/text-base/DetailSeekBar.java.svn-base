package de.telekom.cldii.widget;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import de.telekom.cldii.R;


/**
 * This SeekBar displays/uses an overlay which is updated using a text provided
 * by a {@link SeekBarAdapter}.
 * 
 * In order to work correctly, either an item with ID seekBarOverlayView and
 * seekBarOverlayTextView must exist on the parent or must be set manually using
 * {@link DetailSeekBar#setOverlayTextView(TextView)} and
 * {@link DetailSeekBar#setOverlayView(View)}.
 * 
 * Sample layout:
 * 
 * <pre>
 *     &lt;RelativeLayout
 *         android:layout_width="fill_parent"
 *         android:layout_height="fill_parent" >
 * 
 *         &lt;de.telekom.cldii.widget.SeekBar
 *             android:id="@+id/seekBar"
 *             android:layout_width="fill_parent"
 *             android:layout_height="wrap_content"
 *             android:layout_alignParentBottom="true" />
 * 
 *         &lt;LinearLayout
 *             android:id="@+id/content"
 *             android:layout_width="fill_parent"
 *             android:layout_height="fill_parent"
 *             android:layout_above="@id/seekBar"
 *             android:orientation="vertical" >
 * 
 *             &lt;!-- TODO: place the content here, e.g. the news title and text-->
 * 
 *         &lt;/LinearLayout>
 * 
 *         &lt;LinearLayout
 *             android:id="@+id/seekBarOverlayView"
 *             android:layout_width="fill_parent"
 *             android:layout_height="wrap_content"
 *             android:layout_above="@id/seekBar"
 *             android:layout_marginLeft="20dp"
 *             android:layout_marginRight="20dp"
 *             android:background="#800000ff"
 *             android:padding="10dp" >
 * 
 *             &lt;TextView
 *                 android:id="@+id/seekBarOverlayTextView"
 *                 android:layout_width="fill_parent"
 *                 android:layout_height="fill_parent"
 *                 android:gravity="center"
 *                 android:text="details" />
 *         &lt;/LinearLayout>
 *     &lt;/RelativeLayout>
 * </pre>
 * 
 * @author Marco Pfattner, jambit GmbH
 */
public class DetailSeekBar extends android.widget.SeekBar {

	private OnSeekBarChangeListener listener;
	private boolean isSeeking = false;
	/**
	 * Updates the overlays and forwards the events to the listener set by the
	 * user of this SeekBar.
	 */
	private android.widget.SeekBar.OnSeekBarChangeListener internalListener = new android.widget.SeekBar.OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
		    isSeeking = false;
			if (listener != null) {
				listener.onStopTrackingTouch(seekBar);
			}
			if (overlayView != null) {
				overlayView.setVisibility(View.GONE);
			}
			if (adapter != null) {
				adapter.onItemSelected(seekBar.getProgress());
			}
		}

		@Override
		public void onStartTrackingTouch(android.widget.SeekBar seekBar) {
		    isSeeking = true;
			if (listener != null) {
				listener.onStartTrackingTouch(seekBar);
			}
			if (overlayView != null) {
				updateOverlay(seekBar.getProgress());
				overlayView.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onProgressChanged(android.widget.SeekBar seekBar,
				int progress, boolean fromUser) {
			if (listener != null) {
				listener.onProgressChanged(seekBar, progress, fromUser);
			}
			updateOverlay(progress);
		}

		private void updateOverlay(int progress) {
			if (overlayTextView != null) {
				if (adapter != null) {
					String name = String.valueOf(adapter.getItem(progress));
					overlayTextView.setText(Html.fromHtml(name));
				} else {
					overlayTextView.setText("No SeekBarAdapter set");
				}
			}
		}
	};

	public static interface SeekBarAdapter {
		/**
		 * Get the name associated with the specified position in the data set.
		 * This name will be displayed in the overlay view.
		 * 
		 * @return The name at the specified position.
		 */
		String getItem(int position);

		/**
		 * How many items are in the data set represented by this Adapter.
		 * 
		 * @return Count of items.
		 */
		int getCount();

		/**
		 * Notification that the progress level has changed.
		 * 
		 * @param position
		 */
		void onItemSelected(int position);
	}

	private SeekBarAdapter adapter;
	private View overlayView;
	private TextView overlayTextView;

	public DetailSeekBar(Context context) {
		super(context);
		init();
	}

	public DetailSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DetailSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void setAdapter(SeekBarAdapter adapter) {
		this.adapter = adapter;
		setMax(adapter.getCount() - 1);
	}

	/**
	 * Sets the overlayView which is made visible/invidible depending on the
	 * touch state of the seek bar.
	 */
	public void setOverlayView(View overlayView) {
		this.overlayView = overlayView;
	}

	/**
	 * Sets the overlayTextView which is used to display the item name.
	 */
	public void setOverlayTextView(TextView overlayTextView) {
		this.overlayTextView = overlayTextView;
	}

	private void init() {
		setMax(0);
		super.setOnSeekBarChangeListener(internalListener);
	}

	@Override
	/**
	 * Additionally to layouting tries to find seekBarOverlayView and seekBarOverlayTextView.
	 */
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		// find the other views
		View parent = (View) getParent().getParent();

		overlayView = parent.findViewById(R.id.seekBarOverlayView);
		overlayTextView = (TextView) parent
				.findViewById(R.id.seekBarOverlayTextView);
		if (!isSeeking) {
            overlayView.setVisibility(View.GONE);
        }
	}

	@Override
	public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
		this.listener = l;
	}

}

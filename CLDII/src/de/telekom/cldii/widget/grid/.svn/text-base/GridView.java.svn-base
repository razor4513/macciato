package de.telekom.cldii.widget.grid;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.telekom.cldii.R;

public class GridView extends LinearLayout {
    private GridViewContent gridpageContent;
    private List<View> cellViews;
    private Integer[] indicatorCount;

    // need all constructor params for refresh method
    private Context context;
    private int maxIndicatorCount = 99;
    private String replacementAfterMaxIndicatorCount = "99+";

    private int[] resources = { R.id.topLeftGridItem, R.id.topRightGridItem, R.id.bottomLeftGridItem,
            R.id.bottomRightGridItem };

    public GridView(Context context, GridViewContent gridpageContent) {
        super(context);
        inflate(context, R.layout.gridview_page, this);

        this.gridpageContent = gridpageContent;
        this.context = context;

        initializeGridPageView(context);

    }

    private void initializeGridPageView(Context context) {
        cellViews = new ArrayList<View>();
        indicatorCount = new Integer[gridpageContent.getNoOfCells()];

        if (gridpageContent != null) {
            for (int viewIndex = 0; viewIndex < gridpageContent.getNoOfCells(); viewIndex++) {
                final IGridCellContent gridElementData = gridpageContent.getCellContent(viewIndex);

                final View view = ((Activity) context).getLayoutInflater().inflate(gridElementData.getLayoutId(), null);
                cellViews.add(view);

                initializePageContent(view, gridElementData, viewIndex);
            }
        }
    }

    private void initializePageContent(View view, final IGridCellContent gridElementData, final int currentViewIndex) {
        // Get layout elements
        TextView plateTextView = (TextView) view.findViewById(R.id.plateTextView);
        ImageView plateImageView = (ImageView) view.findViewById(R.id.plateImageView);
        TextView indicatorTextView = (TextView) view.findViewById(R.id.plateBubbleTextView);

        // Set new data
        if (plateTextView != null)
            plateTextView.setText(gridElementData.getText());

        if (plateImageView != null) {
            Bitmap plateBitmap = gridElementData.getImage();
            if (plateBitmap != null) {
                plateImageView.setImageBitmap(plateBitmap);
            }
        }

        if (indicatorTextView != null) {
            indicatorCount[currentViewIndex] = gridElementData.getIndicatorCount();
            updateIndicatorCount(indicatorTextView, indicatorCount[currentViewIndex]);
        }

        view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (gridElementData.getOnItemClickListener() != null)
                    gridElementData.getOnItemClickListener().onItemClick(null, v, currentViewIndex,
                            gridElementData.getId());
            }
        });

        view.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                if (gridElementData.getOnItemLongClickListener() != null)
                    return gridElementData.getOnItemLongClickListener().onItemLongClick(null, v, currentViewIndex,
                            gridElementData.getId());
                return false;
            }
        });

        ((FrameLayout) findViewById(resources[currentViewIndex % resources.length])).addView(view);
    }

    public void updatePageContent(GridViewContent gridpageContent) {
        this.indicatorCount = new Integer[gridpageContent.getNoOfCells()];
        for (int gridCount = 0; gridCount < gridpageContent.getNoOfCells(); gridCount++) {
            // clean up when elements removed
            for (int i = cellViews.size() - 1; i >= gridpageContent.getNoOfCells(); i--) {
                ((FrameLayout) findViewById(resources[i % resources.length])).removeAllViews();
                cellViews.remove(i);
            }

            final IGridCellContent gridElementData = gridpageContent.getCellContent(gridCount);

            View cellView;
            // compare it to the old page to detect content element changes
            if (this.gridpageContent.getNoOfCells() <= gridCount
                    || !(this.gridpageContent.getCellContent(gridCount).getId() == gridpageContent.getCellContent(
                            gridCount).getId())) {
                cellView = ((Activity) context).getLayoutInflater().inflate(gridElementData.getLayoutId(), null);
                if (gridCount > cellViews.size() - 1) {
                    cellViews.add(cellView);
                } else {
                    cellViews.set(gridCount, cellView);
                    ((FrameLayout) findViewById(resources[gridCount % resources.length])).removeAllViews();
                }

                initializePageContent(cellView, gridElementData, gridCount);
            } else {
                cellView = cellViews.get(gridCount);

                // update indicator counter
                TextView indicatorTextView = (TextView) cellView.findViewById(R.id.plateBubbleTextView);
                if (indicatorTextView != null) {
                    indicatorCount[gridCount] = gridElementData.getIndicatorCount();
                    if (!indicatorTextView.getText().toString()
                            .equals(String.valueOf(gridElementData.getIndicatorCount())))
                        updateIndicatorCount(indicatorTextView, indicatorCount[gridCount]);
                }

                // update item text
                TextView plateTextView = (TextView) cellView.findViewById(R.id.plateTextView);
                if (plateTextView != null) {
                    if (!plateTextView.getText().toString().equals(gridElementData.getText()))
                        plateTextView.setText(gridElementData.getText());
                }

                // update item icon
                ImageView plateImageView = (ImageView) cellView.findViewById(R.id.plateImageView);
                if (plateImageView != null) {
                    Bitmap plateBitmap = gridElementData.getImage();
                    if (plateBitmap != null) {
                        plateImageView.setImageBitmap(plateBitmap);
                    }

                }
            }
        }
        this.gridpageContent = gridpageContent;
    }

    /**
     * Triggers the incrementation of the indicator counter for the content item
     * with the given id. If no content item with the given id exists in this
     * view the call is ignored.
     * 
     * @param id
     *            id of the content item that has benn updated
     */
    public void increaseCounterForContentItemWithId(long id) {
        for (int contentCount = 0; (contentCount < gridpageContent.getNoOfCells() && contentCount < indicatorCount.length); contentCount++) {
            IGridCellContent contentItem = gridpageContent.getCellContent(contentCount);
            if (contentItem.getId() == id) {
                if (indicatorCount[contentCount] != null) {
                    View plateToUpdate = cellViews.get(contentCount);
                    indicatorCount[contentCount] = new Integer(indicatorCount[contentCount] + 1);
                    TextView indicatorTextView = (TextView) plateToUpdate.findViewById(R.id.plateBubbleTextView);
                    if (indicatorTextView != null)
                        updateIndicatorCount(indicatorTextView, indicatorCount[contentCount]);
                }
            }
        }
    }

    public void synchronizeIndicatorCount() {
        for (int contentCount = 0; (contentCount < gridpageContent.getNoOfCells() && contentCount < indicatorCount.length); contentCount++) {
            IGridCellContent contentItem = gridpageContent.getCellContent(contentCount);
            indicatorCount[contentCount] = contentItem.getIndicatorCount();
            View plateToUpdate = cellViews.get(contentCount);
            TextView indicatorTextView = (TextView) plateToUpdate.findViewById(R.id.plateBubbleTextView);
            if (indicatorTextView != null)
                updateIndicatorCount(indicatorTextView, indicatorCount[contentCount]);
        }
    }

    /**
     * @param maxIndicatorCount
     *            the maxIndicatorCount to set
     */
    public void setMaxIndicatorCount(int maxIndicatorCount) {
        this.maxIndicatorCount = maxIndicatorCount;
    }

    /**
     * @param replacementAfterMaxIndicatorCount
     *            the replacementAfterMaxIndicatorCount to set
     */
    public void setReplacementAfterMaxIndicatorCount(String replacementAfterMaxIndicatorCount) {
        this.replacementAfterMaxIndicatorCount = replacementAfterMaxIndicatorCount;
    }

    private void updateIndicatorCount(TextView indicatorView, Integer indicatorCount) {
        if (indicatorCount != null && indicatorCount != 0) {
            if (indicatorView.getVisibility() != TextView.VISIBLE) {
                indicatorView.setVisibility(VISIBLE);
            }
            if (indicatorCount > maxIndicatorCount) {
                indicatorView.setText(replacementAfterMaxIndicatorCount);
            } else {
                indicatorView.setText(indicatorCount.toString());
            }
        } else {
            indicatorView.setVisibility(GONE);
        }
    }

}

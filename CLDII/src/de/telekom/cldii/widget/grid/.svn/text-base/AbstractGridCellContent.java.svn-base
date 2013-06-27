package de.telekom.cldii.widget.grid;

import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * Abstract class for all grid cell content that handles the
 * {@link OnItemClickListener} and {@link OnItemLongClickListener}
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */
public abstract class AbstractGridCellContent implements IGridCellContent {

    private OnItemClickListener onClickListener;

    private OnItemLongClickListener onLongClickListener;

    @Override
    public OnItemClickListener getOnItemClickListener() {
        return onClickListener;
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public OnItemLongClickListener getOnItemLongClickListener() {
        return onLongClickListener;
    }

    @Override
    public void setOnItemLongClickListener(OnItemLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

}

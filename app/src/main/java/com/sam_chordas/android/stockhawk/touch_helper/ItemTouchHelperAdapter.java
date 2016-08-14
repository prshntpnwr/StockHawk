package com.sam_chordas.android.stockhawk.touch_helper;

import android.view.View;

/**
 * Interface to enable swipe to delete
 */
public interface ItemTouchHelperAdapter {

    void onItemDismiss(int position);
}

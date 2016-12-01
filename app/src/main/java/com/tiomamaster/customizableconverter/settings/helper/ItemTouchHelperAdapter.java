package com.tiomamaster.customizableconverter.settings.helper;

/**
 * Created by Artyom on 14.11.2016.
 */
public interface ItemTouchHelperAdapter {

    boolean onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}
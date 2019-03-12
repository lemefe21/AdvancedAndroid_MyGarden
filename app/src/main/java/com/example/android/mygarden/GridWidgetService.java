package com.example.android.mygarden;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.PlantDetailActivity;
import com.example.android.mygarden.utils.PlantUtils;

public class GridWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridViewFactory(this.getApplicationContext());
    }
}

class GridViewFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private Cursor mCursor;

    public GridViewFactory(Context applicationContext) {
        mContext = applicationContext;
    }

    @Override
    public void onCreate() {

    }

    //called on start and when notifyAppWidgetViewDataChanged is called
    @Override
    public void onDataSetChanged() {

        // Get all plant info ordered by creation time
        Uri PLANT_URI = PlantContract.BASE_CONTENT_URI.buildUpon()
                .appendPath(PlantContract.PATH_PLANTS).build();

        if(mCursor != null) {
            mCursor.close();
        }

        mCursor = mContext.getContentResolver().query(
                PLANT_URI,
                null,
                null,
                null,
                PlantContract.PlantEntry.COLUMN_CREATION_TIME
        );

    }

    @Override
    public void onDestroy() {
        mCursor.close();
    }

    @Override
    public int getCount() {
        if(mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    /**
     * This method acts like the onBindViewHolder method in an Adapter
     *
     * @param position The current position of the item in the GridView to be displayed
     * @return The RemoteViews object to display for the provided position
     */
    @Override
    public RemoteViews getViewAt(int position) {

        if(mCursor == null || mCursor.getCount() == 0) {
            return null;
        }

        mCursor.moveToPosition(position);

        int indexId = mCursor.getColumnIndex(PlantContract.PlantEntry._ID);
        int createdTimeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
        int waterTimeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
        int plantTypeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);

        long plantId = mCursor.getLong(indexId);
        long createdAt = mCursor.getLong(createdTimeIndex);
        long wateredAt = mCursor.getLong(waterTimeIndex);
        int plantType = mCursor.getInt(plantTypeIndex);

        long timeNow = System.currentTimeMillis();

        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.plant_widget);

        // Update the plant image
        int imgRes = PlantUtils.getPlantImageRes(mContext, timeNow - createdAt,
                timeNow - wateredAt, plantType);

        remoteViews.setImageViewResource(R.id.widget_plant_image, imgRes);
        remoteViews.setTextViewText(R.id.widget_plant_name, String.valueOf(plantId));

        // Always hide the water drop in GridView mode
        remoteViews.setViewVisibility(R.id.widget_water_button, View.GONE);

        // Fill in the onClick PendingIntent Template using the specific plant Id for each item individually
        Bundle extras = new Bundle();
        extras.putLong(PlantDetailActivity.EXTRA_PLANT_ID, plantId);

        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);

        remoteViews.setOnClickFillInIntent(R.id.widget_plant_image, fillInIntent);

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

}

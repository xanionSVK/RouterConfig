package sk.xanion.routerconfig.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.Toast;

import sk.xanion.routerconfig.R;
import sk.xanion.routerconfig.RequestServerData;
import sk.xanion.routerconfig.model.WirelessStatus;
import sk.xanion.routerconfig.util.WifiStatusUtil;

/**
 * Implementation of App Widget functionality.
 */
public class RouterWidget extends AppWidgetProvider implements RequestServerData.RequestServerDataListener {
    public static final String KEY_WIDGET_STATUS = "sk.xanion.routerconfig.KEY_WIDGET_STATUS";
    public static final String KEY_WIDGET_STATUS_UNAVAILABLE = "sk.xanion.routerconfig.KEY_WIDGET_STATUS_UNAVAILABLE";

    Boolean intentStatus;
    boolean statusUnAvailable;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            if (intent.hasExtra(KEY_WIDGET_STATUS)) {
                this.intentStatus = intent.getBooleanExtra(KEY_WIDGET_STATUS, false);
            }
            this.statusUnAvailable = intent.getBooleanExtra(KEY_WIDGET_STATUS_UNAVAILABLE, false);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        if (newOptions.containsKey(KEY_WIDGET_STATUS)) {
            requestChangeWidgetIcon(context, android.R.drawable.ic_popup_sync);
            new RequestServerData(context, this, newOptions.getBoolean(KEY_WIDGET_STATUS) ? RequestServerData.METHOD_UNBLOCK : RequestServerData.METHOD_BLOCK).execute();
        } else {
            requestGetStatus(context);
        }
    }

    public static void requestChangeWidgetIcon(Context ctx, int iconId) {
        RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.router_widget);
        int[] widgetIds = AppWidgetManager.getInstance(ctx).getAppWidgetIds(new ComponentName(ctx, RouterWidget.class));
        Intent intent = new Intent(ctx, RouterWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        views.setImageViewResource(R.id.widgetImage, iconId);
        AppWidgetManager.getInstance(ctx).updateAppWidget(widgetIds, views);
    }

    private void requestGetStatus(Context ctx) {
        if (WifiStatusUtil.isConnectedToHomeWifi(ctx)) {
            requestChangeWidgetIcon(ctx, android.R.drawable.ic_popup_sync);
            new RequestServerData(ctx, this, 3).execute();
        } else {
            new RequestServerData(ctx, this, 3).execute();
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        int srcId = R.mipmap.ic_wifi_block_unavailable;
        Bundle changeExtra = new Bundle();
        if (intentStatus != null) {
            srcId = intentStatus ? R.mipmap.ic_wifi_block_activated : R.mipmap.ic_wifi_block_deactivated;
            changeExtra.putBoolean(KEY_WIDGET_STATUS, intentStatus);
        }
        for (int appWidgetId : appWidgetIds) {
            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.router_widget);
            Intent intent = new Intent(context, RouterWidget.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, changeExtra);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widgetImage, pendingIntent);
            if (statusUnAvailable) {
                srcId = R.mipmap.ic_wifi_block_unavailable;
            } else {
                if (intentStatus != null) {
                    srcId = intentStatus ? R.mipmap.ic_wifi_block_activated : R.mipmap.ic_wifi_block_deactivated;
                    changeExtra.putBoolean(KEY_WIDGET_STATUS, intentStatus);
                }
            }
            if (!statusUnAvailable && intentStatus == null) {
                srcId = android.R.drawable.ic_popup_sync;
            }
            views.setImageViewResource(R.id.widgetImage, srcId);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        if (!statusUnAvailable && intentStatus == null) {
            //firs call
            requestGetStatus(context);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onPostExecute(Context ctx, Bundle result, int methodType) {
        int ids[] = AppWidgetManager.getInstance(ctx).getAppWidgetIds(new ComponentName(ctx, RouterWidget.class));
        Intent intent = new Intent(ctx, RouterWidget.class);
        switch (methodType) {
            case RequestServerData.METHOD_BLOCK:
            case RequestServerData.METHOD_UNBLOCK:
                if (result.containsKey(RequestServerData.KEY_CHANGE_STATUS)) {
                    //request to change wireles
                    if (methodType == RequestServerData.METHOD_BLOCK) {
                        intent.putExtra(KEY_WIDGET_STATUS, Boolean.TRUE);
                    } else {
                        intent.putExtra(KEY_WIDGET_STATUS, Boolean.FALSE);
                    }
                }else{
                    if(result.containsKey("error")){
                        Toast.makeText(ctx, result.getString("error"), Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case RequestServerData.METHOD_STATUS: {
                if (result.containsKey(RequestServerData.KEY_WIRELESS_STATUS)) {
                    WirelessStatus status = (WirelessStatus) result.getSerializable(RequestServerData.KEY_WIRELESS_STATUS);
                    if (status != null && status.active != null) {
                        intent.putExtra(KEY_WIDGET_STATUS, status.active);
                    } else {
                        intent.putExtra(KEY_WIDGET_STATUS_UNAVAILABLE, true);
                        if (!TextUtils.isEmpty(status.exception)) {
                            Toast.makeText(ctx, status.exception, Toast.LENGTH_LONG).show();
                        }
                    }
                }
                break;
            }
            default:
                return;
        }

        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        ctx.sendBroadcast(intent);
    }

    @Override
    public void onPreexecute(int methodType) {

    }
}


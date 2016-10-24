package sk.xanion.routerconfig.receiver;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import sk.xanion.routerconfig.R;
import sk.xanion.routerconfig.RequestServerData;
import sk.xanion.routerconfig.widget.RouterWidget;

/**
 * Created by mkosik on 23. 10. 2016.
 */
public class RouterConfigAlarmReceiver extends BroadcastReceiver implements RequestServerData.RequestServerDataListener {
    public static final String KEY_METHOD_TYPE = "sk.xanion.routerconfig.KEY_METHOD_TYPE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.hasExtra(KEY_METHOD_TYPE)){
            Integer method = intent.getIntExtra(KEY_METHOD_TYPE, -1);
            if(method == RequestServerData.METHOD_BLOCK || method == RequestServerData.METHOD_UNBLOCK){
                new RequestServerData(context, method).execute();
            }
        }
    }

    @Override
    public void onPostExecute(Context ctx, Bundle result, int methodType) {
        if (methodType == RequestServerData.METHOD_BLOCK || methodType == RequestServerData.METHOD_UNBLOCK) {
            Intent intent = new Intent(ctx, RouterWidget.class);
            int ids[] = AppWidgetManager.getInstance(ctx).getAppWidgetIds(new ComponentName(ctx, RouterWidget.class));
            if (result.containsKey(RequestServerData.KEY_CHANGE_STATUS)) {
                //request to change wireles
                if (methodType == RequestServerData.METHOD_BLOCK) {
                    intent.putExtra(RouterWidget.KEY_WIDGET_STATUS, Boolean.TRUE);
                } else {
                    intent.putExtra(RouterWidget.KEY_WIDGET_STATUS, Boolean.FALSE);
                }

            }
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            ctx.sendBroadcast(intent);
        }
    }

    @Override
    public void onPreexecute(int methodType) {

    }
}

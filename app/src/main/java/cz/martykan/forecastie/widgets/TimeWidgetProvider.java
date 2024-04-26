package cz.martykan.forecastie.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import androidx.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cz.martykan.forecastie.AlarmReceiver;
import cz.martykan.forecastie.R;
import cz.martykan.forecastie.models.Weather;

public class TimeWidgetProvider extends AbstractWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.time_widget);

            setTheme(context, remoteViews);

            Intent intent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            remoteViews.setOnClickPendingIntent(R.id.widgetButtonRefresh, pendingIntent);

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Weather widgetWeather = this.getTodayWeather(context);

            if (widgetWeather == null) {
                this.openMainActivity(context, remoteViews);
                return;
            }

            DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
            String defaultDateFormat = context.getResources().getStringArray(R.array.dateFormatsValues)[0];
            String simpleDateFormat = sp.getString("dateFormat", defaultDateFormat);
            if ("custom".equals(simpleDateFormat)) {
                simpleDateFormat = sp.getString("dateFormatCustom", defaultDateFormat);
            }
            String dateString;
            try {
                simpleDateFormat = simpleDateFormat.substring(0, simpleDateFormat.indexOf("-") - 1);
                try {
                    SimpleDateFormat resultFormat = new SimpleDateFormat(simpleDateFormat, Locale.US);
                    dateString = resultFormat.format(new Date());
                } catch (IllegalArgumentException e) {
                    dateString = context.getResources().getString(R.string.error_dateFormat);
                }
            } catch (StringIndexOutOfBoundsException e) {
                DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
                dateString = dateFormat.format(new Date());
            }

            remoteViews.setTextViewText(R.id.time, timeFormat.format(new Date()));
            remoteViews.setTextViewText(R.id.date, dateString);
            remoteViews.setTextViewText(R.id.widgetCity, widgetWeather.getCity() + ", " + widgetWeather.getCountry());
            remoteViews.setTextViewText(R.id.widgetTemperature, this.getFormattedTemperature(widgetWeather, context, sp));
            remoteViews.setTextViewText(R.id.widgetDescription, widgetWeather.getDescription());
            remoteViews.setImageViewBitmap(R.id.widgetIcon, getWeatherIcon(widgetWeather, context));

            if (dateString.length() > 19)
                remoteViews.setViewPadding(R.id.widgetIcon, 40, 0, 0, 0);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
        scheduleNextUpdate(context);
    }
}

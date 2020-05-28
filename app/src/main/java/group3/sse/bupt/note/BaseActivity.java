package group3.sse.bupt.note;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.util.TypedValue;



import java.text.ParseException;
import java.text.SimpleDateFormat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


public abstract class BaseActivity extends AppCompatActivity {

    public final String TAG = "BaseActivity";
    public final String ACTION = "NIGHT_SWITCH";
    protected BroadcastReceiver receiver;
    protected IntentFilter filter;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setNightMode();
        TypedValue typedValue = new TypedValue();
        this.getTheme().resolveAttribute(R.attr.tvBackground, typedValue, true);
        //StatusBarCompat.setStatusBarColor(this, ContextCompat.getColor(this, typedValue.resourceId));

        filter = new IntentFilter();
        filter.addAction(ACTION);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                needRefresh();
            }
        };
         //当夜间模式发生变化时，系统广播会发出一条NIGHT_SWITCH广播
        registerReceiver(receiver, filter);

    }

    public boolean isNightMode(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return sharedPreferences.getBoolean("nightMode", false);
    }

    public void setNightMode(){
        if(isNightMode()) this.setTheme(R.style.NightTheme);
        else setTheme(R.style.DayTheme);

    }
    public boolean isReverseMode(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return sharedPreferences.getBoolean("reverseMode", false);
    }
    protected abstract void needRefresh();

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public long calStrToSec(String date) throws ParseException {//decode calender date to second
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        long secTime = format.parse(date).getTime();
        return secTime;
    }
}
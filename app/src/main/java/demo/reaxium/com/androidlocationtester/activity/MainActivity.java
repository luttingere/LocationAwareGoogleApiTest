package demo.reaxium.com.androidlocationtester.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import demo.reaxium.com.androidlocationtester.R;
import demo.reaxium.com.androidlocationtester.beans.LocationObject;
import demo.reaxium.com.androidlocationtester.globals.GlobalValues;
import demo.reaxium.com.androidlocationtester.service.GoogleLocationServices;

public class MainActivity extends AppCompatActivity {


    /**
     * Manager of broadCast between this activity and any service
     */
    protected LocalBroadcastManager mLocalBroadcastManager;

    /**
     * Objeto de ubicacion utilizado en pantalla
     */
    private LocationObject locationObject;

    /**
     *  Elementos en pantalla
      */
    private TextView latitude, longitude, accuracy, speed, direction, provider, time;

    private SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a");

    /**
     * Broad cast handler
     */
    protected BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case GlobalValues.LOCATION_CHANGED:
                    //Aqui corren las actividades cuando se recibe una nueva ubicacion
                    locationObject = (LocationObject) intent.getSerializableExtra(GlobalValues.BROADCAST_PARAM);
                    if(locationObject != null){

                        latitude.setText(""+locationObject.getLatitude());
                        longitude.setText(""+locationObject.getLongitude());
                        accuracy.setText(""+locationObject.getAccuracy());
                        speed.setText(""+locationObject.getSpeed());
                        direction.setText(""+locationObject.getBearing());
                        provider.setText(locationObject.getProveedor());
                        time.setText(format.format(locationObject.getTime()));

                    }
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        latitude = (TextView) findViewById(R.id.latitude);
        longitude = (TextView) findViewById(R.id.longitude);
        accuracy = (TextView) findViewById(R.id.accuracy);
        speed = (TextView) findViewById(R.id.speed);
        direction = (TextView) findViewById(R.id.direction);
        provider = (TextView) findViewById(R.id.provider);
        time = (TextView) findViewById(R.id.time);
        registerTheBroadCast();
        startNotificationService();
    }


    @Override
    protected void onStop() {
        super.onStop();
        stopNotificationService();
        unregisterBroadCast();
    }

    /**
     * Inicia el servicio de Ubicacion
     */
    private void startNotificationService() {
        Intent servIntent = new Intent(this, GoogleLocationServices.class);
        startService(servIntent);
    }

    /**
     * Detiene el proceso de envio de ubicacion
     */
    private void stopNotificationService() {
        Intent servIntent = new Intent(this, GoogleLocationServices.class);
        stopService(servIntent);
    }

    /**
     * attach to the activity the broadcast listener, so it can be notified if the receiver cancel the call
     */
    private void registerTheBroadCast() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(GlobalValues.LOCATION_CHANGED);
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    /**
     * unattach broadcast
     */
    private void unregisterBroadCast() {
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
    }
}

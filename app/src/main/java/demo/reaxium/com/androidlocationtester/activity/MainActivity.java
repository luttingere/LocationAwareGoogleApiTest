package demo.reaxium.com.androidlocationtester.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.FloatMath;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;

import demo.reaxium.com.androidlocationtester.R;
import demo.reaxium.com.androidlocationtester.beans.LocationObject;
import demo.reaxium.com.androidlocationtester.globals.GlobalValues;
import demo.reaxium.com.androidlocationtester.service.GoogleLocationServices;
import demo.reaxium.com.androidlocationtester.util.MarkerAnimation;
import demo.reaxium.com.androidlocationtester.util.LatLngInterpolator;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    /**
     * Manager of broadCast between this activity and any service
     */
    protected LocalBroadcastManager mLocalBroadcastManager;

    /**
     * Objeto de ubicacion utilizado en pantalla
     */
    private LocationObject locationObject;

    /**
     * Elementos en pantalla
     */
    private TextView latitude, longitude, accuracy, speed, direction, provider, time, motion;

    private SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a");


    /**
     * Mapa fragmento de android
     */
    private SupportMapFragment mapFragment;

    /**
     * Instancia del objeto mapa de google maps
     */
    private GoogleMap googleMap;

    private Marker markerUbicacion;

    private static final float DEFAULT_ZOOM = 17.0F;

    /**
     * referencia de clase al zoom actual de la camara del mapa
     */
    private float googleMapZoom = DEFAULT_ZOOM;


    /**
     * Instancias para sensores de movimiento
     */
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float[] mGravity;
    private double mAccel;
    private double mAccelCurrent;
    private double mAccelLast;

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
                    if (locationObject != null) {

                        latitude.setText("" + locationObject.getLatitude());
                        longitude.setText("" + locationObject.getLongitude());
                        accuracy.setText("" + locationObject.getAccuracy());
                        speed.setText("" + locationObject.getSpeed());
                        direction.setText("" + locationObject.getBearing());
                        provider.setText(locationObject.getProveedor());
                        time.setText(format.format(locationObject.getTime()));

                        if (googleMap != null) {
                            LatLng latLng = new LatLng(locationObject.getLatitude(), locationObject.getLongitude());
                            if (markerUbicacion != null) {
                                MarkerAnimation.animateMarkerToGB(markerUbicacion, latLng, new LatLngInterpolator.LinearFixed(), 2000);
                            } else {
                                createMarker(latLng);
                            }

                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(getCameraPosition(latLng)), 2000, new GoogleMap.CancelableCallback() {
                                @Override
                                public void onFinish() {
                                }

                                @Override
                                public void onCancel() {
                                }
                            });

                        }

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
        motion = (TextView) findViewById(R.id.movement);
        time = (TextView) findViewById(R.id.time);
        // configuracion sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        FragmentManager fragmentManager = getSupportFragmentManager();
        mapFragment = ((SupportMapFragment) fragmentManager.findFragmentById(R.id.map));
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                googleMap = map;
                googleMap.setOnCameraChangeListener(getCameraChangeListener());
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterBroadCast();
        stopNotificationService();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerTheBroadCast();
        startNotificationService();
        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_UI);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mGravity = event.values.clone();
            // Shake detection
            double x = mGravity[0];
            double y = mGravity[1];
            double z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = Math.sqrt(x*x+y*y+z*z);
            double delta =  mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;

            if(mAccel > 1){
                motion.setText(String.valueOf(mAccel));
            }else{
                motion.setText("0.0");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

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


    /**
     * Carga la instancia del marker de la ubicacion del dispositivo
     */
    private void createMarker(LatLng location) {
        MarkerOptions marker = new MarkerOptions();
        marker.position(location);
        marker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
        markerUbicacion = googleMap.addMarker(marker);
    }

    /**
     * Crea una instancia de posicion de la camara
     *
     * @param busLocation
     * @return
     */
    private CameraPosition getCameraPosition(LatLng busLocation) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(busLocation)      // Sets the center of the map to the actual position of the device
                .zoom(googleMapZoom)     // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to north
                .tilt(60)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder

        return cameraPosition;

    }
    /**
     * Vigila los cambios de orientacion y zoom en la camara del mapa
     *
     * @return
     */
    private GoogleMap.OnCameraChangeListener getCameraChangeListener() {
        return new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                googleMapZoom = position.zoom;
            }
        };
    }
}

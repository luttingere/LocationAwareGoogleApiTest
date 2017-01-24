package demo.reaxium.com.androidlocationtester.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Date;

import demo.reaxium.com.androidlocationtester.beans.LocationObject;
import demo.reaxium.com.androidlocationtester.globals.GlobalValues;

/**
 * Created by Eduardo Luttinger on 24/01/2017.
 */
public class GoogleLocationServices extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    /**
     * Tiempo entre notificaciones de ubicacion
     */
    private static final long MIN_TIME_BW_UPDATES = 10000;

    /**
     *
     */
    private static final long FASTER_TIME_BW_UPDATES = 5000;

    /**
     * Cliente del api de google (Servicios de Ubicacion)
     */
    private GoogleApiClient mGoogleApiClient;


    /**
     * BroadCastSender between this service and any activity
     */
    private LocalBroadcastManager mLocalBroadcastManager;

    /**
     * Ulñtima ubicacion buena conocida
     */
    private static Location lastGoodLocationKnown;

    /**
     * Objeto Serializable que encapsula datos importantes de un objeto Location
     */
    private LocationObject locationObject;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initializeGoogleApiClient();
        connectWithLocationServiceApi();
        enableCommunicationWithTheMainThread();
        return START_STICKY;
    }

    /**
     * Aqui las rutinas que deben correr cuando muere el servicio
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        disconnectFromLocationServiceApi();
    }

    /**
     * Inicializa el Cliente del api de ubicacion de google
     */
    private void initializeGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    /**
     * Inicializa un objeto que envia señales desde el servicio hacia cualquier
     * Actividad o fragmento presente en el hilo principal de la aplicacion
     */
    private void enableCommunicationWithTheMainThread() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(GoogleLocationServices.this);
    }


    /**
     * Envia datos que estan disponibles para cualquier actividad en el hilo principal
     * que este subscrita a señales del topico(Tipificador de los datos) que se envie.
     *
     * @param topic  (De que se trata la data que se envia)
     * @param params (Datos que se envian)
     */
    private void sendDataToMainThread(String topic, Serializable params) {
        Intent action = new Intent(topic);
        action.putExtra(GlobalValues.BROADCAST_PARAM, params);
        mLocalBroadcastManager.sendBroadcast(action);
    }

    /**
     * solicita la conexion al servicio de ubicacion de google
     */
    private void connectWithLocationServiceApi() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Solicita una desconexion del sistema con el api de ubicacion de google
     */
    private void disconnectFromLocationServiceApi() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     *
     */
    @SuppressWarnings("MissingPermission")
    private void requestLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationRequest(), this);
    }

    /**
     * Detiene las solicitudes de Ubicacion.
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /**
     * Crea un objeto de solicitud de notificaciones de ubicacion al sistema
     */
    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(MIN_TIME_BW_UPDATES);
        mLocationRequest.setFastestInterval(FASTER_TIME_BW_UPDATES);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }


    /**
     * Metodo llamado cuando el cliente Google establece la conexion con la capa de servicios de Ubicacion
     *
     * @param bundle
     */
    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected(@Nullable Bundle bundle) {
        //Obtenemos ultima ubicacion conocida.
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null) {
            GlobalValues.LAST_LOCATION = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        }
        //Enviamos solicitud de notificaciones de ubicacion
        requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Aca llegan todas las notificaciones de ubicacion por (GPS, CELL-ID Y WIFI)
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        //Validar si la nueva ubicacion recibida es mejor ubicacion que la ultima buena conocida.
        if (lastGoodLocationKnown == null) {
            lastGoodLocationKnown = location;
        } else if (isABetterLocation(lastGoodLocationKnown,location)) {
            lastGoodLocationKnown = location;
        }

        locationObject = getLocationObject(lastGoodLocationKnown);
        sendDataToMainThread(GlobalValues.LOCATION_CHANGED,locationObject);
    }



    /**
     * Carga un Objeto LocationObject de un Objeto Location
     * @param location
     * @return
     */
    private LocationObject getLocationObject(Location location){
        LocationObject locationObject = new LocationObject(
                location.getLatitude(),
                location.getLongitude(),
                location.getSpeed(),
                location.getAltitude(),
                location.getBearing(),
                location.getAccuracy(),
                location.getProvider(),
                new Date(location.getTime()));
        return locationObject;
    }


    /**
     * Validamos si la locacion recibida es una ubicacion coherente segun los siguientes criterios de Aceptacion
     * <p>
     * - Fecha en la que fue emitida la ubicacion
     * - Precision
     * -
     *
     * @param bestLocationKnown
     * @param newLocation
     * @return
     */
    private boolean isABetterLocation(Location bestLocationKnown, Location newLocation) {
        boolean isABetterLocation = true;


        return isABetterLocation;
    }
}

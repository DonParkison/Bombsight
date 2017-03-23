package com.bombbomb.bombsight;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bombbomb.bbapiproxy.Geocoding.GeocodeCallback;
import com.bombbomb.bbapiproxy.Geocoding.GeocodeRequestor;
import com.bombbomb.bbapiproxy.Geocoding.ResponseGeocodeObjects.AddressComponent;
import com.bombbomb.bbapiproxy.Geocoding.ResponseGeocodeObjects.Geocode;
import com.bombbomb.bbapiproxy.Geocoding.ResponseGeocodeObjects.Result;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        BombsightLocListenerCallbacks,
        AddAddressLocationDialog.AddAddressLocationCallbacks,
        GeocodeCallback {


    private MapView mapView;
    private BombsightLocationListener locationListener;
    private LocationManager locationManager;
    private Graphic currentLocationGraphic;
    private GraphicsOverlay locationGraphicsLayer;
    private GraphicsOverlay geocodeLocationsGraphicsLayer;
    private LocatorTask locatorTask;
    private Portal portal;

    public boolean portalLoaded = false;
    public boolean mapLaded = false;

    private boolean gpsEngaged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationListener = new BombsightLocationListener(this);
        locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);

        ArcGISRuntimeEnvironment.setLicense(this.getString(R.string.esriLicenceKey));


        DefaultAuthenticationChallengeHandler handler = new DefaultAuthenticationChallengeHandler(this);
        AuthenticationManager.setAuthenticationChallengeHandler(handler);
        //portal = new Portal("http://www.arcgis.com", true);
        portal = new Portal("http://dlparkisongmail.maps.arcgis.com", true);

        portal.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                if (portal.getLoadStatus() == LoadStatus.LOADED){
                    portalLoaded = true;
                    loadMapData();
                }
            }
        });

        portal.loadAsync();




        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton zoomToLocation = (FloatingActionButton) findViewById(R.id.zoom_to_location);
        zoomToLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                toggleGpsState(view);
            }
        });

        FloatingActionButton addAddress = (FloatingActionButton) findViewById(R.id.add_location);
        addAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addAddress();


            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void loadMapData(){

        if (portalLoaded) {

            PortalItem portalItem = new PortalItem(this.portal, this.getString(R.string.esriMapId));

            this.mapView = (MapView) findViewById(R.id.mapView);
            //ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 38.832689, -104.825886, 14);
            Basemap basemap = new Basemap(portalItem);
            Viewpoint viewpoint = new Viewpoint( 38.832689, -104.825886, 25000);
            ArcGISMap map = new ArcGISMap(this.getString(R.string.esriWebMap));

            map.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {

                    mapLaded = true;

                    loadExistingLocations();

                }
            });

            map.setInitialViewpoint(viewpoint);
            mapView.setMap(map);

            locationGraphicsLayer = new GraphicsOverlay();
            geocodeLocationsGraphicsLayer = new GraphicsOverlay();
            mapView.getGraphicsOverlays().add(0, locationGraphicsLayer);
            mapView.getGraphicsOverlays().add(1, geocodeLocationsGraphicsLayer);




        }
    }

    private void loadExistingLocations(){

        Map<Integer, BombsightLocation> locations = BombsightDbHelper.getInstance(this).getAllLocations();

        Iterator iterator = locations.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry pair = (Map.Entry)iterator.next();

            BombsightLocation location = (BombsightLocation)pair.getValue();
            Point geocodePoint = addBsLocationToMap(location);

        }



    }

    private void addAddress(){
        AddAddressLocationDialog dialog = new AddAddressLocationDialog(this, this);
        this.runOnUiThread(dialog);
}


    public void OnAddressLocationSaveClicked(String address, String zipCode){


        try {
            GeocodeRequestor requestor = new GeocodeRequestor(this, this);
            requestor.executeGetGeocode(address, zipCode);
        } catch (Exception ex){
            String message = ex.getMessage();
        }
    }

    @Override
    public void geocodeReturned(Geocode geocode) {

        FloatingActionButton zoomToLocation = (FloatingActionButton) findViewById(R.id.zoom_to_location);
        deactivateGps();
        gpsEngaged = false;
        zoomToLocation.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorWhite)));


        BombsightLocation bsLocation = buildBsLocationFromGeocode(geocode);

        // Write location to the database
        BombsightLocation bsLocationWithId = BombsightDbHelper.getInstance(this).addOrUpdateLocation(bsLocation);

        // add location to the map

        Point geocodePoint = addBsLocationToMap(bsLocationWithId);
        mapView.setViewpointCenterAsync(geocodePoint, 4000);


    }

    @NonNull
    private Point addBsLocationToMap(BombsightLocation bsLocationWithId) {
        Point geocodePoint = null;
        try {
            SimpleMarkerSymbol locationMarker = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xffff0000, 10f);
            BitmapDrawable picture = (BitmapDrawable) getDrawable(R.drawable.ic_map_location);
            PictureMarkerSymbol pictureMarkerSymbol = new PictureMarkerSymbol(picture);

            geocodePoint = new Point(bsLocationWithId.longitude, bsLocationWithId.latitude, SpatialReferences.getWgs84());
            Graphic geocodeLoc = new Graphic(geocodePoint, pictureMarkerSymbol);


            locationGraphicsLayer.getGraphics().add(0, geocodeLoc);
        } catch (Exception ex){
            String message = ex.getMessage();
        }

        return geocodePoint;
    }

    public BombsightLocation buildBsLocationFromGeocode(Geocode geocode){

        BombsightLocation bsLocation = new BombsightLocation();
        List<Result> results = geocode.getResults();

        for (Result result : results){
            bsLocation.formattedAddress = result.getFormattedAddress();

            bsLocation.latitude = result.getGeometry().getLocation().getLat();
            bsLocation.longitude = result.getGeometry().getLocation().getLng();

            List<AddressComponent> components = result.getAddressComponents();

            for (AddressComponent component : components){

                List<String> types = component.getTypes();

                for (String type : types){

                    if (type.equalsIgnoreCase("street_number")){
                        bsLocation.streetNumber = component.getShortName();
                        break;
                    }
                    if (type.equalsIgnoreCase("route")){
                        bsLocation.route = component.getShortName();
                        break;
                    }
                    if (type.equalsIgnoreCase("neighborhood")){
                        bsLocation.neighborhood = component.getShortName();
                        break;
                    }
                    if (type.equalsIgnoreCase("postal_code")){
                        bsLocation.postCode = component.getShortName();
                        break;
                    }
                    if (type.equalsIgnoreCase("postal_code_suffix")){
                        bsLocation.postCodeSuffix = component.getShortName();
                        break;
                    }
                    if (type.equalsIgnoreCase("locality")){
                        bsLocation.city = component.getShortName();
                        break;
                    }
                    if (type.equalsIgnoreCase("administrative_area_level_2")){
                        bsLocation.county = component.getShortName();
                        break;
                    }
                    if (type.equalsIgnoreCase("administrative_area_level_1")){
                        bsLocation.state = component.getLongName();
                        break;
                    }
                    if (type.equalsIgnoreCase("country")){
                        bsLocation.country = component.getLongName();
                        break;
                    }
                }


            }


        }

        return bsLocation;

    }




    private void toggleGpsState(View view) {
        if (gpsEngaged){
            deactivateGps();
            gpsEngaged = false;
            view.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorWhite)));
        } else {
            activateGps();
            gpsEngaged = true;
            view.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (gpsEngaged)
            activateGps();
    }

    private void activateGps() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PermissionsUtil.RequestPermission(this, PermissionsUtil.PERMISSIONS.LOCATION);
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                3000, 0, locationListener);
    }

    @Override
    protected void onPause(){
        super.onPause();

        gpsEngaged = false;
        deactivateGps();

    }

    private void deactivateGps() {
        locationManager.removeUpdates(locationListener);

        try {
            if (locationGraphicsLayer != null) {
                if (locationGraphicsLayer.getGraphics() != null && locationGraphicsLayer.getGraphics().size() > 0) {
                    locationGraphicsLayer.getGraphics().remove(0);
                    currentLocationGraphic = null;
                }
            }
        } catch (Exception ex){
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onLocationChanged(Point locationPoint) {

        SimpleMarkerSymbol locationMarker = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xffff0000, 10f);

        if (currentLocationGraphic == null){
            currentLocationGraphic = new Graphic(locationPoint, locationMarker);
            locationGraphicsLayer.getGraphics().add(0, currentLocationGraphic);
        } else {
            currentLocationGraphic.setGeometry(locationPoint);
        }

         mapView.setViewpointCenterAsync(locationPoint, mapView.getMapScale());


    }


}

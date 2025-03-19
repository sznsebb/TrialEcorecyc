package com.capstone.ecorecyc.maps;

import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;

import com.capstone.ecorecyc.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;
import com.mapbox.maps.ImageHolder;

public class Maps extends AppCompatActivity {
    private MapView mapView;
    FloatingActionButton floatingActionButton;

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    Toast.makeText(Maps.this, "Permission granted!", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final OnIndicatorBearingChangedListener onIndicatorBearingChangedListener = v ->
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().bearing(v).build());

    private final OnIndicatorPositionChangedListener onIndicatorPositionChangedListener = point -> {
        mapView.getMapboxMap().setCamera(new CameraOptions.Builder().center(point).zoom(20.0).build());
        getGestures(mapView).setFocalPoint(mapView.getMapboxMap().pixelForCoordinate(point));
    };

    private final OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
            getLocationComponent(mapView).removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
            getLocationComponent(mapView).removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
            getGestures(mapView).removeOnMoveListener(onMoveListener);
            floatingActionButton.show();
        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {
            // No action required
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mapView = findViewById(R.id.mapView);
        floatingActionButton = findViewById(R.id.focusLocation);

        if (ActivityCompat.checkSelfPermission(Maps.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        floatingActionButton.hide();
        mapView.getMapboxMap().loadStyleUri("mapbox://styles/mapbox/standard-satellite", style -> {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(20.0).build());
            LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
            locationComponentPlugin.setEnabled(true);

            Drawable drawable = AppCompatResources.getDrawable(Maps.this, R.drawable.baseline_location_pin_24);
            Bitmap bitmap = drawableToBitmap(drawable);

            LocationPuck2D locationPuck2D = new LocationPuck2D();
            locationPuck2D.setBearingImage(ImageHolder.from(bitmap));
            locationComponentPlugin.setLocationPuck(locationPuck2D);

            locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
            locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
            getGestures(mapView).addOnMoveListener(onMoveListener);

            floatingActionButton.setOnClickListener(view -> {
                locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
                locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
                getGestures(mapView).addOnMoveListener(onMoveListener);
                floatingActionButton.hide();
            });
        });
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}

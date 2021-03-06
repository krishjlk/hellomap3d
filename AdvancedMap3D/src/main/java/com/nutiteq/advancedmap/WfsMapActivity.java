package com.nutiteq.advancedmap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ZoomControls;

import com.nutiteq.MapView;
import com.nutiteq.components.Components;
import com.nutiteq.components.Options;
import com.nutiteq.layers.vector.WfsLayer;
import com.nutiteq.log.Log;
import com.nutiteq.projections.EPSG3857;
import com.nutiteq.style.LineStyle;
import com.nutiteq.style.PointStyle;
import com.nutiteq.style.StyleSet;
import com.nutiteq.utils.UnscaledBitmapLoader;

public class WfsMapActivity extends Activity {

	private MapView mapView;

    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// enable logging for troubleshooting - optional
		Log.enableAll();
		Log.setTag("wfs");

		// 1. Get the MapView from the Layout xml - mandatory
		mapView = (MapView) findViewById(R.id.mapView);

		// Optional, but very useful: restore map state during device rotation,
		// it is saved in onRetainNonConfigurationInstance() below
		Components retainObject = (Components) getLastNonConfigurationInstance();
		if (retainObject != null) {
			// just restore configuration, skip other initializations
			mapView.setComponents(retainObject);
			mapView.startMapping();
			return;
		} else {
			// 2. create and set MapView components - mandatory
		      Components components = new Components();
		      // set stereo view: works if you rotate to landscape and device has HTC 3D or LG Real3D
		      mapView.setComponents(components);
		      }


		// 3. Define map layer for basemap - mandatory.
        
        String layers = "osm:towns,osm:osm_mainroads_gen1,osm:osm_amenities,osm:osm_roads";

        // add WFS layer as base layer
        String wfsUrl = "http://kaart.maakaart.ee/geoserver/osm/ows?service=WFS&version=1.0.0&request=GetFeature&typeName="+layers+"&maxFeatures=500";
        
        StyleSet<LineStyle> roadLineStyleSet = new StyleSet<LineStyle>(LineStyle.builder().setWidth(0.04f).setColor(0xFFAAAAAA).build());
        
        
        Bitmap pointMarker = UnscaledBitmapLoader.decodeResource(getResources(), R.drawable.point);
        StyleSet<PointStyle> pointStyleSet = new StyleSet<PointStyle>(PointStyle.builder().setBitmap(pointMarker).setSize(0.05f).setColor(Color.BLUE).setPickingSize(0.2f).build());
        
        WfsLayer wfsLayer = new WfsLayer(new EPSG3857(), wfsUrl, pointStyleSet, roadLineStyleSet, null);
        mapView.getLayers().setBaseLayer(wfsLayer);

        // add label layer for WFS streets
        // 1. define style callback for labels
        // disabled - requires experimental SDK
        /* 
        WfsTextLayer textLayer = new WfsTextLayer(
                mapView.getLayers().getBaseLayer().getProjection(),
                wfsLayer) {
        
            private StyleSet<TextStyle> styleSetRoad = new StyleSet<TextStyle>(
                    TextStyle.builder().setAllowOverlap(false)
                            .setOrientation(TextStyle.GROUND_ORIENTATION)
                            .setAnchorY(TextStyle.CENTER)
                            .setSize(25).build());
            
            private StyleSet<TextStyle> styleSetAmenity = new StyleSet<TextStyle>(
                    TextStyle
                            .builder()
                            .setAllowOverlap(false)
                            .setOrientation(
                                    TextStyle.CAMERA_BILLBOARD_ORIENTATION)
                            .setSize(30)
                            .setColor(Color.argb(255, 100, 100, 100))
                            .setPlacementPriority(5).build());

            @Override
            protected StyleSet<TextStyle> createStyleSet(Feature feature,
                    int zoom) {
                if (feature.geometry.type.equals("Point")) {
                    return styleSetAmenity;
                }
                return styleSetRoad;
            }

        };
        // 2. set properties for texts
        textLayer.setZOrdered(false);
        textLayer.setMaxVisibleElements(30);
        
        // 3. add layer
        mapView.getLayers().addLayer(textLayer);
        */
        
		// Location: San Francisco
        mapView.setFocusPoint(mapView.getLayers().getBaseLayer().getProjection().fromWgs84(-122.416667f, 37.766667f));
        
		// rotation - 0 = north-up
		mapView.setRotation(0f);
		// zoom - 0 = world, like on most web maps
		mapView.setZoom(16.0f);
        // tilt means perspective view. Default is 90 degrees for "normal" 2D map view, minimum allowed is 30 degrees.
		mapView.setTilt(40.0f);


		// Activate some mapview options to make it smoother - optional
		mapView.getOptions().setPreloading(false);
		mapView.getOptions().setSeamlessHorizontalPan(true);
		mapView.getOptions().setTileFading(false);
		mapView.getOptions().setKineticPanning(true);
		mapView.getOptions().setDoubleClickZoomIn(true);
		mapView.getOptions().setDualClickZoomOut(true);
		
		// set sky bitmap - optional, default - white
		mapView.getOptions().setSkyDrawMode(Options.DRAW_BITMAP);
		mapView.getOptions().setSkyOffset(4.86f);
		mapView.getOptions().setSkyBitmap(
				UnscaledBitmapLoader.decodeResource(getResources(),
						R.drawable.sky_small));

        // Map background, visible if no map tiles loaded - optional, default - white
		mapView.getOptions().setBackgroundPlaneDrawMode(Options.DRAW_COLOR);
		mapView.getOptions().setBackgroundPlaneColor(Color.WHITE);
		mapView.getOptions().setClearColor(Color.WHITE);

		// configure texture caching - optional, suggested
		mapView.getOptions().setTextureMemoryCacheSize(20 * 1024 * 1024);
		mapView.getOptions().setCompressedMemoryCacheSize(8 * 1024 * 1024);

        // define online map persistent caching - optional, suggested. Default - no caching
        mapView.getOptions().setPersistentCachePath(this.getDatabasePath("mapcache_wms").getPath());
        // set persistent raster cache limit to 100MB
        mapView.getOptions().setPersistentCacheSize(100 * 1024 * 1024);
		
		// 4. Start the map - mandatory
		mapView.startMapping();
        
		// 5. zoom buttons using Android widgets - optional
		// get the zoomcontrols that was defined in main.xml
		ZoomControls zoomControls = (ZoomControls) findViewById(R.id.zoomcontrols);
		// set zoomcontrols listeners to enable zooming
		zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
			public void onClick(final View v) {
				mapView.zoomIn();
			}
		});
		zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
			public void onClick(final View v) {
				mapView.zoomOut();
			}
		});

	}


    public MapView getMapView() {
        return mapView;
    }
     
    @Override
    protected void onStop() {
        super.onStop();
        mapView.stopMapping();
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
        Log.debug("onRetainNonConfigurationInstance");
        return this.mapView.getComponents();
    }

}


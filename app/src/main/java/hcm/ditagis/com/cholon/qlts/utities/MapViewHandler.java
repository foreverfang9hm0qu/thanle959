package hcm.ditagis.com.cholon.qlts.utities;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.ArcGISFeatureTable;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import hcm.ditagis.com.cholon.qlts.MainActivity;
import hcm.ditagis.com.cholon.qlts.R;
import hcm.ditagis.com.cholon.qlts.adapter.ObjectsAdapter;
import hcm.ditagis.com.cholon.qlts.async.QuerySearchAsycn;
import hcm.ditagis.com.cholon.qlts.async.SingleTapAddFeatureAsync;
import hcm.ditagis.com.cholon.qlts.async.SingleTapMapViewAsync;
import hcm.ditagis.com.cholon.qlts.libs.FeatureLayerDTG;


/**
 * Created by ThanLe on 2/2/2018.
 */

public class MapViewHandler extends Activity {

    private static final int REQUEST_ID_IMAGE_CAPTURE = 1;
    private static double DELTA_MOVE_Y = 0;//7000;
    private final ArcGISMap mMap;
    LocatorTask loc = new LocatorTask("http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer");
    private FeatureLayerDTG mFeatureLayerDTG;
    private android.graphics.Point mClickPoint;
    private ArcGISFeature mSelectedArcGISFeature;
    private MapView mMapView;
    private boolean isClickBtnAdd = false;
    private ServiceFeatureTable searchSFT;
    private ServiceFeatureTable addSFT;
    private Popup mPopUp;
    private MainActivity quanLyTaiSan;


    public void setFeatureLayerDTGs(List<FeatureLayerDTG> mFeatureLayerDTGs) {
        this.mFeatureLayerDTGs = mFeatureLayerDTGs;
    }

    public void setmPopUp(Popup mPopUp) {
        this.mPopUp = mPopUp;
    }

    private List<FeatureLayerDTG> mFeatureLayerDTGs;

    public MapViewHandler(MapView mMapView, MainActivity quanLyTaiSan) {
        this.mMapView = mMapView;
        this.quanLyTaiSan = quanLyTaiSan;
        this.mMap = mMapView.getMap();
    }

    public ServiceFeatureTable getSearchSFT() {
        return searchSFT;
    }

    public void setSearchSFT(ServiceFeatureTable searchSFT) {
        this.searchSFT = searchSFT;
    }

    public ServiceFeatureTable getAddSFT() {
        return addSFT;
    }

    public void setAddSFT(ServiceFeatureTable addSFT) {
        this.addSFT = addSFT;
    }

    public void setClickBtnAdd(boolean clickBtnAdd) {
        isClickBtnAdd = clickBtnAdd;
    }

    public void addFeature() {
        SingleTapAddFeatureAsync singleTapAdddFeatureAsync = new SingleTapAddFeatureAsync(quanLyTaiSan, addSFT, mMapView);
        Point add_point = mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).getTargetGeometry().getExtent().getCenter();
        singleTapAdddFeatureAsync.execute(add_point);
        closeAddFeature();
    }

    public void onSingleTapMapView(MotionEvent e) {
        final Point clickPoint = mMapView.screenToLocation(new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY())));
        mClickPoint = new android.graphics.Point((int) e.getX(), (int) e.getY());
        SingleTapMapViewAsync singleTapMapViewAsync = new SingleTapMapViewAsync(quanLyTaiSan, mFeatureLayerDTGs, mPopUp, mClickPoint, mMapView);
        singleTapMapViewAsync.execute(clickPoint);
    }

    public void closeAddFeature() {
        ((LinearLayout) this.quanLyTaiSan.findViewById(R.id.linear_addfeature)).setVisibility(View.GONE);
        ((ImageView) this.quanLyTaiSan.findViewById(R.id.img_map_pin)).setVisibility(View.GONE);
        ((FloatingActionButton) this.quanLyTaiSan.findViewById(R.id.floatBtnAdd)).setVisibility(View.VISIBLE);
        this.setClickBtnAdd(false);
    }

    public double[] onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Point center = ((MapView) mMapView).getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).getTargetGeometry().getExtent().getCenter();
        Geometry project = GeometryEngine.project(center, SpatialReferences.getWgs84());
        double[] location = {project.getExtent().getCenter().getX(), project.getExtent().getCenter().getY()};
//        Geometry geometry = GeometryEngine.project(project, SpatialReferences.getWebMercator());
        return location;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getDateString() {
        String timeStamp = Constant.DATE_FORMAT.format(Calendar.getInstance().getTime());

        SimpleDateFormat writeDate = new SimpleDateFormat("dd_MM_yyyy HH:mm:ss");
        writeDate.setTimeZone(TimeZone.getTimeZone("GMT+07:00"));
        String timeStamp1 = writeDate.format(Calendar.getInstance().getTime());
        return timeStamp1;
    }

    private String getTimeID() {
        String timeStamp = Constant.DATE_FORMAT.format(Calendar.getInstance().getTime());
        return timeStamp;
    }

    public void queryByObjectID(int objectID) {
        final QueryParameters queryParameters = new QueryParameters();
        final String query = "OBJECTID = " + objectID;
        queryParameters.setWhereClause(query);
        final ListenableFuture<FeatureQueryResult> feature = searchSFT.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
        feature.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    FeatureQueryResult result = feature.get();
                    if (result.iterator().hasNext()) {
                        mSelectedArcGISFeature = (ArcGISFeature) result.iterator().next();
                        if (mSelectedArcGISFeature != null) {
                            String tableName = ((ArcGISFeatureTable) mSelectedArcGISFeature.getFeatureTable()).getTableName();
                            FeatureLayerDTG featureLayerDTG = getmFeatureLayerDTG(tableName);
                            mPopUp.setFeatureLayerDTG(featureLayerDTG);
                            mPopUp.showPopup(mSelectedArcGISFeature, false);
                        } else mPopUp.dimissCallout();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public FeatureLayerDTG getmFeatureLayerDTG(String tableName) {
        for (FeatureLayerDTG featureLayerDTG : mFeatureLayerDTGs) {
            String tableNameDTG = ((ArcGISFeatureTable) featureLayerDTG.getFeatureLayer().getFeatureTable()).getTableName();
            if (tableNameDTG.equals(tableName)) return featureLayerDTG;
        }
        return null;
    }

    public void querySearch(String searchStr, ListView listView, final ObjectsAdapter adapter) {
        new QuerySearchAsycn(mMapView.getContext(), searchSFT, output -> {
            if (output != null && output.size() > 0) {
                adapter.clear();
                adapter.addAll(output);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(mMapView.getContext(), "Không tìm thấy thông tin!", Toast.LENGTH_SHORT).show();
            }

        }).execute(searchStr);
    }
}
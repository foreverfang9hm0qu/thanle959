package hcm.ditagis.com.cholon.qlts.async;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Attachment;
import com.esri.arcgisruntime.data.CodedValue;
import com.esri.arcgisruntime.data.CodedValueDomain;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import hcm.ditagis.com.cholon.qlts.R;
import hcm.ditagis.com.cholon.qlts.tools.MySnackBar;
import hcm.ditagis.com.cholon.qlts.utities.Constant;
import hcm.ditagis.com.cholon.qlts.utities.DApplication;

/**
 * Created by ThanLe on 4/16/2018.
 */

public class SingleTapAddFeatureAsync extends AsyncTask<Point, Void, Void> {
    private ProgressDialog mDialog;
    private Activity mActivity;
    private DApplication mDApplication;
    private ServiceFeatureTable mServiceFeatureTable;
    private ArcGISFeature mSelectedArcGISFeature;
    private MapView mMapView;

    public SingleTapAddFeatureAsync(Activity activity, ServiceFeatureTable serviceFeatureTable, MapView mapView) {
        this.mServiceFeatureTable = serviceFeatureTable;
        this.mMapView = mapView;
        this.mDApplication = (DApplication) activity.getApplication();
        this.mActivity = activity;
        this.mDialog = new ProgressDialog(activity, android.R.style.Theme_Material_Dialog_Alert);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog.setMessage("Đang xử lý...");
        mDialog.setCancelable(false);
        mDialog.setButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                publishProgress(null);
            }
        });
        mDialog.show();
    }

    @Override
    protected Void doInBackground(Point... params) {
        final Point clickPoint = params[0];
        final Feature feature;
        try {
            feature = mServiceFeatureTable.createFeature();
            feature.setGeometry(clickPoint);
            String typeIdField = mServiceFeatureTable.getTypeIdField();
            List<Field> fields = mServiceFeatureTable.getFields();
            if(fields.size() > 0){
                for (Field field:fields){
                    if(field.getName().toUpperCase().equals(mActivity.getString(R.string.NGAYTHEMMOI))){
                        Calendar currentTime = Calendar.getInstance();
                        feature.getAttributes().put(field.getName(), currentTime);
                    }
                    if(field.getName().toUpperCase().equals(mActivity.getString(R.string.NGAYCAPNHAT))){
                        Calendar currentTime = Calendar.getInstance();
                        feature.getAttributes().put(field.getName(), currentTime);
                    }
                    if(field.getName().toUpperCase().equals(mActivity.getString(R.string.NGUOICAPNHAT))){
                        feature.getAttributes().put(field.getName(),this.mDApplication.getUser().getUserName() );
                    }
                    if(field.getName().equals(typeIdField)){
                        CodedValueDomain codedValueDomain = (CodedValueDomain) field.getDomain();
                        CodedValue codedValue = codedValueDomain.getCodedValues().get(0);
                        feature.getAttributes().put(typeIdField, codedValue.getCode());
                    }
                }
            }

            ListenableFuture<Void> mapViewResult = mServiceFeatureTable.addFeatureAsync(feature);
            mapViewResult.addDoneListener(new Runnable() {
                @Override
                public void run() {
                    if (mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    final ListenableFuture<List<FeatureEditResult>> listListenableEditAsync = mServiceFeatureTable.applyEditsAsync();
                    listListenableEditAsync.addDoneListener(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                List<FeatureEditResult> featureEditResults = listListenableEditAsync.get();
                                if (featureEditResults.size() > 0) {
                                    long objectId = featureEditResults.get(0).getObjectId();
                                    final QueryParameters queryParameters = new QueryParameters();
                                    final String query = "OBJECTID = " + objectId;
                                    queryParameters.setWhereClause(query);
                                    final ListenableFuture<FeatureQueryResult> feature = mServiceFeatureTable.queryFeaturesAsync(queryParameters);
                                    feature.addDoneListener(new Runnable() {
                                        @Override
                                        public void run() {

                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                MySnackBar.make(mMapView, "Không thêm được đối tượng. Vui lòng thử lại sau", true);
                            }

                        }
                    });
                }
            });

        } catch (Exception e) {
            MySnackBar.make(mMapView, "Không tạo được thuộc tính. Vui lòng thử lại sau", true);
        }


        return null;
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




    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);

    }


    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

    }

}
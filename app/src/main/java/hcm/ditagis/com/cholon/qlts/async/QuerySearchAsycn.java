package hcm.ditagis.com.cholon.qlts.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.SeekBar;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import hcm.ditagis.com.cholon.qlts.R;
import hcm.ditagis.com.cholon.qlts.adapter.ObjectsAdapter;
import hcm.ditagis.com.cholon.qlts.entities.entitiesDB.LayerInfoDTG;
import hcm.ditagis.com.cholon.qlts.entities.entitiesDB.ListObjectDB;
import hcm.ditagis.com.cholon.qlts.tools.Route;
import hcm.ditagis.com.cholon.qlts.utities.Constant;


public class QuerySearchAsycn extends AsyncTask<String, List<ObjectsAdapter.Item>, Void> {
    private ProgressDialog mDialog;
    private Context mContext;
    private AsyncResponse mDelegate;
    private ServiceFeatureTable mServiceFeatureTable;

    public interface AsyncResponse {
        void processFinish(List<ObjectsAdapter.Item> output);
    }

    public QuerySearchAsycn(Context context, ServiceFeatureTable serviceFeatureTable, AsyncResponse delegate) {
        this.mContext = context;
        this.mServiceFeatureTable = serviceFeatureTable;
        this.mDelegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.mDialog = new ProgressDialog(this.mContext, android.R.style.Theme_Material_Dialog_Alert);
        this.mDialog.setMessage("Đang tìm kiếm...");
        this.mDialog.setCancelable(true);
        this.mDialog.show();
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            if (params != null && params.length > 0) {
                List<ObjectsAdapter.Item> items = new ArrayList<>();
                String searchStr = params[0];
                QueryParameters queryParameters = new QueryParameters();
                StringBuilder builder = new StringBuilder();
                for (Field field : mServiceFeatureTable.getFields()) {
                    switch (field.getFieldType()) {
                        case OID:
                        case INTEGER:
                        case SHORT:
                            try {
                                int search = Integer.parseInt(searchStr);
                                builder.append(String.format("%s = %s", field.getName(), search));
                                builder.append(" or ");
                            } catch (Exception e) {

                            }
                            break;
                        case FLOAT:
                        case DOUBLE:
                            try {
                                double search = Double.parseDouble(searchStr);
                                builder.append(String.format("%s = %s", field.getName(), search));
                                builder.append(" or ");
                            } catch (Exception e) {

                            }
                            break;
                        case TEXT:
                            builder.append(field.getName() + " like N'%" + searchStr + "%'");
                            builder.append(" or ");
                            break;
                    }
                }
                builder.append(" 1 = 2 ");
                queryParameters.setWhereClause(builder.toString());
                final ListenableFuture<FeatureQueryResult> feature = mServiceFeatureTable.queryFeaturesAsync(queryParameters);
                feature.addDoneListener(() -> {
                    try {
                        FeatureQueryResult result = feature.get();
                        Iterator iterator = result.iterator();
                        if (iterator.hasNext()) {
                            while (iterator.hasNext()) {
                                Feature item = (Feature) iterator.next();
                                Map<String, Object> attributes = item.getAttributes();
                                String objectid = attributes.get(Constant.OBJECTID).toString();
                                items.add(new ObjectsAdapter.Item(objectid, objectid, ""));

//                        queryByObjectID(Integer.parseInt(attributes.get(Constant.OBJECTID).toString()));
                            }
                            publishProgress(items);
                        } else
                            publishProgress();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        publishProgress();
                    }
                });
            }
        } catch (Exception e) {
            Log.e("Lỗi tim kiem", e.toString());
            publishProgress();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(List<ObjectsAdapter.Item>... values) {
        super.onProgressUpdate(values);

        mDialog.dismiss();
        if (values != null && values.length > 0)
            mDelegate.processFinish(values[0]);
        else mDelegate.processFinish(null);
    }

}

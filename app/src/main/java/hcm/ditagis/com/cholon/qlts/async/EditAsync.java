package hcm.ditagis.com.cholon.qlts.async;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Attachment;
import com.esri.arcgisruntime.data.CodedValue;
import com.esri.arcgisruntime.data.CodedValueDomain;
import com.esri.arcgisruntime.data.Domain;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureType;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.symbology.Renderer;
import com.esri.arcgisruntime.symbology.UniqueValueRenderer;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import hcm.ditagis.com.cholon.qlts.R;
import hcm.ditagis.com.cholon.qlts.adapter.FeatureViewMoreInfoAdapter;
import hcm.ditagis.com.cholon.qlts.utities.Constant;
import hcm.ditagis.com.cholon.qlts.utities.DApplication;

/**
 * Created by ThanLe on 4/16/2018.
 */

public class EditAsync extends AsyncTask<FeatureViewMoreInfoAdapter, Void, Void> {
    public interface AsyncResponse {
        void processFinish();
    }

    public AsyncResponse delegate = null;
    private ProgressDialog mDialog;
    private Activity mMainActivity;
    private ServiceFeatureTable mServiceFeatureTable;
    private ArcGISFeature mSelectedArcGISFeature = null;
    private DApplication mDApplication;

    public EditAsync(Activity mainActivity, ServiceFeatureTable serviceFeatureTable, ArcGISFeature selectedArcGISFeature, AsyncResponse delegate) {
        mMainActivity = mainActivity;
        mServiceFeatureTable = serviceFeatureTable;
        mSelectedArcGISFeature = selectedArcGISFeature;
        mDialog = new ProgressDialog(mMainActivity, android.R.style.Theme_Material_Dialog_Alert);
        this.delegate = delegate;
        this.mDApplication = (DApplication) mMainActivity.getApplication();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog.setMessage(mMainActivity.getString(R.string.async_dang_xu_ly));
        mDialog.setCancelable(false);
        mDialog.setButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                publishProgress();
            }
        });
        mDialog.show();

    }

    @Override
    protected Void doInBackground(FeatureViewMoreInfoAdapter... params) {
        FeatureViewMoreInfoAdapter adapter = params[0];
        Renderer renderer = mSelectedArcGISFeature.getFeatureTable().getLayerInfo().getDrawingInfo().getRenderer();
        List<UniqueValueRenderer.UniqueValue> uniqueValues  = null;
        String fieldName = null;
        if(renderer instanceof UniqueValueRenderer){
            UniqueValueRenderer uniqueValueRenderer = (UniqueValueRenderer) renderer;
            uniqueValues = uniqueValueRenderer.getUniqueValues();
            fieldName = uniqueValueRenderer.getFieldNames().get(0);
        }
        try {
            for (FeatureViewMoreInfoAdapter.Item item : adapter.getItems()) {
                if (item.getValue() == null || !item.isEdit()) continue;
                Domain domain = mSelectedArcGISFeature.getFeatureTable().getField(item.getFieldName()).getDomain();
                Object codeDomain = null;
                if (domain != null) {
                    List<CodedValue> codedValues = ((CodedValueDomain) this.mSelectedArcGISFeature.getFeatureTable().getField(item.getFieldName()).getDomain()).getCodedValues();
                    codeDomain = getCodeDomain(codedValues, item.getValue());
                }

                if (uniqueValues != null && uniqueValues.size() > 0 && item.getFieldName().equals(fieldName)) {
                    Object valueUniqueRenderer = getValueUniqueRenderer(uniqueValues, item.getValue());
                    mSelectedArcGISFeature.getAttributes().put(item.getFieldName(), Short.parseShort(valueUniqueRenderer.toString()));

                }
                else switch (item.getFieldType()) {
                    case DATE:
                        Date date = null;
                        try {
                            date = Constant.DATE_FORMAT.parse(item.getValue());
                            Calendar c = Calendar.getInstance();
                            c.setTime(date);
                            mSelectedArcGISFeature.getAttributes().put(item.getFieldName(), c);
                        } catch (ParseException e) {
                        }
                        break;

                    case TEXT:
                        if (codeDomain != null) {
                            mSelectedArcGISFeature.getAttributes().put(item.getFieldName(), codeDomain.toString());
                        } else
                            mSelectedArcGISFeature.getAttributes().put(item.getFieldName(), item.getValue());
                        break;
                    case INTEGER:
                        if (codeDomain != null) {
                            mSelectedArcGISFeature.getAttributes().put(item.getFieldName(), Integer.parseInt(codeDomain.toString()));
                        } else
                            mSelectedArcGISFeature.getAttributes().put(item.getFieldName(), Integer.parseInt(item.getValue()));
                        break;
                    case SHORT:
                        if (codeDomain != null) {
                            mSelectedArcGISFeature.getAttributes().put(item.getFieldName(), Short.parseShort(codeDomain.toString()));
                        } else
                            mSelectedArcGISFeature.getAttributes().put(item.getFieldName(), Short.parseShort(item.getValue()));
                        break;
                }
            }
        } catch (Exception e) {
            Log.e("",e.toString());
        }
        List<Field> fields = mSelectedArcGISFeature.getFeatureTable().getFields();
        for(Field field:fields){
            if (field.getName().toUpperCase().equals(mMainActivity.getResources().getString(R.string.NGAYCAPNHAT))) {
                Calendar currentTime = Calendar.getInstance();
                mSelectedArcGISFeature.getAttributes().put(field.getName(), currentTime);
            }
            if (field.getName().toUpperCase().equals(mMainActivity.getResources().getString(R.string.NGUOICAPNHAT))) {
                String userName = this.mDApplication.getUser().getUserName();
                mSelectedArcGISFeature.getAttributes().put(field.getName(), userName);
            }
        }


        mServiceFeatureTable.loadAsync();
        mServiceFeatureTable.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                try {
                    updateFeature(mSelectedArcGISFeature);


                } catch (Exception e) {
                }
            }
        });
        return null;
    }

    private void updateFeature(final Feature feature) {
        final ListenableFuture<Void> mapViewResult = mServiceFeatureTable.updateFeatureAsync(feature);
        mapViewResult.addDoneListener(new Runnable() {
            @Override
            public void run() {
                final ListenableFuture<List<FeatureEditResult>> listListenableEditAsync = mServiceFeatureTable.applyEditsAsync();
                listListenableEditAsync.addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mDialog.dismiss();
                            List<FeatureEditResult> featureEditResults = listListenableEditAsync.get();
                            if (featureEditResults.size() > 0) {
                                long objectId = featureEditResults.get(0).getObjectId();

                            }
                            publishProgress();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });
    }


    private Object getIdFeatureTypes(List<FeatureType> featureTypes, String value) {
        Object code = null;
        for (FeatureType featureType : featureTypes) {
            if (featureType.getName().equals(value)) {
                code = featureType.getId();
                break;
            }
        }
        return code;
    }

    private Object getValueUniqueRenderer(List<UniqueValueRenderer.UniqueValue> uniqueValues, String label) {
        Object value = null;
        for (UniqueValueRenderer.UniqueValue uniqueValue : uniqueValues) {
            if (uniqueValue.getLabel() != null && uniqueValue.getLabel().toString().equals(label)) {
                value = uniqueValue.getValues().get(0).toString();
                break;
            }
        }
        return value;
    }

    private Object getCodeDomain(List<CodedValue> codedValues, String value) {
        Object code = null;
        for (CodedValue codedValue : codedValues) {
            if (codedValue.getName().equals(value)) {
                code = codedValue.getCode();
                break;
            }

        }
        return code;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        delegate.processFinish();

    }


    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

    }

}


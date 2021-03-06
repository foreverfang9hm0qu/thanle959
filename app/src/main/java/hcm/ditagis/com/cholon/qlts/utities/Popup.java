package hcm.ditagis.com.cholon.qlts.utities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.CodedValue;
import com.esri.arcgisruntime.data.CodedValueDomain;
import com.esri.arcgisruntime.data.Domain;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureType;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.Renderer;
import com.esri.arcgisruntime.symbology.UniqueValueRenderer;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import hcm.ditagis.com.cholon.qlts.MainActivity;
import hcm.ditagis.com.cholon.qlts.R;
import hcm.ditagis.com.cholon.qlts.adapter.FeatureViewInfoAdapter;
import hcm.ditagis.com.cholon.qlts.adapter.FeatureViewMoreInfoAdapter;
import hcm.ditagis.com.cholon.qlts.async.EditAsync;
import hcm.ditagis.com.cholon.qlts.async.NotifyDataSetChangeAsync;
import hcm.ditagis.com.cholon.qlts.async.QueryHanhChinhAsync;
import hcm.ditagis.com.cholon.qlts.async.ViewAttachmentAsync;
import hcm.ditagis.com.cholon.qlts.libs.FeatureLayerDTG;

public class Popup extends AppCompatActivity {
    private MainActivity mMainActivity;
    private ArcGISFeature mSelectedArcGISFeature = null;
    private ServiceFeatureTable mServiceFeatureTable;
    private Callout mCallout;
    private FeatureLayerDTG mFeatureLayerDTG;
    private List<String> lstUniqueValues;
    private String fieldNameDrawInfo;
    private LinearLayout linearLayout;
    private Uri mUri;
    private FeatureViewMoreInfoAdapter mFeatureViewMoreInfoAdapter;
    private MapView mMapView;
    private ArrayList<Feature> quanhuyen_features;
    private Feature quanhuyen_feature;
    private DApplication mDApplication;


    public Popup(MainActivity mainActivity, MapView mMapView, Callout callout) {
        this.mMainActivity = mainActivity;
        this.mMapView = mMapView;
        this.mCallout = callout;
        this.mDApplication = (DApplication) mainActivity.getApplication();


    }


    public void setmSFTHanhChinh(ServiceFeatureTable mSFTHanhChinh) {
        new QueryHanhChinhAsync(mMainActivity, mSFTHanhChinh, new QueryHanhChinhAsync.AsyncResponse() {
            @Override
            public void processFinish(ArrayList<Feature> output) {
                quanhuyen_features = output;
            }
        }).execute();
    }

    public void setFeatureLayerDTG(FeatureLayerDTG layerDTG) {
        this.mFeatureLayerDTG = layerDTG;
    }

    public void refressPopup() {
        String[] hiddenFields = mMainActivity.getResources().getStringArray(R.array.hiddenFields);
        Map<String, Object> attributes = mSelectedArcGISFeature.getAttributes();
        ListView listView = linearLayout.findViewById(R.id.lstview_thongtinsuco);
        FeatureViewInfoAdapter featureViewInfoAdapter = new FeatureViewInfoAdapter(mMainActivity, new ArrayList<FeatureViewInfoAdapter.Item>());
        listView.setAdapter(featureViewInfoAdapter);

        Renderer renderer = mSelectedArcGISFeature.getFeatureTable().getLayerInfo().getDrawingInfo().getRenderer();
        UniqueValueRenderer uniqueValueRenderer = null;
        if (renderer instanceof UniqueValueRenderer) {
            uniqueValueRenderer = (UniqueValueRenderer) renderer;
            fieldNameDrawInfo = uniqueValueRenderer.getFieldNames().get(0);

        }
        boolean checkHiddenField;
        for (Field field : this.mSelectedArcGISFeature.getFeatureTable().getFields()) {
            checkHiddenField = false;
            for (String hiddenField : hiddenFields) {
                if (hiddenField.equals(field.getName())) {
                    checkHiddenField = true;
                    break;
                }
            }
            Object value = attributes.get(field.getName());
            if (value != null && !checkHiddenField) {
                FeatureViewInfoAdapter.Item item = new FeatureViewInfoAdapter.Item();
                item.setAlias(field.getAlias());
                item.setFieldName(field.getName());
                if (item.getFieldName().toUpperCase().equals("MAPHUONG")) {
                    getHanhChinhFeature(value.toString());
                    if (quanhuyen_feature != null)
                        item.setValue(quanhuyen_feature.getAttributes().get("TenHanhChinh").toString());
                } else if (item.getFieldName().toUpperCase().equals("MAQUAN")) {
                    if (quanhuyen_feature != null)
                        item.setValue(quanhuyen_feature.getAttributes().get("TenQuan").toString());
                } else if (item.getFieldName().equals(fieldNameDrawInfo)) {
                    List<UniqueValueRenderer.UniqueValue> uniqueValues = uniqueValueRenderer.getUniqueValues();
                    if (uniqueValues.size() > 0) {
                        Object valueFeatureType = getLabelUniqueRenderer(uniqueValues, value.toString());
                        if (valueFeatureType != null) item.setValue(valueFeatureType.toString());
                    } else item.setValue(value.toString());

                } else if (field.getDomain() != null) {
                    List<CodedValue> codedValues = ((CodedValueDomain) this.mSelectedArcGISFeature.getFeatureTable().getField(item.getFieldName()).getDomain()).getCodedValues();
                    Object valueDomainObject = getValueDomain(codedValues, value.toString());
                    if (valueDomainObject != null) item.setValue(valueDomainObject.toString());
                } else switch (field.getFieldType()) {
                    case DATE:
                        item.setValue(Constant.DATE_FORMAT.format(((Calendar) value).getTime()));
                        break;
                    default:
                        item.setValue(value.toString());
                }

                featureViewInfoAdapter.add(item);
                featureViewInfoAdapter.notifyDataSetChanged();
            }
        }
    }


    private void viewMoreInfo() {
        Map<String, Object> attr = mSelectedArcGISFeature.getAttributes();
        AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        View layout = mMainActivity.getLayoutInflater().inflate(R.layout.layout_viewmoreinfo_feature, null);
        mFeatureViewMoreInfoAdapter = new FeatureViewMoreInfoAdapter(mMainActivity, new ArrayList<FeatureViewMoreInfoAdapter.Item>());
        final ListView lstViewInfo = layout.findViewById(R.id.lstView_alertdialog_info);
        lstViewInfo.setAdapter(mFeatureViewMoreInfoAdapter);
        lstViewInfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                edit(parent, view, position, id);
            }
        });

        String[] updateFields = mFeatureLayerDTG.getUpdateFields();
        String[] unedit_Fields = mMainActivity.getResources().getStringArray(R.array.unedit_Fields);
        String[] hiddenFields = mMainActivity.getResources().getStringArray(R.array.hiddenFields);
        Renderer renderer = mSelectedArcGISFeature.getFeatureTable().getLayerInfo().getDrawingInfo().getRenderer();
        UniqueValueRenderer uniqueValueRenderer = null;
        if (renderer instanceof UniqueValueRenderer) {
            uniqueValueRenderer = (UniqueValueRenderer) renderer;
        }
        boolean checkHiddenField;
        for (Field field : this.mSelectedArcGISFeature.getFeatureTable().getFields()) {
            checkHiddenField = false;
            for (String hiddenField : hiddenFields) {
                if (field.getName().toUpperCase().equals(hiddenField.toUpperCase())) {
                    checkHiddenField = true;
                    break;
                }
            }
            if (checkHiddenField) continue;
            Object value = attr.get(field.getName());
            if (field.getName().equals(Constant.IDSU_CO)) {
                if (value != null)
                    ((TextView) layout.findViewById(R.id.txt_alertdialog_id_su_co)).setText(value.toString());
            } else {
                FeatureViewMoreInfoAdapter.Item item = new FeatureViewMoreInfoAdapter.Item();
                item.setAlias(field.getAlias());
                item.setFieldName(field.getName());
                if (updateFields.length > 0) {
                    if (updateFields[0].equals("*")) {
                        item.setEdit(true);
                    } else {
                        for (String updateField : updateFields) {
                            if (item.getFieldName().equals(updateField)) {
                                item.setEdit(true);
                                break;
                            }
                        }
                    }
                }
                for (String unedit_Field : unedit_Fields) {
                    if (unedit_Field.toUpperCase().equals(item.getFieldName().toUpperCase())) {
                        item.setEdit(false);
                        break;
                    }
                }
                if (value != null) {
                    if (item.getFieldName().equals(fieldNameDrawInfo)) {
                        List<UniqueValueRenderer.UniqueValue> uniqueValues = uniqueValueRenderer.getUniqueValues();
                        if (uniqueValues.size() > 0) {
                            Object valueFeatureType = getLabelUniqueRenderer(uniqueValues, value.toString());
                            if (valueFeatureType != null)
                                item.setValue(valueFeatureType.toString());
                        } else item.setValue(value.toString());

                    } else if (item.getFieldName().toUpperCase().equals("MAPHUONG")) {
                        getHanhChinhFeature(value.toString());
                        if (quanhuyen_feature != null)
                            item.setValue(quanhuyen_feature.getAttributes().get("TenHanhChinh").toString());
                    } else if (item.getFieldName().toUpperCase().equals("MAQUAN")) {
                        if (quanhuyen_feature != null)
                            item.setValue(quanhuyen_feature.getAttributes().get("TenQuan").toString());
                    } else if (field.getDomain() != null) {
                        List<CodedValue> codedValues = ((CodedValueDomain) this.mSelectedArcGISFeature.getFeatureTable().getField(item.getFieldName()).getDomain()).getCodedValues();
                        Object valueDomainObject = getValueDomain(codedValues, value.toString());
                        if (valueDomainObject != null) item.setValue(valueDomainObject.toString());
                    } else switch (field.getFieldType()) {
                        case DATE:
                            item.setValue(Constant.DATE_FORMAT.format(((Calendar) value).getTime()));
                            break;
                        case OID:
                        case TEXT:
                            item.setValue(value.toString());
                            break;
                        case DOUBLE:
                        case INTEGER:
                        case SHORT:
                            item.setValue(value.toString());

                            break;
                    }
                }


                item.setFieldType(field.getFieldType());
                mFeatureViewMoreInfoAdapter.add(item);
                mFeatureViewMoreInfoAdapter.notifyDataSetChanged();
            }
        }

        builder.setView(layout);
        builder.setCancelable(false);
        builder.setPositiveButton("Tho??t", new DialogInterface.OnClickListener()

        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setNegativeButton("C???p nh???t", (dialog, which) -> {
            if (mSelectedArcGISFeature.canUpdateGeometry()) {
                new EditAsync(mMainActivity, mServiceFeatureTable, mSelectedArcGISFeature, () -> refressPopup()).execute(mFeatureViewMoreInfoAdapter);
            } else
                Toast.makeText(mMainActivity, "Kh??ng ???????c quy???n ch???nh s???a d??? li???u!!!", Toast.LENGTH_LONG).show();

        });
        final AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();


    }

    private void viewAttachment() {
        ViewAttachmentAsync viewAttachmentAsync = new ViewAttachmentAsync(mMainActivity, mSelectedArcGISFeature);
        viewAttachmentAsync.execute();
    }

    private Object getValueDomain(List<CodedValue> codedValues, String code) {
        Object value = null;
        for (CodedValue codedValue : codedValues) {
            if (codedValue.getCode().toString().equals(code)) {
                value = codedValue.getName();
                break;
            }

        }
        return value;
    }

    private Object getValueFeatureType(List<FeatureType> featureTypes, String code) {
        Object value = null;
        for (FeatureType featureType : featureTypes) {
            if (featureType.getId() != null && featureType.getId().toString().equals(code)) {
                value = featureType.getName();
                break;
            }
        }
        return value;
    }

    private Object getLabelUniqueRenderer(List<UniqueValueRenderer.UniqueValue> uniqueValues, String code) {
        Object value = null;
        for (UniqueValueRenderer.UniqueValue uniqueValue : uniqueValues) {
            if (uniqueValue.getValues() != null && uniqueValue.getValues().get(0).toString().equals(code)) {
                value = uniqueValue.getLabel();
                break;
            }
        }
        return value;
    }

    private void edit1(final AdapterView<?> parent, View view, int position, long id) {
        if (parent.getItemAtPosition(position) instanceof FeatureViewMoreInfoAdapter.Item) {
            final FeatureViewMoreInfoAdapter.Item item = (FeatureViewMoreInfoAdapter.Item) parent.getItemAtPosition(position);
            if (item.isEdit()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity, android.R.style.Theme_Material_Light_Dialog_Alert);
                builder.setTitle("C???p nh???t thu???c t??nh");
                builder.setMessage(item.getAlias());
                builder.setCancelable(false).setNegativeButton("H???y", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                final LinearLayout layout = (LinearLayout) mMainActivity.getLayoutInflater().
                        inflate(R.layout.layout_dialog_update_feature_listview, null);

                final FrameLayout layoutTextView = layout.findViewById(R.id.layout_edit_viewmoreinfo_TextView);
                final TextView textView = layout.findViewById(R.id.txt_edit_viewmoreinfo);
                ImageView img_selectTime = (ImageView) layout.findViewById(R.id.img_selectLayer);
                final LinearLayout layoutEditText = layout.findViewById(R.id.layout_edit_viewmoreinfo_Editext);
                final EditText editText = layout.findViewById(R.id.etxt_edit_viewmoreinfo);
                final LinearLayout layoutSpin = layout.findViewById(R.id.layout_edit_viewmoreinfo_Spinner);
                final Spinner spin = layout.findViewById(R.id.spin_edit_viewmoreinfo);

                final Domain domain = mSelectedArcGISFeature.getFeatureTable().getField(item.getFieldName()).getDomain();
                if (item.getFieldName().equals(fieldNameDrawInfo)) {
                    if (lstUniqueValues.size() > 0) {
                        layoutSpin.setVisibility(View.VISIBLE);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(layout.getContext(), android.R.layout.simple_list_item_1, lstUniqueValues);
                        spin.setAdapter(adapter);
                        if (item.getValue() != null)
                            spin.setSelection(lstUniqueValues.indexOf(item.getValue()));
                    } else {
                        layoutEditText.setVisibility(View.VISIBLE);
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        editText.setText(item.getValue());
                    }
                } else if (domain != null) {
                    layoutSpin.setVisibility(View.VISIBLE);
                    List<CodedValue> codedValues = ((CodedValueDomain) domain).getCodedValues();
                    if (codedValues != null) {
                        List<String> codes = new ArrayList<>();
                        for (CodedValue codedValue : codedValues)
                            codes.add(codedValue.getName());
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(layout.getContext(), android.R.layout.simple_list_item_1, codes);
                        spin.setAdapter(adapter);
                        if (item.getValue() != null)
                            spin.setSelection(codes.indexOf(item.getValue()));

                    }
                } else switch (item.getFieldType()) {
                    case DATE:
                        layoutTextView.setVisibility(View.VISIBLE);
                        textView.setText(item.getValue());
                        img_selectTime.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final View dialogView = View.inflate(mMainActivity, R.layout.date_time_picker, null);
                                final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(mMainActivity).create();
                                dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
                                        String s = String.format(mMainActivity.getResources().getString(R.string.format_typeday), datePicker.getDayOfMonth(), datePicker.getMonth() + 1, datePicker.getYear());
                                        textView.setText(s);
                                        alertDialog.dismiss();
                                    }
                                });
                                alertDialog.setView(dialogView);
                                alertDialog.show();
                            }
                        });
                        break;
                    case TEXT:
                        layoutEditText.setVisibility(View.VISIBLE);
                        editText.setText(item.getValue());
                        break;
                    case INTEGER:
                    case SHORT:
                        layoutEditText.setVisibility(View.VISIBLE);
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        editText.setText(item.getValue());


                        break;

                    case DOUBLE:
                        layoutEditText.setVisibility(View.VISIBLE);
                        editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        editText.setText(item.getValue());
                        break;
                }
                builder.setPositiveButton("C???p nh???t", (dialog, which) -> {
                    String value = null;
                    if ((lstUniqueValues.size() > 0 && item.getFieldName().equals(fieldNameDrawInfo)) || (domain != null)) {
                        value = spin.getSelectedItem().toString();
                    } else {
                        switch (item.getFieldType()) {
                            case DATE:
                                value = textView.getText().toString();
                                break;
                            case FLOAT:
                            case DOUBLE:
                                try {
                                    double x = Double.parseDouble(editText.getText().toString());
                                    value = editText.getText().toString();
                                } catch (Exception e) {
                                    Toast.makeText(mMainActivity, mMainActivity.getResources().getString(R.string.INCORRECT_INPUT_FORMAT), Toast.LENGTH_LONG).show();
                                }
                                break;
                            case TEXT:
                                value = editText.getText().toString();
                                break;
                            case INTEGER:
                                try {
                                    int x = Integer.parseInt(editText.getText().toString());
                                    value = editText.getText().toString();
                                } catch (Exception e) {
                                    Toast.makeText(mMainActivity, mMainActivity.getResources().getString(R.string.INCORRECT_INPUT_FORMAT), Toast.LENGTH_LONG).show();
                                }
                                break;
                            case SHORT:
                                try {
                                    short x = Short.parseShort(editText.getText().toString());
                                    value = editText.getText().toString();
                                } catch (Exception e) {
                                    Toast.makeText(mMainActivity, mMainActivity.getResources().getString(R.string.INCORRECT_INPUT_FORMAT), Toast.LENGTH_LONG).show();
                                }
                                break;
                        }
                    }
                    if (value != null) {
                        item.setValue(value);
                        FeatureViewMoreInfoAdapter adapter = (FeatureViewMoreInfoAdapter) parent.getAdapter();
                        new NotifyDataSetChangeAsync(mMainActivity).execute(adapter);
                    }
                });
                builder.setView(layout);
                AlertDialog dialog = builder.create();
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.show();

            }
        }

    }

    private void edit(final AdapterView<?> parent, View view, int position, long id) {
        if (parent.getItemAtPosition(position) instanceof FeatureViewMoreInfoAdapter.Item) {
            final FeatureViewMoreInfoAdapter.Item item = (FeatureViewMoreInfoAdapter.Item) parent.getItemAtPosition(position);
            if (item.isEdit()) {
                final LinearLayout layout = (LinearLayout) mMainActivity.getLayoutInflater().
                        inflate(R.layout.layout_dialog_update_feature_listview, null);

                final FrameLayout layoutTextView = layout.findViewById(R.id.layout_edit_viewmoreinfo_TextView);
                final TextView textView = layout.findViewById(R.id.txt_edit_viewmoreinfo);
                TextView txtNotifyInCorrect = layout.findViewById(R.id.txtNotifyInCorrect);
                ImageView img_selectTime = (ImageView) layout.findViewById(R.id.img_selectLayer);
                final LinearLayout layoutEditText = layout.findViewById(R.id.layout_edit_viewmoreinfo_Editext);
                final EditText editText = layout.findViewById(R.id.etxt_edit_viewmoreinfo);
                final LinearLayout layoutSpin = layout.findViewById(R.id.layout_edit_viewmoreinfo_Spinner);
                final Spinner spin = layout.findViewById(R.id.spin_edit_viewmoreinfo);

                final Domain domain = mSelectedArcGISFeature.getFeatureTable().getField(item.getFieldName()).getDomain();
                if (item.getFieldName().equals(fieldNameDrawInfo)) {
                    if (lstUniqueValues.size() > 0) {
                        layoutSpin.setVisibility(View.VISIBLE);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(layout.getContext(), android.R.layout.simple_list_item_1, lstUniqueValues);
                        spin.setAdapter(adapter);
                        if (item.getValue() != null)
                            spin.setSelection(lstUniqueValues.indexOf(item.getValue()));
                    } else {
                        layoutEditText.setVisibility(View.VISIBLE);
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        editText.setText(item.getValue());
                    }
                } else if (domain != null) {
                    layoutSpin.setVisibility(View.VISIBLE);
                    List<CodedValue> codedValues = ((CodedValueDomain) domain).getCodedValues();
                    if (codedValues != null) {
                        List<String> codes = new ArrayList<>();
                        for (CodedValue codedValue : codedValues)
                            codes.add(codedValue.getName());
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(layout.getContext(), android.R.layout.simple_list_item_1, codes);
                        spin.setAdapter(adapter);
                        if (item.getValue() != null)
                            spin.setSelection(codes.indexOf(item.getValue()));

                    }
                } else switch (item.getFieldType()) {
                    case DATE:
                        layoutTextView.setVisibility(View.VISIBLE);
                        textView.setText(item.getValue());
                        img_selectTime.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final View dialogView = View.inflate(mMainActivity, R.layout.date_time_picker, null);
                                final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(mMainActivity).create();
                                dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
                                        String s = String.format(mMainActivity.getResources().getString(R.string.format_typeday), datePicker.getDayOfMonth(), datePicker.getMonth() + 1, datePicker.getYear());
                                        textView.setText(s);
                                        alertDialog.dismiss();
                                    }
                                });
                                alertDialog.setView(dialogView);
                                alertDialog.show();
                            }
                        });
                        break;
                    case TEXT:
                        layoutEditText.setVisibility(View.VISIBLE);
                        editText.setText(item.getValue());
                        break;
                    case INTEGER:
                    case SHORT:
                        layoutEditText.setVisibility(View.VISIBLE);
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        editText.setText(item.getValue());
                        break;
                    case DOUBLE:
                    case FLOAT:
                        layoutEditText.setVisibility(View.VISIBLE);
                        editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        editText.setText(item.getValue());
                        break;
                }
                final AlertDialog dialog = new AlertDialog.Builder(mMainActivity)
                        .setView(layout)
                        .setMessage(item.getAlias())
                        .setTitle("C???p nh???t thu???c t??nh")
                        .setPositiveButton(R.string.btn_Update, null) //Set to null. We override the onclick
                        .setNegativeButton(R.string.btn_Esc, null)
                        .create();

                dialog.setOnShowListener(dialogInterface -> {

                    Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(view1 -> {
                        String value = null;
                        if ((lstUniqueValues.size() > 0 && item.getFieldName().equals(fieldNameDrawInfo)) || (domain != null)) {
                            value = spin.getSelectedItem().toString();
                        } else {
                            switch (item.getFieldType()) {
                                case DATE:
                                    value = textView.getText().toString();
                                    break;
                                case FLOAT:
                                case DOUBLE:
                                    try {
                                        double x = Double.parseDouble(editText.getText().toString());
                                        value = editText.getText().toString();
                                    } catch (Exception e) {
                                        txtNotifyInCorrect.setVisibility(View.VISIBLE);
                                    }
                                    break;
                                case TEXT:
                                    value = editText.getText().toString();
                                    break;
                                case INTEGER:
                                    try {
                                        int x = Integer.parseInt(editText.getText().toString());
                                        value = editText.getText().toString();
                                    } catch (Exception e) {
                                        txtNotifyInCorrect.setVisibility(View.VISIBLE);
                                    }
                                    break;
                                case SHORT:
                                    try {
                                        short x = Short.parseShort(editText.getText().toString());
                                        value = editText.getText().toString();
                                    } catch (Exception e) {
                                        txtNotifyInCorrect.setVisibility(View.VISIBLE);
                                    }
                                    break;
                            }
                        }
                        if (value != null) {
                            dialog.dismiss();
                            item.setValue(value);
                            FeatureViewMoreInfoAdapter adapter = (FeatureViewMoreInfoAdapter) parent.getAdapter();
                            new NotifyDataSetChangeAsync(mMainActivity).execute(adapter);
                        } else txtNotifyInCorrect.setVisibility(View.VISIBLE);
                    });
                });
                dialog.show();
            }
        }
    }

    public void clearSelection() {
        if (mFeatureLayerDTG != null) {
            FeatureLayer featureLayer = mFeatureLayerDTG.getFeatureLayer();
            featureLayer.clearSelection();
        }
    }

    public void dimissCallout() {
        this.clearSelection();
        if (mCallout != null && mCallout.isShowing()) {
            mCallout.dismiss();
        }
    }

    private void getHanhChinhFeature(String IDHanhChinh) {
        quanhuyen_feature = null;
        if (quanhuyen_features != null) {
            for (Feature feature : quanhuyen_features) {
                Object idHanhChinh = feature.getAttributes().get("IDHanhChinh");
                if (idHanhChinh != null && idHanhChinh.equals(IDHanhChinh)) {
                    quanhuyen_feature = feature;
                }
            }
        }
    }

    public LinearLayout showPopup(final ArcGISFeature mSelectedArcGISFeature, Boolean clickMap) {
        dimissCallout();
        mServiceFeatureTable = (ServiceFeatureTable) mFeatureLayerDTG.getFeatureLayer().getFeatureTable();
        this.mSelectedArcGISFeature = mSelectedArcGISFeature;
        FeatureLayer featureLayer = mFeatureLayerDTG.getFeatureLayer();
        featureLayer.selectFeature(mSelectedArcGISFeature);
        lstUniqueValues = new ArrayList<>();
        Renderer renderer = mSelectedArcGISFeature.getFeatureTable().getLayerInfo().getDrawingInfo().getRenderer();
        List<UniqueValueRenderer.UniqueValue> uniqueValues = null;
        if (renderer instanceof UniqueValueRenderer) {
            UniqueValueRenderer uniqueValueRenderer = (UniqueValueRenderer) renderer;
            uniqueValues = uniqueValueRenderer.getUniqueValues();
        }
        if (uniqueValues != null) {
            for (int i = 0; i < uniqueValues.size(); i++) {
                lstUniqueValues.add(uniqueValues.get(i).getLabel().toString());
            }
        }

        LayoutInflater inflater = LayoutInflater.from(this.mMainActivity.getApplicationContext());
        linearLayout = (LinearLayout) inflater.inflate(R.layout.layout_popup_infos, null);
        refressPopup();
        ((TextView) linearLayout.findViewById(R.id.txt_title_layer)).setText(mFeatureLayerDTG.getFeatureLayer().getName());
        ImageButton imgBtn_ViewMoreInfo = (ImageButton) linearLayout.findViewById(R.id.imgBtn_ViewMoreInfo);
        if (mFeatureLayerDTG.getAction().isEdit()) {
            imgBtn_ViewMoreInfo.setVisibility(View.VISIBLE);
            imgBtn_ViewMoreInfo.setOnClickListener(v -> viewMoreInfo());
        } else imgBtn_ViewMoreInfo.setVisibility(View.GONE);
        ImageButton imgBtn_view_attachment = (ImageButton) linearLayout.findViewById(R.id.imgBtn_view_attachment);
        if (this.mSelectedArcGISFeature.canEditAttachments()) {
            imgBtn_view_attachment.setVisibility(View.VISIBLE);
            imgBtn_view_attachment.setOnClickListener(v -> viewAttachment());
        } else imgBtn_view_attachment.setVisibility(View.GONE);
        ImageButton imgBtn_delete = (ImageButton) linearLayout.findViewById(R.id.imgBtn_delete);

        if (mFeatureLayerDTG.getAction().isDelete() && isDeleteFeature()) {
            imgBtn_delete.setVisibility(View.VISIBLE);
            imgBtn_delete.setOnClickListener(v -> {
                mSelectedArcGISFeature.getFeatureTable().getFeatureLayer().clearSelection();
                deleteFeature();
            });
        } else imgBtn_delete.setVisibility(View.GONE);
        ((Button) linearLayout.findViewById(R.id.btn_layer_close)).setOnClickListener(v -> dimissCallout());
        if (mFeatureLayerDTG.getAction().isEdit() && this.mSelectedArcGISFeature.canEditAttachments()) {
            ImageButton imgBtn_takePics = (ImageButton) linearLayout.findViewById(R.id.imgBtn_takePics);
            imgBtn_takePics.setVisibility(View.VISIBLE);
            imgBtn_takePics.setOnClickListener(v -> updateAttachment());
        }
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Envelope envelope = mSelectedArcGISFeature.getGeometry().getExtent();
        if (!clickMap) mMapView.setViewpointGeometryAsync(envelope, 0);
        // show CallOut
        mCallout.setLocation(envelope.getCenter());
        mCallout.setContent(linearLayout);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCallout.refresh();
                mCallout.show();
            }
        });
        return linearLayout;
    }

    public static boolean isPast1DateOfCurrentDate(Calendar calendar, Calendar currentDate) {
        if (calendar == null || currentDate == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        long deviationTime = currentDate.getTimeInMillis() - calendar.getTimeInMillis();
        int time = 24 * 60 * 60 * 1000;
        return (time - deviationTime) > 0;
    }

    private boolean isDeleteFeature() {
        boolean isPast1DateOfCurrentDate = false, isSameUser = false;
        Map<String, Object> attr = mSelectedArcGISFeature.getAttributes();
        List<Field> fields = mSelectedArcGISFeature.getFeatureTable().getFields();
        for (Field field : fields) {
            if (field.getName().toUpperCase().equals(this.mMainActivity.getResources().getString(R.string.NGAYTHEMMOI))) {
                Object ngayThemMoi = attr.get(field.getName());
                if (ngayThemMoi != null) {
                    Calendar calendar = (Calendar) ngayThemMoi;
                    Calendar currentDate = Calendar.getInstance();
                    isPast1DateOfCurrentDate = isPast1DateOfCurrentDate(calendar, currentDate);
                }
            }
            if (field.getName().toUpperCase().equals(this.mMainActivity.getResources().getString(R.string.NGUOICAPNHAT))) {
                Object nguoiCapNhat = attr.get(field.getName());
                if (nguoiCapNhat != null) {
                    isSameUser = nguoiCapNhat.toString().equals(this.mDApplication.getUser().getUserName());
                }
            }
        }
        return isPast1DateOfCurrentDate && isSameUser;
    }

    private void deleteFeature() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity, android.R.style.Theme_Material_Light_Dialog_Alert);
        builder.setTitle("X??c nh???n");
        builder.setMessage("B???n c?? ch???c ch???n x??a ?????i t?????ng n??y kh??ng?");
        builder.setPositiveButton("C??", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mSelectedArcGISFeature.loadAsync();

                // update the selected feature
                mSelectedArcGISFeature.addDoneLoadingListener(new Runnable() {
                    @Override
                    public void run() {
                        if (mSelectedArcGISFeature.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
                            Log.d(mMainActivity.getResources().getString(R.string.app_name), "Error while loading feature");
                        }
                        try {
                            // update feature in the feature table
                            ListenableFuture<Void> mapViewResult = mServiceFeatureTable.deleteFeatureAsync(mSelectedArcGISFeature);
                            mapViewResult.addDoneListener(new Runnable() {
                                @Override
                                public void run() {
                                    // apply change to the server
                                    final ListenableFuture<List<FeatureEditResult>> serverResult = mServiceFeatureTable.applyEditsAsync();
                                    serverResult.addDoneListener(new Runnable() {
                                        @Override
                                        public void run() {
                                            List<FeatureEditResult> edits = null;
                                            try {
                                                edits = serverResult.get();
                                                if (edits.size() > 0) {
                                                    if (!edits.get(0).hasCompletedWithErrors()) {
                                                        Log.e("", "Feature successfully updated");
                                                    }
                                                }
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            } catch (ExecutionException e) {
                                                e.printStackTrace();
                                            }

                                        }
                                    });
                                }
                            });

                        } catch (Exception e) {
                            Log.e(mMainActivity.getResources().getString(R.string.app_name), "deteting feature in the feature table failed: " + e.getMessage());
                        }
                    }
                });
                if (mCallout != null) mCallout.dismiss();
            }
        }).setNegativeButton("Kh??ng", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();


    }


    public void updateAttachment() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPath());
        File photo = ImageFile.getFile(mMainActivity);
//        this.mUri= FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".my.package.name.provider", photo);
        this.mUri = Uri.fromFile(photo);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, this.mUri);
        mMainActivity.setSelectedArcGISFeature(mSelectedArcGISFeature);
        mMainActivity.setFeatureViewMoreInfoAdapter(mFeatureViewMoreInfoAdapter);
        mMainActivity.setUri(mUri);
        mMainActivity.startActivityForResult(cameraIntent, mMainActivity.getResources().getInteger(R.integer.REQUEST_ID_UPDATE_ATTACHMENT));
    }

}

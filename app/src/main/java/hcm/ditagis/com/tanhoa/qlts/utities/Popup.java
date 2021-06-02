package hcm.ditagis.com.tanhoa.qlts.utities;

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
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureType;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import hcm.ditagis.com.tanhoa.qlts.QuanLyTaiSan;
import hcm.ditagis.com.tanhoa.qlts.R;
import hcm.ditagis.com.tanhoa.qlts.adapter.FeatureViewInfoAdapter;
import hcm.ditagis.com.tanhoa.qlts.adapter.FeatureViewMoreInfoAdapter;
import hcm.ditagis.com.tanhoa.qlts.async.EditAsync;
import hcm.ditagis.com.tanhoa.qlts.async.NotifyDataSetChangeAsync;
import hcm.ditagis.com.tanhoa.qlts.async.ViewAttachmentAsync;
import hcm.ditagis.com.tanhoa.qlts.libs.FeatureLayerDTG;

public class Popup extends AppCompatActivity {
    private QuanLyTaiSan mMainActivity;
    private ArcGISFeature mSelectedArcGISFeature = null;
    private ServiceFeatureTable mServiceFeatureTable;
    private Callout mCallout;
    private FeatureLayerDTG mFeatureLayerDTG;
    private List<String> lstFeatureType;
    private LinearLayout linearLayout;
    private Uri mUri;
    private static final int REQUEST_ID_IMAGE_CAPTURE = 44;
    private FeatureViewMoreInfoAdapter mFeatureViewMoreInfoAdapter;
    private DialogInterface mDialog;
    private static double DELTA_MOVE_Y = 0;//7000;
    private MapView mMapView;

    public DialogInterface getDialog() {
        return mDialog;
    }

    public Popup(QuanLyTaiSan mainActivity, MapView mMapView, Callout callout) {
        this.mMainActivity = mainActivity;
        this.mMapView = mMapView;
        this.mCallout = callout;

    }

    public Popup(QuanLyTaiSan mainActivity, MapView mMapView, ServiceFeatureTable mServiceFeatureTable, Callout callout) {
        this.mMainActivity = mainActivity;
        this.mMapView = mMapView;
        this.mServiceFeatureTable = mServiceFeatureTable;
        this.mCallout = callout;

    }


    public void setFeatureLayerDTG(FeatureLayerDTG layerDTG) {
        this.mFeatureLayerDTG = layerDTG;
    }

    private void refressPopup() {
        String[] hiddenFields = mMainActivity.getResources().getStringArray(R.array.hiddenFields);
        Map<String, Object> attributes = mSelectedArcGISFeature.getAttributes();
        ListView listView = linearLayout.findViewById(R.id.lstview_thongtinsuco);
        FeatureViewInfoAdapter featureViewInfoAdapter = new FeatureViewInfoAdapter(mMainActivity, new ArrayList<FeatureViewInfoAdapter.Item>());
        listView.setAdapter(featureViewInfoAdapter);
        String typeIdField = mSelectedArcGISFeature.getFeatureTable().getTypeIdField();
        for (Field field : this.mSelectedArcGISFeature.getFeatureTable().getFields()) {
            boolean checkHiddenField = false;
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
                if (item.getFieldName().equals(typeIdField)) {
                    List<FeatureType> featureTypes = mSelectedArcGISFeature.getFeatureTable().getFeatureTypes();
                    Object valueFeatureType = getValueFeatureType(featureTypes, value.toString());
                    if (valueFeatureType != null) item.setValue(valueFeatureType.toString());
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
                    case GLOBALID:
                        item.setValue(value.toString());
                        break;
                    case SHORT:
                    case DOUBLE:
                    case INTEGER:
                    case FLOAT:
                        item.setValue(value.toString());

                        break;
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
        layout.findViewById(R.id.layout_viewmoreinfo_id_su_co).setVisibility(View.VISIBLE);
        layout.findViewById(R.id.framelayout_viewmoreinfo_attachment).setVisibility(View.VISIBLE);
        layout.findViewById(R.id.framelayout_viewmoreinfo_attachment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewAttachment();
            }
        });

        lstViewInfo.setAdapter(mFeatureViewMoreInfoAdapter);
        lstViewInfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                edit(parent, view, position, id);
            }
        });

        String[] updateFields = mFeatureLayerDTG.getUpdateFields();
        String typeIdField = mSelectedArcGISFeature.getFeatureTable().getTypeIdField();
        for (Field field : this.mSelectedArcGISFeature.getFeatureTable().getFields()) {
            Object value = attr.get(field.getName());
            if (field.getName().equals(Constant.IDSU_CO)) {
                if (value != null)
                    ((TextView) layout.findViewById(R.id.txt_alertdialog_id_su_co)).setText(value.toString());
            } else {
                FeatureViewMoreInfoAdapter.Item item = new FeatureViewMoreInfoAdapter.Item();
                item.setAlias(field.getAlias());
                item.setFieldName(field.getName());
                if (value != null) {
                    if (item.getFieldName().equals(typeIdField)) {
                        List<FeatureType> featureTypes = mSelectedArcGISFeature.getFeatureTable().getFeatureTypes();
                        String valueFeatureType = getValueFeatureType(featureTypes, value.toString()).toString();
                        if (valueFeatureType != null) item.setValue(valueFeatureType);
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
                item.setEdit(false);
                if (updateFields.length > 0) {
                    for (String updateField : updateFields) {
                        if (item.getFieldName().equals(updateField)) {
                            item.setEdit(true);
                            break;
                        }
                    }
                }
                item.setFieldType(field.getFieldType());
                mFeatureViewMoreInfoAdapter.add(item);
                mFeatureViewMoreInfoAdapter.notifyDataSetChanged();
            }
        }

        builder.setView(layout);
        builder.setCancelable(false);
        builder.setPositiveButton("Thoát", new DialogInterface.OnClickListener()

        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setNegativeButton("Cập nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditAsync editAsync = new EditAsync(mMainActivity, mServiceFeatureTable, mSelectedArcGISFeature);
                editAsync.execute(mFeatureViewMoreInfoAdapter);
                refressPopup();
            }
        });

//        builder.setPositiveButton(mMainActivity.getString(R.string.btn_TakePicture), null);
//        builder.setNegativeButton(mMainActivity.getString(R.string.btn_Update), null);
//        builder.setNeutralButton(mMainActivity.getString(R.string.btn_Esc), null);
        final AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
////        PICTURE
//        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                capture();
//            }
//        });
////        UPDATE
//        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                EditAsync editAsync = new EditAsync(mMainActivity, mServiceFeatureTable, mSelectedArcGISFeature);
//                editAsync.execute(mFeatureViewMoreInfoAdapter);
//                refressPopup();
//            }
//        });
////        ESC
//        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
        dialog.show();


    }

    private void viewAttachment() {
        ViewAttachmentAsync viewAttachmentAsync = new ViewAttachmentAsync(mMainActivity, mSelectedArcGISFeature);
        viewAttachmentAsync.execute();
//        get attachment
//        final AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
//        LayoutInflater layoutInflater = LayoutInflater.from(mMainActivity);
//        final View layout = layoutInflater.inflate(R.layout.layout_viewmoreinfo_feature_attachment, null);
//        ListView lstViewAttachment = layout.findViewById(R.id.lstView_alertdialog_attachments);
//
//        final FeatureViewMoreInfoAttachmentsAdapter attachmentsAdapter = new FeatureViewMoreInfoAttachmentsAdapter(mMainActivity, new ArrayList<FeatureViewMoreInfoAttachmentsAdapter.Item>());
//        lstViewAttachment.setAdapter(attachmentsAdapter);
//        final ListenableFuture<List<Attachment>> attachmentResults = mSelectedArcGISFeature.fetchAttachmentsAsync();
//        attachmentResults.addDoneListener(new Runnable() {
//            @Override
//            public void run() {
//                try {
//
//                    final List<Attachment> attachments = attachmentResults.get();
//                    int size = attachments.size();
//                    // if selected feature has attachments, display them in a list fashion
//                    if (!attachments.isEmpty()) {
//                        //
//                        for (final Attachment attachment : attachments) {
//                            if (attachment.getContentType().toLowerCase().trim().contains("png")) {
//                                final FeatureViewMoreInfoAttachmentsAdapter.Item item = new FeatureViewMoreInfoAttachmentsAdapter.Item();
//                                item.setName(attachment.getName());
//                                final ListenableFuture<InputStream> inputStreamListenableFuture = attachment.fetchDataAsync();
//                                inputStreamListenableFuture.addDoneListener(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        try {
//                                            InputStream inputStream = inputStreamListenableFuture.get();
//                                            item.setImg(IOUtils.toByteArray(inputStream));
//                                            attachmentsAdapter.add(item);
//                                            attachmentsAdapter.notifyDataSetChanged();
//                                            if (attachmentsAdapter.getCount() > 0 && attachments.lastIndexOf(attachment) == attachments.size() - 1) {
//                                                builder.setView(layout);
//                                                builder.setCancelable(false);
//                                                builder.setPositiveButton("Thoát", new DialogInterface.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(DialogInterface dialog, int which) {
//                                                        dialog.dismiss();
//                                                    }
//                                                });
//                                                AlertDialog dialog = builder.create();
//                                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//
//                                                dialog.show();
//                                            } else {
//                                                Toast.makeText(mMainActivity, "Không có file hình ảnh đính kèm", Toast.LENGTH_LONG).show();
//                                            }
//                                        } catch (InterruptedException e) {
//                                            e.printStackTrace();
//                                        } catch (ExecutionException e) {
//                                            e.printStackTrace();
//                                        } catch (IOException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                });
//
//                            }
//                        }
//
//                    } else {
//                        size--;
////                        MySnackBar.make(mCallout, "Không có file hình ảnh đính kèm", true);
//                    }
//
//                } catch (Exception e) {
//                    Log.e("ERROR", e.getMessage());
//                }
//            }
//        });


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
            if (featureType.getId().toString().equals(code)) {
                value = featureType.getName();
                break;
            }
        }
        return value;
    }

    private void edit(final AdapterView<?> parent, View view, int position, long id) {
        if (parent.getItemAtPosition(position) instanceof FeatureViewMoreInfoAdapter.Item) {
            final FeatureViewMoreInfoAdapter.Item item = (FeatureViewMoreInfoAdapter.Item) parent.getItemAtPosition(position);
            if (item.isEdit()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity, android.R.style.Theme_Material_Light_Dialog_Alert);
                builder.setTitle("Cập nhật thuộc tính");
                builder.setMessage(item.getAlias());
                builder.setCancelable(false).setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                final LinearLayout layout = (LinearLayout) mMainActivity.getLayoutInflater().
                        inflate(R.layout.layout_dialog_update_feature_listview, null);

                final FrameLayout layoutTextView = layout.findViewById(R.id.layout_edit_viewmoreinfo_TextView);
                final TextView textView = layout.findViewById(R.id.txt_edit_viewmoreinfo);
                ImageView img_selectTime = (ImageView) layout.findViewById(R.id.img_selectTime);
                final LinearLayout layoutEditText = layout.findViewById(R.id.layout_edit_viewmoreinfo_Editext);
                final EditText editText = layout.findViewById(R.id.etxt_edit_viewmoreinfo);
                final LinearLayout layoutSpin = layout.findViewById(R.id.layout_edit_viewmoreinfo_Spinner);
                final Spinner spin = layout.findViewById(R.id.spin_edit_viewmoreinfo);

                final Domain domain = mSelectedArcGISFeature.getFeatureTable().getField(item.getFieldName()).getDomain();
                if (item.getFieldName().equals(mSelectedArcGISFeature.getFeatureTable().getTypeIdField())) {
                    layoutSpin.setVisibility(View.VISIBLE);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(layout.getContext(), android.R.layout.simple_list_item_1, lstFeatureType);
                    spin.setAdapter(adapter);
                    if (item.getValue() != null)
                        spin.setSelection(lstFeatureType.indexOf(item.getValue()));
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
                                        Calendar calendar = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                                        String s = String.format("%02d_%02d_%d", datePicker.getDayOfMonth(), datePicker.getMonth(), datePicker.getYear());

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
                builder.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (item.getFieldName().equals(mSelectedArcGISFeature.getFeatureTable().getTypeIdField()) || (domain != null)) {
                            item.setValue(spin.getSelectedItem().toString());
                        } else {
                            switch (item.getFieldType()) {
                                case DATE:
                                    item.setValue(textView.getText().toString());
                                    break;
                                case DOUBLE:
                                    try {
                                        double x = Double.parseDouble(editText.getText().toString());
                                        item.setValue(editText.getText().toString());
                                    } catch (Exception e) {
                                        Toast.makeText(mMainActivity, "Số liệu nhập vào không đúng định dạng!!!", Toast.LENGTH_LONG).show();
                                    }
                                    break;
                                case TEXT:
                                    item.setValue(editText.getText().toString());
                                    break;
                                case INTEGER:
                                    try {
                                        int x = Integer.parseInt(editText.getText().toString());
                                        item.setValue(editText.getText().toString());
                                    } catch (Exception e) {
                                        Toast.makeText(mMainActivity, "Số liệu nhập vào không đúng định dạng!!!", Toast.LENGTH_LONG).show();
                                    }
                                    break;
                                case SHORT:
                                    try {
                                        short x = Short.parseShort(editText.getText().toString());
                                        item.setValue(editText.getText().toString());
                                    } catch (Exception e) {
                                        Toast.makeText(mMainActivity, "Số liệu nhập vào không đúng định dạng!!!", Toast.LENGTH_LONG).show();
                                    }
                                    break;
                            }
                        }
                        dialog.dismiss();
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

    public void clearSelection() {
        if (mFeatureLayerDTG != null) {
            FeatureLayer featureLayer = mFeatureLayerDTG.getFeatureLayer();
            featureLayer.clearSelection();
        }
    }

    public void dimissCallout() {
        if (mCallout != null && mCallout.isShowing()) {
            mCallout.dismiss();
        }
    }

    public LinearLayout showPopup(final ArcGISFeature mSelectedArcGISFeature, Boolean clickMap) {
        dimissCallout();
        mServiceFeatureTable = (ServiceFeatureTable) mFeatureLayerDTG.getFeatureLayer().getFeatureTable();
        this.mSelectedArcGISFeature = mSelectedArcGISFeature;
        FeatureLayer featureLayer = mFeatureLayerDTG.getFeatureLayer();
        featureLayer.selectFeature(mSelectedArcGISFeature);
        lstFeatureType = new ArrayList<>();
        for (int i = 0; i < mSelectedArcGISFeature.getFeatureTable().getFeatureTypes().size(); i++) {
            lstFeatureType.add(mSelectedArcGISFeature.getFeatureTable().getFeatureTypes().get(i).getName());
        }
        LayoutInflater inflater = LayoutInflater.from(this.mMainActivity.getApplicationContext());
        linearLayout = (LinearLayout) inflater.inflate(R.layout.layout_popup_infos, null);
        refressPopup();
        ((TextView) linearLayout.findViewById(R.id.txt_title_layer)).setText(mFeatureLayerDTG.getTitleLayer());
        ((ImageButton) linearLayout.findViewById(R.id.imgBtn_ViewMoreInfo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMoreInfo();
            }
        });

        ((ImageButton) linearLayout.findViewById(R.id.imgBtn_delete)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedArcGISFeature.getFeatureTable().getFeatureLayer().clearSelection();
                deleteFeature();
            }
        });
        ((Button) linearLayout.findViewById(R.id.btn_layer_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dimissCallout();
            }
        });
        if (this.mSelectedArcGISFeature.canEditAttachments()) {
            ((ImageButton) linearLayout.findViewById(R.id.imgBtn_takePics)).setVisibility(View.VISIBLE);
            ((ImageButton) linearLayout.findViewById(R.id.imgBtn_takePics)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateAttachment();
                }
            });
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

    private void deleteFeature() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity, android.R.style.Theme_Material_Light_Dialog_Alert);
        builder.setTitle("Xác nhận");
        builder.setMessage("Bạn có chắc chắn xóa sự cố này?");
        builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
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
        }).setNegativeButton("Không", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();


    }

    public void capture() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPath());

        File photo = ImageFile.getFile(mMainActivity);
//        this.mUri= FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".my.package.name.provider", photo);
        this.mUri = Uri.fromFile(photo);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, this.mUri);
        mMainActivity.setSelectedArcGISFeature(mSelectedArcGISFeature);
        mMainActivity.setFeatureViewMoreInfoAdapter(mFeatureViewMoreInfoAdapter);
        mMainActivity.setUri(mUri);
//        this.mUri = Uri.fromFile(photo);
        mMainActivity.startActivityForResult(cameraIntent, REQUEST_ID_IMAGE_CAPTURE);

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

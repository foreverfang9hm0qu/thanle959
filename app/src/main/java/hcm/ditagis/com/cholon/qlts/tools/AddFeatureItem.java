package hcm.ditagis.com.cholon.qlts.tools;

import com.esri.arcgisruntime.geometry.GeometryType;

import java.util.ArrayList;
import java.util.List;

import hcm.ditagis.com.cholon.qlts.MainActivity;
import hcm.ditagis.com.cholon.qlts.R;
import hcm.ditagis.com.cholon.qlts.adapter.FeatureLayerAdapter;
import hcm.ditagis.com.cholon.qlts.libs.FeatureLayerDTG;
public class AddFeatureItem {
    private List<FeatureLayerAdapter.Item> items;
    private List<FeatureLayerDTG> mFeatureLayerDTGS;

    public AddFeatureItem(List<FeatureLayerDTG> mFeatureLayerDTGS, MainActivity quanLyTaiSan) {
        this.mFeatureLayerDTGS = mFeatureLayerDTGS;
        items = new ArrayList<>();
        for (FeatureLayerDTG featureLayerDTG : mFeatureLayerDTGS) {
            GeometryType geometryType = featureLayerDTG.getFeatureLayer().getFeatureTable().getGeometryType();
            if (featureLayerDTG.getAction() != null && featureLayerDTG.getAction().isCreate() && featureLayerDTG.getAction().isView() && geometryType == GeometryType.POINT)
                items.add(new FeatureLayerAdapter.Item(quanLyTaiSan.getString(R.string.type_search_feature_layer), featureLayerDTG.getFeatureLayer().getName(),featureLayerDTG.getFeatureLayer().getId()));
        }

    }

    public List<FeatureLayerAdapter.Item> getItems() {
        return items;
    }

    public void setItems(List<FeatureLayerAdapter.Item> items) {
        this.items = items;
    }
}


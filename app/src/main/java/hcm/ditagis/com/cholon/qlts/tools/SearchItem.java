package hcm.ditagis.com.cholon.qlts.tools;

import java.util.ArrayList;
import java.util.List;

import hcm.ditagis.com.cholon.qlts.MainActivity;
import hcm.ditagis.com.cholon.qlts.R;
import hcm.ditagis.com.cholon.qlts.adapter.FeatureLayerAdapter;
import hcm.ditagis.com.cholon.qlts.libs.FeatureLayerDTG;

/**
 * Created by NGUYEN HONG on 4/26/2018.
 */

public class SearchItem {
    private List<FeatureLayerAdapter.Item> items;
    private List<FeatureLayerDTG> mFeatureLayerDTGS;

    public SearchItem(List<FeatureLayerDTG> mFeatureLayerDTGS, MainActivity quanLyTaiSan) {
        this.mFeatureLayerDTGS = mFeatureLayerDTGS;
        items = new ArrayList<>();
        for (FeatureLayerDTG featureLayerDTG : mFeatureLayerDTGS) {
            if (featureLayerDTG.getAction() != null && featureLayerDTG.getAction().isView())
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

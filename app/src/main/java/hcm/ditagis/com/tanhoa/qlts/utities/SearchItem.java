package hcm.ditagis.com.tanhoa.qlts.utities;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import hcm.ditagis.com.tanhoa.qlts.QuanLySuCo;
import hcm.ditagis.com.tanhoa.qlts.R;
import hcm.ditagis.com.tanhoa.qlts.adapter.SearchAdapter;
import hcm.ditagis.com.tanhoa.qlts.libs.FeatureLayerDTG;

/**
 * Created by NGUYEN HONG on 4/26/2018.
 */

public class SearchItem {
    private List<SearchAdapter.Item> items;
    private List<FeatureLayerDTG> mFeatureLayerDTGS;

    public SearchItem(List<FeatureLayerDTG> mFeatureLayerDTGS, QuanLySuCo quanLySuCo) {
        this.mFeatureLayerDTGS = mFeatureLayerDTGS;
        items = new ArrayList<>();
        items.add(new SearchAdapter.Item(quanLySuCo.getString(R.string.type_search_address),quanLySuCo.getString(R.string.nav_find_route)));
        for(FeatureLayerDTG featureLayerDTG: mFeatureLayerDTGS){
            items.add(new SearchAdapter.Item(quanLySuCo.getString(R.string.type_search_feature_layer),featureLayerDTG.getTitleLayer()));
        }
        
    }
    public List<SearchAdapter.Item> getItems() {
        return items;
    }

    public void setItems(List<SearchAdapter.Item> items) {
        this.items = items;
    }
}
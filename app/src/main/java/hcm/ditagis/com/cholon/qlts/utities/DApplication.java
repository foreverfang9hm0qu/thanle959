package hcm.ditagis.com.cholon.qlts.utities;

import android.app.Application;

import com.esri.arcgisruntime.layers.FeatureLayer;

public class DApplication extends Application {
    private FeatureLayer selectedFeatureLayer;

    public FeatureLayer getSelectedFeatureLayer() {
        return selectedFeatureLayer;
    }

    public void setSelectedFeatureLayer(FeatureLayer selectedFeatureLayer) {
        this.selectedFeatureLayer = selectedFeatureLayer;
    }
}

package hcm.ditagis.com.cholon.qlts.utities;

import android.app.Application;

import com.esri.arcgisruntime.layers.FeatureLayer;

import hcm.ditagis.com.cholon.qlts.entities.entitiesDB.User;

public class DApplication extends Application {
    private FeatureLayer selectedFeatureLayer;
    private User user;

    public FeatureLayer getSelectedFeatureLayer() {
        return selectedFeatureLayer;
    }

    public void setSelectedFeatureLayer(FeatureLayer selectedFeatureLayer) {
        this.selectedFeatureLayer = selectedFeatureLayer;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    private boolean checkedVersion;

    public boolean isCheckedVersion() {
        return checkedVersion;
    }

    public void setCheckedVersion(boolean checkedVersion) {
        this.checkedVersion = checkedVersion;
    }
}

package hcm.ditagis.com.tanhoa.qlts.tools;

import java.util.List;

import hcm.ditagis.com.tanhoa.qlts.tools.Route;

/**
 * Created by Mai Thanh Hiep on 4/3/2016.
 */
public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}

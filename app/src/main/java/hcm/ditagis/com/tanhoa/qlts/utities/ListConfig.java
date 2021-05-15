package hcm.ditagis.com.tanhoa.qlts.utities;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import hcm.ditagis.com.tanhoa.qlts.R;

/**
 * Created by ThanLe on 5/8/2018.
 */

public class ListConfig {
    private static Context mContext;
    private static ListConfig instance = null;

    public static ListConfig getInstance(Context context) {
        if (instance == null)
            instance = new ListConfig();
        mContext = context;
        return instance;
    }

    private ListConfig() {
    }

    public List<Config> getConfigs() {
        List<Config> configs = new ArrayList<>();
        configs.add(new Config(mContext.getResources().getString(R.string.URL_TIM_DUONG), mContext.getResources().getString(R.string.TITLE_TIM_DUONG), mContext.getResources().getInteger(R.integer.MIN_SCALE_TIM_DUONG),mContext.getString(R.string.group_base_map)));
        return configs;
    }

    public static class Name {
        public static String name_diemsuco = "DIEMSUCO";
    }
//    }
}

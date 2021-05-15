package hcm.ditagis.com.tanhoa.qlts.utities;

import android.content.Context;

/**
 * Created by NGUYEN HONG on 3/20/2018.
 */

public class Config {
    private String url;
    private String[] queryField;
    private String[] outField;
    private String[] updateField;
    private String titleService;
    private String name;
    private int minScale;
    private Context mContext;
    private static Config instance = null;
    private String groupLayer;


    public static Config getInstance() {
        if (instance == null) instance = new Config();
        return instance;
    }

    private Config() {
    }

    public String[] getUpdateField() {
        return updateField;
    }

    public void setUpdateField(String[] updateField) {
        this.updateField = updateField;
    }

    //    CONSTRUCTOR OF BASEMAP
    public Config(String url, String titleService, int minScale, String groupLayer) {
        this.url = url;
        this.titleService = titleService;
        this.minScale = minScale;
        this.groupLayer = groupLayer;
    }

    public Config(String url, String[] queryField, String[] outField, String titleService, int minScale, String[] updateField) {
        this.url = url;
        this.queryField = queryField;
        this.outField = outField;
        this.updateField = updateField;
        this.titleService = titleService;
        this.minScale = minScale;
    }

    public Config(String url, String[] queryField, String[] outField, String name, String titleService, int minScale, String[] updateField, String groupLayer) {
        this.url = url;
        this.queryField = queryField;
        this.outField = outField;
        this.updateField = updateField;
        this.titleService = titleService;
        this.minScale = minScale;
        this.name = name;
        this.groupLayer = groupLayer;
    }

    public int getMinScale() {
        return minScale;
    }

    public void setMinScale(int minScale) {
        this.minScale = minScale;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String[] getQueryField() {
        return queryField;
    }

    public void setQueryField(String[] queryField) {
        this.queryField = queryField;
    }

    public String[] getOutField() {
        return outField;
    }

    public void setOutField(String[] outField) {
        this.outField = outField;
    }

    public String getTitleService() {
        return titleService;
    }

    public void setTitleService(String titleService) {
        this.titleService = titleService;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupLayer() {
        return groupLayer;
    }

    public void setGroupLayer(String groupLayer) {
        this.groupLayer = groupLayer;
    }
}

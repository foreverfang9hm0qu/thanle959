package hcm.ditagis.com.cholon.qlts.utities;

import java.text.SimpleDateFormat;

import hcm.ditagis.com.cholon.qlts.adapter.SettingsAdapter;

/**
 * Created by ThanLe on 3/1/2018.
 */

public class Constant {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private SettingsAdapter.Item[] mSettingsItems;
    public static final String OBJECTID = "OBJECTID";
    public static final String IDSU_CO = "IDSuCo";
            public static final String SERVER = "https://gis.capnuoccholon.com.vn/cholon";
//    public static final String SERVER = "http://sawagis.vn/cholon";
    public static final String SERVER_API = SERVER + "/api";
    public static String API_LOGIN;

    {
        API_LOGIN = SERVER_API + "/Login";
    }

    public String DISPLAY_NAME;

    {
        DISPLAY_NAME = SERVER_API + "/Account/Profile";
    }

    public String LAYER_INFO;

    {
        LAYER_INFO = SERVER_API + "/layerinfo";
    }

    public String IS_ACCESS;

    {
        IS_ACCESS = SERVER_API + "/Account/IsAccess/m_qlts";
    }

    public static final String NAME_DIEMSUCO = "DIEMSUCO";

    private static Constant mInstance = null;

    public static Constant getInstance() {
        if (mInstance == null)
            mInstance = new Constant();
        return mInstance;
    }

    private Constant() {
        mSettingsItems = new SettingsAdapter.Item[]{
                new SettingsAdapter.Item("Phương thức thêm điểm sự cố", ""),
                new SettingsAdapter.Item("Tùy chọn tìm kiếm", ""),
                new SettingsAdapter.Item("Bố cục giao diện", ""),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
        };
    }

    public class URL_API {
        public static final String CHECK_VERSION = SERVER + "/versioning/QLTS?version=%s";
        public static final String ADD_FEATURE = SERVER_API + "/QuanLySuCo/TiepNhanSuCo/%s";
        public static final String LOGIN = SERVER_API + "/Login";
        public static final String PROFILE = SERVER_API + "/Account/Profile";
        public static final String GENERATE_ID_SUCO = SERVER_API + "/QuanLySuCo/GenerateIDSuCo";
        public static final String LAYER_INFO = SERVER_API + "/Account/layerinfo";
        public static final String CHANGE_PASSWORD = SERVER_API + "/Account/changepass";
        public static final String COMPLETE = SERVER_API + "/quanlysuco/xacnhanhoanthanhnhanvien?id=%s";
        public static final String IS_ACCESS = SERVER_API + "/Account/IsAccess/m_qlsc";
        public static final String GENERATE_ID_SUCOTHONGTIN = SERVER_API + "/QuanLySuCo/GenerateIDSuCoThongTin/";


    }

    public SettingsAdapter.Item[] getSettingsItems() {
        return mSettingsItems;
    }


}

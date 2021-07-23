package hcm.ditagis.com.cholon.qlts.utities;

import java.text.SimpleDateFormat;

import hcm.ditagis.com.cholon.qlts.adapter.SettingsAdapter;

/**
 * Created by ThanLe on 3/1/2018.
 */

public class Constant {
    public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:SS");
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private SettingsAdapter.Item[] mSettingsItems;
    public static final String OBJECTID = "OBJECTID";
    public static final String IDSU_CO = "IDSuCo";
    public static final String VI_TRI = "ViTri";
    public static final String TRANG_THAI = "TrangThai";
    public static final String NGAY_CAP_NHAT = "NgayCapNhat";
    public static final String NGAY_THONG_BAO = "NgayThongBao";
        public static final String SERVER_API = "http://gis.capnuoccholon.com.vn/cholon/api";
//    private final String SERVER_API = "http://sawagis.vn/cholon/api";
    public String API_LOGIN;

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

    public SettingsAdapter.Item[] getSettingsItems() {
        return mSettingsItems;
    }


}

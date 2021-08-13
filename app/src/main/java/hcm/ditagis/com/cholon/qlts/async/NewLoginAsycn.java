package hcm.ditagis.com.cholon.qlts.async;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import hcm.ditagis.com.cholon.qlts.R;
import hcm.ditagis.com.cholon.qlts.entities.entitiesDB.User;
import hcm.ditagis.com.cholon.qlts.entities.entitiesDB.UserDangNhap;
import hcm.ditagis.com.cholon.qlts.utities.Constant;
import hcm.ditagis.com.cholon.qlts.utities.DApplication;
import hcm.ditagis.com.cholon.qlts.utities.Preference;

public class NewLoginAsycn extends AsyncTask<String, Void, Void> {
    private Exception exception;
    private ProgressDialog mDialog;
    private Activity mActivity;
    private AsyncResponse mDelegate;
    private DApplication mDApplication;
    public interface AsyncResponse {
        void processFinish(Void output);
    }

    public NewLoginAsycn(Activity activity, AsyncResponse delegate) {
        this.mActivity = activity;
        this.mDApplication = (DApplication) activity.getApplication();
        this.mDelegate = delegate;
    }

    protected void onPreExecute() {
        super.onPreExecute();
        this.mDialog = new ProgressDialog(this.mActivity, android.R.style.Theme_Material_Dialog_Alert);
        this.mDialog.setMessage(mActivity.getString(R.string.connect_message));
        this.mDialog.setCancelable(false);
        this.mDialog.show();
    }

    @Override
    protected Void doInBackground(String... params) {
        String userName = params[0];
        String pin = params[1];
//        String passEncoded = (new EncodeMD5()).encode(pin + "_DITAGIS");
        // Do some validation here
        String urlParameters = String.format("Username=%s&Password=%s", userName, pin);
        String urlWithParam = String.format("%s?%s", Constant.getInstance().API_LOGIN, urlParameters);
        try {
//            + "&apiKey=" + API_KEY
            URL url = new URL(urlWithParam);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setRequestMethod("GET");
                conn.connect();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                    break;
                }
                Preference.getInstance().savePreferences(mActivity.getString(R.string.preference_login_api), stringBuilder.toString().replace("\"", ""));
                bufferedReader.close();
                User user = new User();
                user.setDisplayName(getDisplayName());
                user.setUserName(userName);
                this.mDApplication.setUser(user);
                return null;


            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Void user) {
//        if (user != null) {
        mDialog.dismiss();
        this.mDelegate.processFinish(user);
//        }
    }

    private String getDisplayName() {
        String API_URL = mActivity.getString(R.string.URL_API) + "/api/Account/Profile";
        String displayName = "";
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setDoOutput(false);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", Preference.getInstance().loadPreference(mActivity.getString(R.string.preference_login_api)));
                conn.connect();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    displayName = pajsonRouteeJSon(line);

                    break;
                }

            } catch (Exception e) {
                Log.e("error", e.toString());
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            Log.e("error", e.toString());
        } finally {
            return displayName;
        }
    }

    private String pajsonRouteeJSon(String data) throws JSONException {
        if (data == null)
            return "";
        String displayName = "";
        String myData = "{ \"account\": [".concat(data).concat("]}");
        JSONObject jsonData = new JSONObject(myData);
        JSONArray jsonRoutes = jsonData.getJSONArray("account");
//        jsonData.getJSONArray("account");
        for (int i = 0; i < jsonRoutes.length(); i++) {
            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            displayName = jsonRoute.getString(mActivity.getString(R.string.sql_coloumn_login_displayname));
        }
        return displayName;

    }
}

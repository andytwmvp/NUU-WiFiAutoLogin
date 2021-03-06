package tw.edu.nuu.wifiautologin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import tw.edu.nuu.network.NetworkStatus;
import tw.edu.nuu.network.WifiControl;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
//import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
//import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import com.google.analytics.tracking.android.EasyTracker;

public class WifiAutoLogin extends Activity {
    public static final String PREF = "ACCOUNT_PREF";
    public static final String PREF_USERNAME = "USERNAME";
    public static final String PREF_PWD = "PASSWORD";
    private EditText usernameEditText;
    private EditText passwordEditText;
//  private CheckBox showPasswordCheckBox;

    private NetworkStatus networkStatus;
    private WifiControl wifiControl;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_auto_login);

        usernameEditText = (EditText) findViewById(R.id.et_username);
        passwordEditText = (EditText) findViewById(R.id.et_password);
//      showPasswordCheckBox = (CheckBox) findViewById(R.id.cb_showpwd);

        networkStatus = new NetworkStatus(this);
        wifiControl = new WifiControl(this);
        
        restorePrefs();
    }
/*
    public void showPasswordCheckBoxOnclick(View view) {
        if (showPasswordCheckBox.isChecked()) {
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
        else {
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }
*/
    public void enableBthOnclick(View view) {
        if(!wifiControl.wifiStatus());
        wifiControl.addNetwork(wifiControl.createWifiInfo(getString(R.string.ssid)));
        wifiControl.connectWifi(getString(R.string.ssid));
        Toast.makeText(this, getString(R.string.enb_wifi_msg), Toast.LENGTH_LONG).show();
    }
    public void disableBthOnclick(View view) {
        wifiControl.closeWifi();
        Toast.makeText(this, getString(R.string.err_ssid_msg), Toast.LENGTH_LONG).show();
    }
    public void loginBthOnclick(View view) {
        final ProgressDialog progressDialog;
        progressDialog = ProgressDialog.show(this, "Log In", "Please wait...");
        new Thread() {
            @Override
            public void run() {
                Resources resources = getResources();
                
                String url = getString(R.string.url);
                String[] params_name = resources.getStringArray(R.array.params_name);
                String[] params_value = resources.getStringArray(R.array.params_value);
                params_value[0] = usernameEditText.getText().toString();
                params_value[1] = passwordEditText.getText().toString();             
                
                if (params_name.length != params_value.length)
                    Log.e(getString(R.string.app_name), "Config file error!");

                List<BasicNameValuePair> parms = new LinkedList<BasicNameValuePair>();
                for (int i = 0; i < params_name.length; i++) {
                    Log.d("asd", params_name[i] + " " + params_value[i]);
                    parms.add(new BasicNameValuePair(params_name[i], params_value[i]));
                }

                try {
                    HttpPost httpPost = new HttpPost(url);
                    httpPost.setEntity(new UrlEncodedFormEntity(parms, "UTF-8"));
                    new DefaultHttpClient().execute(httpPost);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                progressDialog.dismiss();
                finish();
            };
        }.start();
    }
    private void restorePrefs() {
        SharedPreferences setting = getSharedPreferences(PREF, 0);
        String username = setting.getString(PREF_USERNAME, "");
        String password = setting.getString(PREF_PWD, "");

        usernameEditText.setText(username);
        passwordEditText.setText(password);
    }
    private void savePrefs() {
        SharedPreferences setting = getSharedPreferences(PREF, 0);
        setting.edit()
                .putString(PREF_USERNAME, usernameEditText.getText().toString())
                .putString(PREF_PWD, passwordEditText.getText().toString())
                .commit();
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (networkStatus.getSSID().compareTo(getString(R.string.ssid)) != 0) {
            Toast.makeText(this, getString(R.string.err_ssid_msg), Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        savePrefs();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
     // TODO Auto-generated method stub
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_nuu_wifi_auto_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
     // TODO Auto-generated method stub
        switch(item.getItemId()){
            case (R.id.about):
                ShowMsgDialog();
        }
        return true;
    }
    
    private void ShowMsgDialog()
    {
        final TextView message = new TextView(this);
        final SpannableString info = new SpannableString(this.getText(R.string.about_info));
        
        Linkify.addLinks(info, Linkify.WEB_URLS);
        message.setText(info);
        message.setMovementMethod(LinkMovementMethod.getInstance());

        Builder aboutAlertDialog = new AlertDialog.Builder(this);
        
        aboutAlertDialog.setTitle(getString(R.string.app_name))
                        .setView(message)
                        .setNeutralButton("Close", null)
                        .show();
    }
    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

}

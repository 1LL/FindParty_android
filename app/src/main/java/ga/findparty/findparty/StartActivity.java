package ga.findparty.findparty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.facebook.FacebookException;
import com.facebook.Profile;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;
import com.squareup.picasso.Picasso;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import ga.findparty.findparty.profile.WtInfoActivity;
import ga.findparty.findparty.util.AdditionalFunc;
import ga.findparty.findparty.util.FacebookLogin;
import ga.findparty.findparty.util.FacebookLoginSupport;
import ga.findparty.findparty.util.NaverLogin;
import ga.findparty.findparty.util.NaverLoginSupport;
import ga.findparty.findparty.util.ParsePHP;

public class StartActivity extends AppCompatActivity implements FacebookLoginSupport, NaverLoginSupport {

    public static final String FACEBOOK_LOGIN = "facebook";
    public static final String NAVER_LOGIN = "naver";

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_SHOW_LOGIN = 500;
    private final int MSG_MESSAGE_SUCCESS = 501;
    private final int MSG_MESSAGE_FAIL_FB = 502;
    private final int MSG_MESSAGE_FAIL_NAVER = 503;
    private final int MSG_MESSAGE_FACEBOOK_EMPTY = 505;
    private final int MSG_MESSAGE_NAVER_EMPTY = 506;

    private SharedPreferences setting;
    private SharedPreferences.Editor editor;

    // Facebook
    private FacebookLogin facebookLogin;
    private ImageView fbLogin;

    // Naver
    private NaverLogin naverLogin;
    private OAuthLoginButton mOAuthLoginButton;

    // UI
    private KenBurnsView kenBurnsView;
    private RelativeLayout rl_background;

    public static String USER_ID = "";
    public static HashMap<String, Object> USER_DATA = new HashMap<>();

    // ToWtInfo
    private String wt_id;
    private String wt_email;
    private String wt_img;
    private String wt_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        facebookLogin = new FacebookLogin(this, this);
        setContentView(R.layout.activity_start);

        setting = getSharedPreferences("setting", 0);
        editor = setting.edit();

        init();

        checkAlreadyLogin();

    }

    private void init(){

        kenBurnsView = (KenBurnsView)findViewById(R.id.image);
        kenBurnsView.setImageResource(R.drawable.loading);
//        Picasso.with(getApplicationContext())
//                .load(R.drawable.loading)
//                .into(kenBurnsView);

        rl_background = (RelativeLayout) findViewById(R.id.rl_background);

        fbLogin = (ImageView)findViewById(R.id.fb_login);
        fbLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLogin.login();
            }
        });

        mOAuthLoginButton = (OAuthLoginButton) findViewById(R.id.naver_login);
        naverLogin = new NaverLogin(this, mOAuthLoginButton, this);
        mOAuthLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                naverLogin.login();
            }
        });

        findViewById(R.id.fb_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLogin.logout();
            }
        });
        findViewById(R.id.naver_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                naverLogin.deleteToken();
            }
        });

    }

    private void checkAlreadyLogin(){

        String login = setting.getString("login", null);

        if(null != login){

            if (FACEBOOK_LOGIN.equals(login)) {

                if(facebookLogin.isAlreadyLogin()){

                    USER_ID = facebookLogin.getID();
                    checkLogin(MSG_MESSAGE_SUCCESS, MSG_MESSAGE_FAIL_FB);

                    return;

                }

            } else if (NAVER_LOGIN.equals(login)) {

                HashMap<String, String> data = naverLogin.getUserInformation();
                if(!data.isEmpty()){

                    USER_ID = data.get("id");
                    checkLogin(MSG_MESSAGE_SUCCESS, MSG_MESSAGE_FAIL_NAVER);

                    return;

                }

            }

        }

        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SHOW_LOGIN));

    }

    private void checkLogin(final int success, final int fail) {
        HashMap<String, String> map = new HashMap<>();
        map.put("service", "getUserInfo");
        map.put("id", USER_ID);
        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){
            @Override
            protected void afterThreadFinish(String data) {
                USER_DATA = AdditionalFunc.getUserInfo(data);
                if(USER_DATA.isEmpty()){
                    handler.sendMessage(handler.obtainMessage(fail));
                }else{
                    handler.sendMessage(handler.obtainMessage(success));
                }
            }
        }.start();
    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_SHOW_LOGIN:
//                    li_login.setVisibility(View.VISIBLE);
                    rl_background.setVisibility(View.VISIBLE);
                    break;
                case MSG_MESSAGE_SUCCESS:
                    redirectMainActivity();
                    break;
                case MSG_MESSAGE_FAIL_FB:
                    facebookLogin.login();
                    break;
                case MSG_MESSAGE_FAIL_NAVER:
                    naverLogin.login();
                    break;
                case MSG_MESSAGE_FACEBOOK_EMPTY:
                    redirectWtInfoActivity();
                    break;
                case MSG_MESSAGE_NAVER_EMPTY:
                    redirectWtInfoActivity();
                    break;
                default:
                    break;
            }
        }
    }

    // =================== Login Method ============================

    // ================== Facebook ==================
    @Override
    public void afterFBLoginSuccess(Profile profile, HashMap<String, String> data) {
        // System.out.println(profile.getId() + ", " + profile.getName());
        editor.putString("login", FACEBOOK_LOGIN);
        editor.commit();

        USER_ID = profile.getId();

        wt_id = profile.getId();
        wt_img = profile.getProfilePictureUri(500, 500).toString();
        wt_email = data.get("email");
        wt_name = profile.getName();

        checkLogin(MSG_MESSAGE_SUCCESS, MSG_MESSAGE_FACEBOOK_EMPTY);
    }

    @Override
    public void afterFBLoginCancel() {
        showSnackbar("Facebook Login Cancel");
    }

    @Override
    public void afterFBLoginError(FacebookException error) {
        showSnackbar("Facebook Login Error : " + error.getCause().toString());
    }

    @Override
    public void afterFBLogout() {
        showSnackbar("Facebook Logout");
    }


    // ================== Naver ==================
    @Override
    public void afterNaverLoginSuccess(HashMap<String, String> data) {
        editor.putString("login", NAVER_LOGIN);
        editor.commit();

        USER_ID = data.get("id");

        wt_id = data.get("id");
        wt_img = data.get("img");
        wt_email = data.get("email");
        wt_name = data.get("name");

        checkLogin(MSG_MESSAGE_SUCCESS, MSG_MESSAGE_NAVER_EMPTY);
    }

    @Override
    public void afterNaverLoginFail(String errorCode, String errorDesc) {
        showSnackbar("Naver Login Error : " + errorCode + ", " + errorDesc);
    }

    public void redirectWtInfoActivity() {
        Intent intent = new Intent(this, WtInfoActivity.class);
        intent.putExtra("id", wt_id);
        intent.putExtra("img", wt_img);
        intent.putExtra("email", wt_email);
        intent.putExtra("name", wt_name);
        startActivity(intent);
        finish();
    }

    public void redirectMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookLogin.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case 0:
                break;
            default:
                break;
        }
    }


    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

    private void printKeyHash(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "tk.twpooi.tuetue",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
//                showSnackbar(Base64.encodeToString(md.digest(), Base64.DEFAULT));
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }
}

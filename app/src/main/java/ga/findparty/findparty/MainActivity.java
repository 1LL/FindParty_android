package ga.findparty.findparty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import ga.findparty.findparty.util.FacebookLogin;
import ga.findparty.findparty.util.NaverLogin;
import ga.findparty.findparty.util.ParsePHP;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_FINISH = 500;

    private NavigationView navigationView;

    private String[] menuList = {"nav_show_my_team", "nav_info", "nav_report", "nav_help", "nav_open_source"};

    // Logout
    private MaterialDialog progressDialog;
    private FacebookLogin facebookLogin;
    private NaverLogin naverLogin;
    private SharedPreferences setting;
    private SharedPreferences.Editor editor;
    private String login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        facebookLogin = new FacebookLogin(this);
        setContentView(R.layout.activity_main);

        setting = getSharedPreferences("setting", 0);
        editor = setting.edit();
        login = setting.getString("login", null);

        naverLogin = new NaverLogin(this, new OAuthLoginButton(this));

        progressDialog = new com.afollestad.materialdialogs.MaterialDialog.Builder(this)
                .content("잠시만 기다려주세요.")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .theme(Theme.LIGHT)
                .build();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        String name = (String)StartActivity.USER_DATA.get("name") + "님";
        String email = (String)StartActivity.USER_DATA.get("email");
        String img = (String)StartActivity.USER_DATA.get("img");

        View headerView = navigationView.getHeaderView(0);
        ImageView profileImg = (ImageView)headerView.findViewById(R.id.profileImg);
        Picasso.with(this)
                .load(img)
                .transform(new CropCircleTransformation())
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(profileImg);

        TextView tv_name = (TextView)headerView.findViewById(R.id.tv_name);
        tv_name.setText(name);
        TextView tv_email = (TextView)headerView.findViewById(R.id.tv_email);
        tv_email.setText(email);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        String title = null;

        if(id == R.id.nav_my_team){

            showSnackbar("내팀");

        } else if (id == R.id.nav_show_profile) {

            showSnackbar("프로필 보기");

        } else if(id == R.id.nav_logout){

            if (StartActivity.FACEBOOK_LOGIN.equals(login)) {
                facebookLogin.logout();
            } else if (StartActivity.NAVER_LOGIN.equals(login)) {
                naverLogin.logout();
            }

            editor.remove("login");
            editor.commit();

            redirectStartPage();

        } else if(id == R.id.nav_logout_delete){
            if (StartActivity.FACEBOOK_LOGIN.equals(login)) {

                facebookLogin.logout();
                editor.remove("login");
                editor.commit();
                removeUser(StartActivity.USER_ID);

            } else if (StartActivity.NAVER_LOGIN.equals(login)) {

                naverLogin.deleteToken();
                editor.remove("login");
                editor.commit();
                removeUser(StartActivity.USER_ID);

            }

        } else if(id == R.id.nav_info){

            String text = getResources().getString(R.string.app_name) + " " + getVersion() + "(build " + getVersionCode() + ")";

            showSnackbar(text);
//            final MaterialDialog dialog = new MaterialDialog(MainActivity.this);
//            dialog.content(text)
//                    .title("정보")
//                    .btnNum(1)
//                    .btnText("확인")
//                    .showAnim(new FadeEnter())
//                    .show();
//            dialog.setOnBtnClickL(new OnBtnClickL() {
//                @Override
//                public void onBtnClick() {
//                    dialog.dismiss();
//                }
//            });

        } else if(id == R.id.nav_report){

            String text = getResources().getString(R.string.app_name) + " " + getVersion() + "(build " + getVersionCode() + ") 오류제보";

            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{Information.ADMINISTRATOR_EMAIL});
            i.putExtra(Intent.EXTRA_SUBJECT, text);
            i.putExtra(Intent.EXTRA_TEXT, "내용을 입력해주세요.");
            try {
                startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                showSnackbar("설치된 이메일 클라이언트가 존재하지 않습니다.");
            }

        } else if(id == R.id.nav_help){

            showSnackbar("도움말 페이지");
//            Intent intent = new Intent(getApplicationContext(), IntroduceActivity.class);
//            startActivity(intent);

        } else if(id == R.id.nav_open_source){

            showSnackbar("오픈소스 페이지");
//            Intent intent = new Intent(getApplicationContext(), OpenSourceActivity.class);
//            startActivity(intent);
        }

        if (getSupportActionBar() != null && title != null) {
            getSupportActionBar().setTitle(title);
        }

        navigationView.setCheckedItem(id);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void showFragment(String tag, Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();

        if(fragmentManager.findFragmentByTag(tag) != null){
            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag(tag)).commit();
        }else{
            fragmentManager.beginTransaction().add(R.id.content_fragment_layout, fragment, tag).commit();
        }
        hideFragment(fragmentManager,tag);

    }

    private void hideFragment(FragmentManager fragmentManager, String name){

        for(String s : menuList){

            if(!s.equals(name)) {
                if (fragmentManager.findFragmentByTag(s) != null) {
                    fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(s)).commit();
                }
            }

        }

    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_FINISH:
                    redirectStartPage();
                    break;
                default:
                    break;
            }
        }
    }

    private void removeUser(String userId){
        HashMap<String, String> map = new HashMap<>();
        map.put("service", "deleteUser");
        map.put("id", userId);

        progressDialog.setContent("데이터 삭제 중입니다.");
        progressDialog.show();
        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){
            @Override
            protected void afterThreadFinish(String data) {
                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_FINISH));
            }
        }.start();
    }



    private String getVersionCode() {
        String version = "";
        try {
            PackageInfo i = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            version = Integer.toString(i.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
        }
        return version;
    }

    private String getVersion() {
        String version = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (Exception e) {

        }
        return version;
    }

    private void redirectStartPage(){
        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

}

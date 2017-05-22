package ga.findparty.findparty.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import ga.findparty.findparty.BaseActivity;
import ga.findparty.findparty.Information;
import ga.findparty.findparty.MainActivity;
import ga.findparty.findparty.R;
import ga.findparty.findparty.StartActivity;
import ga.findparty.findparty.util.AdditionalFunc;
import ga.findparty.findparty.util.FacebookLogin;
import ga.findparty.findparty.util.NaverLogin;
import ga.findparty.findparty.util.ParsePHP;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class WtInfoActivity extends BaseActivity {

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_FINISH = 500;
    private final int MSG_MESSAGE_SAVE_USER_PROFILE = 501;
    private final int MSG_MESSAGE_UPDATE_PROFILE_FINISH = 502;
    private final int MSG_MESSAGE_UPDATE_PROFILE = 503;

    private SharedPreferences setting;
    private SharedPreferences.Editor editor;

    // UI
    private ImageView profileImage;
    private TextView profileName;

    private TextView selectSchoolBtn;
    private MaterialEditText editStudentId;
    private MaterialEditText editContact;
    private MaterialEditText editIntro;
    private MaterialEditText editEmail;

    private Button logoutBtn;
    private Button nextBtn;


    private FacebookLogin facebookLogin;
    private NaverLogin naverLogin;

    // UI - Interest Field
    private LinearLayout interestField;
    private TextView interestBtn;

    // User Data
    private String id;
    private String img;
    private String name;
    private String email;
    private String schoolName;
    private String schoolCode;
    private ArrayList<String> interest;

    private MaterialDialog progressDialog;

    private HashMap<String, Object> item;
    private boolean isEditMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        facebookLogin = new FacebookLogin(this);
        setContentView(R.layout.activity_wt_info);

        naverLogin = new NaverLogin(this, new OAuthLoginButton(this));
        setting = getSharedPreferences("setting", 0);
        editor = setting.edit();

        initData();

        init();

        if (isEditMode) {
            progressDialog.setContent("잠시만 기다려주세요.");
            progressDialog.show();

            HashMap<String, String> map = new HashMap<>();
            map.put("service", "getUserInfo");
            map.put("id", id);

            new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {
                @Override
                protected void afterThreadFinish(String data) {
                    item.clear();
                    item = AdditionalFunc.getUserInfo(data);
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_UPDATE_PROFILE));
                }
            }.start();
        }

    }


    private void initData(){

        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("edit", false);
        id = intent.getStringExtra("id");

        if (!isEditMode) {
            img = intent.getStringExtra("img");
            name = intent.getStringExtra("name");
            email = intent.getStringExtra("email");
        }

        interest = new ArrayList<>();
        item = new HashMap<>();

    }

    private void init(){

        profileImage = (ImageView) findViewById(R.id.profileImg);
        profileName = (TextView)findViewById(R.id.profileName);

        selectSchoolBtn = (TextView)findViewById(R.id.btn_school);
        selectSchoolBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEditMode)
                    selectSchool();
            }
        });
        editStudentId = (MaterialEditText)findViewById(R.id.edit_studentId);
        editContact = (MaterialEditText)findViewById(R.id.edit_contact);
        editIntro = (MaterialEditText)findViewById(R.id.edit_intro);
        editEmail = (MaterialEditText)findViewById(R.id.edit_email);

        interestField = (LinearLayout)findViewById(R.id.interest_field);
        interestBtn = (TextView)findViewById(R.id.interest_btn);
        interestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectSelectActivity();
            }
        });
        logoutBtn = (Button)findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        if(isEditMode){
            logoutBtn.setVisibility(View.GONE);
        }
        nextBtn = (Button)findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SAVE_USER_PROFILE));
            }
        });
        if (isEditMode) {
            nextBtn.setText("편집하기");
        }

        setNextButton(false);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkEditText();
            }
        };

        editStudentId.addTextChangedListener(textWatcher);
        editContact.addTextChangedListener(textWatcher);
        editIntro.addTextChangedListener(textWatcher);
        editEmail.addTextChangedListener(textWatcher);

        if (!isEditMode) {
            Picasso.with(getApplicationContext())
                    .load(img)
                    .transform(new CropCircleTransformation())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .into(profileImage);
            profileName.setText(name + "님");

            editEmail.setText(email);
            //editContact.setText(email);
        }

        progressDialog = new MaterialDialog.Builder(this)
                .content("잠시만 기다려주세요.")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .theme(Theme.LIGHT)
                .build();

    }

    private void logout(){
        String login = setting.getString("login", null);
        if (StartActivity.FACEBOOK_LOGIN.equals(login)) {
            facebookLogin.logout();
        } else if (StartActivity.NAVER_LOGIN.equals(login)) {
            naverLogin.logout();
        }

        editor.remove("login");
        editor.commit();

        redirectStartPage();
    }

    private void checkEditText(){

        boolean stuid = editStudentId.isCharactersCountValid();
        boolean contact = editContact.isCharactersCountValid();
        boolean intro = editIntro.isCharactersCountValid();
        boolean em = editEmail.isCharactersCountValid();
        boolean inter = interest.size() >= 3;
        boolean school = schoolCode != null;

        setNextButton(stuid && contact && intro && em && inter && school);

    }

    private void setNextButton(boolean type){

        if(type){
            nextBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            nextBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        }else{
            nextBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
            nextBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.light_gray));
        }

        nextBtn.setEnabled(type);


    }

    private void fillField() {

        img = (String) item.get("img");
        name = (String) item.get("name");
        String contact = (String) item.get("contact");
        String intro = (String) item.get("intro");
        String email = (String) item.get("email");
        this.interest = (ArrayList<String>) item.get("interest");

        schoolCode = (String)item.get("school");
        schoolName = Information.getSchoolName(schoolCode);
        selectSchoolBtn.setText(schoolName);
        selectSchoolBtn.setEnabled(false);

        Picasso.with(getApplicationContext())
                .load(img)
                .transform(new CropCircleTransformation())
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(profileImage);
        profileName.setText(name + "님");

        editStudentId.setText((String)item.get("studentId"));
        editContact.setText(contact);
        editIntro.setText(intro);
        editEmail.setText(email);

        setInterestField();

        checkEditText();

    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_FINISH:
                    StartActivity.USER_SCHOOL = (String)StartActivity.USER_DATA.get("school");
                    progressDialog.hide();
                    redirectMainActivity();
                    break;
                case MSG_MESSAGE_SAVE_USER_PROFILE:
                    saveUserInformation();
                    break;
                case MSG_MESSAGE_UPDATE_PROFILE:
                    progressDialog.hide();
                    fillField();
                    break;
                case MSG_MESSAGE_UPDATE_PROFILE_FINISH:
                    progressDialog.hide();
                    redirectPreviousActivity();
                    break;
                default:
                    break;
            }
        }
    }

    private void selectSchool(){
        new MaterialDialog.Builder(this).title("학교 선택")
                .items(Information.SCHOOL_LIST)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        schoolName = Information.SCHOOL_LIST[position];
                        schoolCode = Information.getSchoolCode(schoolName);
                        selectSchoolBtn.setText(schoolName);
                    }
                })
                .positiveText("취소")
                .theme(Theme.LIGHT)
                .show();
    }

    private void setInterestField(){

        if(interest.size() <= 0){
            interestBtn.setVisibility(View.VISIBLE);
            return;
        }else{
            interestBtn.setVisibility(View.GONE);
        }

        interestField.removeAllViews();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpWidth -= 85;

        final float scale =getResources().getDisplayMetrics().density;
        int width = (int) (dpWidth * scale + 0.5f);
        int dp5 = (int) (5 * scale + 0.5f);
        int dp3 = (int) (3 * scale * 0.5f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(dp3, dp3, dp3, dp3);
        int mButtonsSize = 0;
        Rect bounds = new Rect();

        boolean isAdd = false;
        TextView finalText = new TextView(this);
        finalText.setPadding(dp5*2, dp5, dp5*2, dp5);
        finalText.setBackgroundResource(R.drawable.round_button_blue);
        finalText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        finalText.setText("+" + interest.size());
        finalText.setLayoutParams(params);
        finalText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectSelectActivity();
            }
        });

        for(int i=0; i<interest.size(); i++){

            int remainCount = interest.size() - i;
            String remainString = "+" + remainCount;
            finalText.setText(remainString);

            String s = interest.get(i);
            TextView mBtn = new TextView(this);
            mBtn.setPadding(dp5*2, dp5, dp5*2, dp5);
            mBtn.setBackgroundResource(R.drawable.round_button_blue_line);
            mBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.pastel_blue));
            mBtn.setText(s);
            mBtn.setLayoutParams(params);

            Paint textPaint = mBtn.getPaint();
            textPaint.getTextBounds(s, 0, s.length(), bounds);
            int textWidth = bounds.width() + dp5*4 + dp3*2;

            Rect tempBounds = new Rect();
            textPaint = finalText.getPaint();
            textPaint.getTextBounds(remainString, 0, remainString.length(), tempBounds);
            int remainWidth = tempBounds.width() + dp5*4 + dp3*2;

            if(mButtonsSize + textWidth + remainWidth < width){
                interestField.addView(mBtn);
                mButtonsSize += textWidth;
            }else{
                interestField.addView(finalText);
                isAdd = true;
                break;
            }

        }

        if(!isAdd){
            finalText.setText("+0");
            interestField.addView(finalText);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case 1:
                ArrayList<String> temp = (ArrayList<String>)data.getSerializableExtra("interest");

                boolean check = true;

                if(temp.size() == interest.size()) {
                    for (String s : temp) {
                        if (!interest.contains(s)) {
                            check = false;
                            break;
                        }
                    }
                }else{
                    check = false;
                }

                interest = temp;

                if(!check) {
                    showSnackbar("관심분야가 수정되었습니다.");
                    setInterestField();
                }

                checkEditText();
                break;
            default:
                break;
        }
    }

    private void saveUserInformation(){

        email = editEmail.getText().toString();
        String intro = AdditionalFunc.replaceNewLineString(editIntro.getText().toString());
        String contact = AdditionalFunc.replaceNewLineString(editContact.getText().toString());
        String inter = AdditionalFunc.arrayListToString(interest);

        HashMap<String, String> map = new HashMap<>();
        if (isEditMode) {
            map.put("service", "updateUser");
        } else {
            map.put("service", "saveUser");
        }
        map.put("id", id);
        map.put("school", schoolCode);
        map.put("studentId", editStudentId.getText().toString());
        map.put("name", name);
        map.put("img", img);
        map.put("email", email);
        map.put("contact", contact);
        map.put("interest", inter);
        map.put("intro", intro);

//        progressDialog.show();
        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){
            @Override
            protected void afterThreadFinish(String data) {
                if("1".equals(data)) {
                    editor.putString("userId", id);
                    editor.commit();

                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("service", "getUserInfo");
                    map.put("id", id);
                    new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){
                        @Override
                        protected void afterThreadFinish(String data) {
                            StartActivity.USER_DATA = AdditionalFunc.getUserInfo(data);
                            if (isEditMode) {
                                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_UPDATE_PROFILE_FINISH));
                            } else {
                                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_FINISH));
                            }
                        }
                    }.start();

                }else{
                    showSnackbar("오류!!");
                }
            }
        }.start();

    }

    private void redirectSelectActivity() {
        Intent intent = new Intent(this, SelectInterestActivity.class);
        intent.putStringArrayListExtra("interest", interest);
        startActivityForResult(intent, 0);
    }

    private void redirectMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void redirectPreviousActivity() {
        Intent intent = new Intent();
        intent.putExtra("item", StartActivity.USER_DATA);
        setResult(ProfileActivity.EDIT_PROFILE, intent);
        finish();
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

}

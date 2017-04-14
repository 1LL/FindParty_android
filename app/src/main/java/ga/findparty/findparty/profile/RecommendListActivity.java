package ga.findparty.findparty.profile;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import ga.findparty.findparty.BaseActivity;
import ga.findparty.findparty.Information;
import ga.findparty.findparty.R;
import ga.findparty.findparty.StartActivity;
import ga.findparty.findparty.util.ParsePHP;

public class RecommendListActivity extends BaseActivity {

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;
    private final int MSG_MESSAGE_SHOW_LOADING = 501;
    private final int MSG_MESSAGE_REFRESH_LIST = 502;
    private final int MSG_MESSAGE_ERROR = 503;
    private final int MSG_MESSAGE_SUCCESS_RECOMMEND = 504;
    private final int MSG_MESSAGE_SUCCESS_UNRECOMMEND = 505;

    private FrameLayout root;
    private Button closeBtn;
    private LinearLayout li_recommendField;
    private AVLoadingIndicatorView loading;

    private MaterialDialog progressDialog;

    private String userId;
    //private ArrayList<String> recommendedList;
    private HashMap<String, String> recommendedInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend_list);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        //recommendedList = new ArrayList<>();
        recommendedInfo = new HashMap<>();

        init();

        getRecommendedList();

    }

    private void init(){

        progressDialog = new MaterialDialog.Builder(this)
                .content("잠시만 기다려주세요.")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .theme(Theme.LIGHT)
                .build();

        root = (FrameLayout)findViewById(R.id.root);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecommendListActivity.super.onBackPressed();
            }
        });
        closeBtn = (Button)findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecommendListActivity.super.onBackPressed();
            }
        });

        li_recommendField = (LinearLayout)findViewById(R.id.li_recommend_field);
        loading = (AVLoadingIndicatorView)findViewById(R.id.loading);

    }

    private void makeList(){

        li_recommendField.removeAllViews();

        for(String f : Information.RECOMMEND_FIELD_LIST){

            View v = getLayoutInflater().inflate(R.layout.detail_board_custom_item, null, false);

            RelativeLayout rl_background = (RelativeLayout)v.findViewById(R.id.rl_background);
            TextView tv_title = (TextView)v.findViewById(R.id.tv_title);
            tv_title.setText(f);
            TextView tv_number = (TextView)v.findViewById(R.id.tv_number);
            tv_number.setVisibility(View.GONE);
            TextView tv_currentNumber = (TextView)v.findViewById(R.id.tv_current_number);
            tv_currentNumber.setVisibility(View.GONE);
            Button applyBtn = (Button)v.findViewById(R.id.applyBtn);
            applyBtn.setTag(f);

            if(recommendedInfo.containsKey(f)){

                rl_background.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
                applyBtn.setText("철회하기");
                applyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String field = (String)v.getTag();
                        unrecommend(field);
                    }
                });

            }else{

                rl_background.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                applyBtn.setText("추천하기");
                applyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String field = (String)v.getTag();
                        recommend(field);
                    }
                });

            }

            li_recommendField.addView(v);

        }

    }

    private void unrecommend(String field){

        String id = recommendedInfo.get(field);

        progressDialog.show();

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("service", "unrecommendUser");
        map.put("id", id);

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

            @Override
            protected void afterThreadFinish(String data) {

                if("1".equals(data)){
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SHOW_LOADING));
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_REFRESH_LIST));
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SUCCESS_UNRECOMMEND));
                }else{
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_ERROR));
                }

            }
        }.start();

    }

    private void recommend(String field){

        progressDialog.show();

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("service", "recommendUser");
        map.put("userId", StartActivity.USER_ID);
        map.put("recipientId", userId);
        map.put("field", field);

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

            @Override
            protected void afterThreadFinish(String data) {

                if("1".equals(data)){
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SHOW_LOADING));
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_REFRESH_LIST));
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SUCCESS_RECOMMEND));
                }else{
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_ERROR));
                }

            }
        }.start();

    }

    private void getRecommendedList(){

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("service", "getRecommendedList");
        map.put("userId", StartActivity.USER_ID);
        map.put("recipientId", userId);

        loading.show();
        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {
            @Override
            protected void afterThreadFinish(String data) {

                try {
                    // PHP에서 받아온 JSON 데이터를 JSON오브젝트로 변환
                    JSONObject jObject = new JSONObject(data);
                    // results라는 key는 JSON배열로 되어있다.
                    JSONArray results = jObject.getJSONArray("result");
                    String countTemp = (String) jObject.get("num_result");
                    int count = Integer.parseInt(countTemp);

                    //recommendedList.clear();
                    recommendedInfo.clear();

                    for (int i = 0; i < count; ++i) {
                        JSONObject temp = results.getJSONObject(i);
                        //recommendedList.add((String) temp.get("field"));
                        recommendedInfo.put((String) temp.get("field"), (String) temp.get("id"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));

            }
        }.start();

    }


    private class MyHandler extends Handler {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MESSAGE_MAKE_LIST:
                    loading.hide();
                    makeList();
                    break;
                case MSG_MESSAGE_SHOW_LOADING:
                    loading.show();
                    break;
                case MSG_MESSAGE_REFRESH_LIST:
                    progressDialog.hide();
                    getRecommendedList();
                    break;
                case MSG_MESSAGE_SUCCESS_RECOMMEND:
                    new MaterialDialog.Builder(RecommendListActivity.this)
                            .title("성공")
                            .content("성공적으로 추천하였습니다.")
                            .positiveText("확인")
                            .show();
                    break;
                case MSG_MESSAGE_SUCCESS_UNRECOMMEND:
                    new MaterialDialog.Builder(RecommendListActivity.this)
                            .title("성공")
                            .content("성공적으로 철회하였습니다.")
                            .positiveText("확인")
                            .show();
                    break;
                case MSG_MESSAGE_ERROR:
                    new MaterialDialog.Builder(RecommendListActivity.this)
                            .title("오류")
                            .content("잠시 후 다시시도해주세요.")
                            .positiveText("확인")
                            .show();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

}

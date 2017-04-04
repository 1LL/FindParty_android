package ga.findparty.findparty.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;

import ga.findparty.findparty.BaseActivity;
import ga.findparty.findparty.Information;
import ga.findparty.findparty.R;
import ga.findparty.findparty.StartActivity;
import ga.findparty.findparty.util.AdditionalFunc;
import ga.findparty.findparty.util.ParsePHP;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class DetailBoardActivity extends BaseActivity {

    public static final int UPDATE_APPLY_FORM = 100;

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_FILL_FORM = 500;
    private final int MSG_MESSAGE_MAKE_LIST = 501;
    private final int MSG_MESSAGE_SHOW_APPLY_FORM = 502;
    private final int MSG_MESSAGE_NOT_SHOW_APPLY_FORM = 503;

    private AVLoadingIndicatorView loadingContent;
    private AVLoadingIndicatorView loadingDuration;
    private AVLoadingIndicatorView loadingList;
    private MaterialDialog progressDialog;

    // UI
    private ImageView profileImage;
    private TextView tv_name;
    private TextView tv_email;
    private TextView tv_content;
    private TextView tv_interest;
    private TextView tv_duration;
    // UI-FIELD
    private LinearLayout li_add_field;

    private String boardId;
    private HashMap<String, Object> item;
    private ArrayList<HashMap<String, Object>> fieldList;
    private HashMap<String, Object> lastTouchField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_board);

        Intent intent = getIntent();
        boardId = intent.getStringExtra("boardId");

        fieldList = new ArrayList<>();

        init();

        getBoardList();
        getFieldList();

    }

    private void init(){

        progressDialog = new MaterialDialog.Builder(this)
                .content("잠시만 기다려주세요.")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .theme(Theme.LIGHT)
                .build();

        profileImage = (ImageView)findViewById(R.id.profileImg);
        tv_name = (TextView)findViewById(R.id.tv_name);
        tv_email = (TextView)findViewById(R.id.tv_email);
        tv_content = (TextView)findViewById(R.id.tv_content);
        tv_interest = (TextView)findViewById(R.id.tv_interest);
        tv_duration = (TextView)findViewById(R.id.tv_duration);

        li_add_field = (LinearLayout)findViewById(R.id.li_add_field);

        loadingContent = (AVLoadingIndicatorView)findViewById(R.id.loading_content);
        loadingDuration = (AVLoadingIndicatorView)findViewById(R.id.loading_duration);
        loadingList = (AVLoadingIndicatorView)findViewById(R.id.loading_list);

        loadingContent.show();
        loadingDuration.show();
        loadingList.show();

    }

    public void makeList(){

        li_add_field.removeAllViews();

        for(int i=0; i<fieldList.size(); i++){
            final HashMap<String, Object> map  = fieldList.get(i);

            View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.detail_board_custom_item, null, false);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DetailBoardActivity.this, ShowApplyPeopleActivity.class);
                    intent.putExtra("id", (String)map.get("id"));
                    intent.putExtra("title", (String)map.get("field"));
                    startActivity(intent);
                }
            });

            TextView tv_title = (TextView)v.findViewById(R.id.tv_title);
            TextView tv_number = (TextView)v.findViewById(R.id.tv_number);
            TextView tv_currentNumber = (TextView)v.findViewById(R.id.tv_current_number);
            Button applyBtn = (Button)v.findViewById(R.id.applyBtn);

            String title = (String)map.get("field");
            String number = (String)map.get("number") + "명";
            String currentNumber = "현재 " + ((ArrayList)map.get("participant")).size() + "명 지원 중";

            tv_title.setText(title);
            tv_number.setText(number);
            tv_currentNumber.setText(currentNumber);

            applyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    applyField(map);
                }
            });

            li_add_field.addView(v);

        }

    }

    private void fillForm(){

        Picasso.with(getApplicationContext())
                .load((String)item.get("img"))
                .transform(new CropCircleTransformation())
                .into(profileImage);
        tv_name.setText((String)item.get("name"));
        tv_email.setText((String)item.get("email"));
        tv_content.setText((String)item.get("content"));
        tv_interest.setText(AdditionalFunc.interestListToString((ArrayList<String>)item.get("interest")));

        String startText = AdditionalFunc.getDateString((Long)item.get("start"));
        String finishText = AdditionalFunc.getDateString((Long)item.get("finish"));
        tv_duration.setText(startText + " ~ " + finishText);

    }

    public void applyField(HashMap<String, Object> h){

        lastTouchField = h;
        String userId = (String)item.get("userId");

        if(StartActivity.USER_ID.equals(userId)){
            new MaterialDialog.Builder(this)
                    .title("경고")
                    .content("본인 글에는 지원할 수 없습니다.")
                    .positiveText("확인")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }else{
            progressDialog.show();

            HashMap<String, String> map = new HashMap<>();
            map.put("service", "checkApplyAble");
            map.put("userId", StartActivity.USER_ID);
            map.put("boardFieldIdList", AdditionalFunc.makeBoardFieldIdListToString(fieldList));

            new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {

                @Override
                protected void afterThreadFinish(String data) {

                    if("1".equals(data)){
                        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SHOW_APPLY_FORM));
                    }else{
                        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_NOT_SHOW_APPLY_FORM));
                    }

                }
            }.start();
        }

    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_FILL_FORM:
                    fillForm();
                    loadingContent.hide();
                    loadingDuration.hide();
                    break;
                case MSG_MESSAGE_MAKE_LIST:
                    makeList();
                    loadingList.hide();
                    break;
                case MSG_MESSAGE_SHOW_APPLY_FORM:
                    progressDialog.hide();
                    Intent intent = new Intent(getApplicationContext(), ApplyFormActivity.class);
                    intent.putExtra("item", lastTouchField);
                    startActivityForResult(intent, UPDATE_APPLY_FORM);
                    break;
                case MSG_MESSAGE_NOT_SHOW_APPLY_FORM:
                    progressDialog.hide();
                    new MaterialDialog.Builder(DetailBoardActivity.this)
                            .title("알림")
                            .content("이미 다른 분야에 지원하였습니다.")
                            .positiveText("확인")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    break;
                default:
                    break;
            }
        }
    }

    private void getBoardList(){

        HashMap<String, String> map = new HashMap<>();
        map.put("service", "getBoardList");
        map.put("id", boardId);

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {

            @Override
            protected void afterThreadFinish(String data) {

                item = AdditionalFunc.getBoardListInfo(data).get(0);
                if(item.size() > 0) {
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_FILL_FORM));
                }else{

                }

            }
        }.start();

    }

    private void getFieldList(){

        HashMap<String, String> map = new HashMap<>();
        map.put("service", "getBoardFieldList");
        map.put("boardId", boardId);

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {

            @Override
            protected void afterThreadFinish(String data) {

                fieldList = AdditionalFunc.getBoardFieldListInfo(data);
                if(fieldList.size() > 0) {
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));
                }else{

                }

            }
        }.start();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case UPDATE_APPLY_FORM:
                getFieldList();
                break;
            default:
                break;
        }
    }

}

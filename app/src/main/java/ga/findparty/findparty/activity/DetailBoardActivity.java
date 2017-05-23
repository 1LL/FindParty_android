package ga.findparty.findparty.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import ga.findparty.findparty.profile.ProfileActivity;
import ga.findparty.findparty.util.AdditionalFunc;
import ga.findparty.findparty.util.ParsePHP;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class DetailBoardActivity extends BaseActivity {

    public static final int UPDATE_APPLY_FORM = 100;
    public static final int UPDATE_APPLY_FORM_SELECT_MODE = 101;

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_FILL_FORM = 500;
    private final int MSG_MESSAGE_MAKE_LIST = 501;
    private final int MSG_MESSAGE_SHOW_APPLY_FORM = 502;
    private final int MSG_MESSAGE_NOT_SHOW_APPLY_FORM = 503;
    private final int MSG_MESSAGE_SUCCESS_DECISION = 504;
    private final int MSG_MESSAGE_FAIL_DECISION = 505;
    private final int MSG_MESSAGE_ALREADY_WRITE_BOARD = 506;
    private final int MSG_MESSAGE_ALREADY_APPLY_FIELD = 507;

    private AVLoadingIndicatorView loadingContent;
    private AVLoadingIndicatorView loadingDuration;
    private AVLoadingIndicatorView loadingList;
    private MaterialDialog progressDialog;

    // UI
    private ImageView profileImage;
    private TextView tv_name;
    private TextView tv_email;
    private TextView recommendBtn;
    private TextView tv_content;
    private TextView tv_interest;
    private TextView tv_duration;
    private Button decisionBtn;
    // UI-FIELD
    private LinearLayout li_add_field;

    private String boardId;
    private String courseId;
    private HashMap<String, Object> item;
    private ArrayList<HashMap<String, Object>> fieldList;
    private HashMap<String, Object> lastTouchField;
    private ArrayList<String> endFieldList;
    private boolean isMyBoard;
    private boolean isDecision;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_board);

        Intent intent = getIntent();
        boardId = intent.getStringExtra("boardId");
        courseId = intent.getStringExtra("courseId");
        String userId = intent.getStringExtra("userId");
        isDecision = intent.getBooleanExtra("isDecision", false);

        if(getUserID(this).equals(userId)){
            isMyBoard = true;
        }

        fieldList = new ArrayList<>();
        endFieldList = new ArrayList<>();

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
        recommendBtn = (TextView)findViewById(R.id.recommend_btn);
        tv_content = (TextView)findViewById(R.id.tv_content);
        tv_interest = (TextView)findViewById(R.id.tv_interest);
        tv_duration = (TextView)findViewById(R.id.tv_duration);
        decisionBtn = (Button)findViewById(R.id.decision_btn);
        decisionBtn.setEnabled(false);

        recommendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailBoardActivity.this, RecommendCourseUserActivity.class);
                intent.putExtra("courseId", courseId);
                intent.putExtra("boardId", boardId);
                startActivity(intent);
            }
        });

        li_add_field = (LinearLayout)findViewById(R.id.li_add_field);

        loadingContent = (AVLoadingIndicatorView)findViewById(R.id.loading_content);
        loadingDuration = (AVLoadingIndicatorView)findViewById(R.id.loading_duration);
        loadingList = (AVLoadingIndicatorView)findViewById(R.id.loading_list);

        findViewById(R.id.rl_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailBoardActivity.this, ProfileActivity.class);
                intent.putExtra("id", (String)item.get("userId"));
                startActivity(intent);
            }
        });

        loadingContent.show();
        loadingDuration.show();
        loadingList.show();

    }

    public void makeList(){

        li_add_field.removeAllViews();
        endFieldList.clear();

        for(int i=0; i<fieldList.size(); i++){
            final HashMap<String, Object> map  = fieldList.get(i);

            View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.detail_board_custom_item, null, false);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DetailBoardActivity.this, ShowApplyPeopleActivity.class);
                    intent.putExtra("id", (String)map.get("id"));
                    intent.putExtra("title", (String)map.get("field"));
                    intent.putExtra("number", (int)map.get("number"));
                    startActivityForResult(intent, UPDATE_APPLY_FORM);
                }
            });


            ArrayList<String> memberList = (ArrayList<String>)map.get("member");
            RelativeLayout rl_finish = (RelativeLayout)v.findViewById(R.id.rl_finish);
            if(memberList.size() > 0){
                rl_finish.setVisibility(View.VISIBLE);
                v.setOnClickListener(null);
                endFieldList.addAll(memberList);
            }

            TextView tv_title = (TextView)v.findViewById(R.id.tv_title);
            TextView tv_number = (TextView)v.findViewById(R.id.tv_number);
            TextView tv_currentNumber = (TextView)v.findViewById(R.id.tv_current_number);
            Button applyBtn = (Button)v.findViewById(R.id.applyBtn);
            if(isMyBoard){
                applyBtn.setText("마감하기");
            }else{
                applyBtn.setText("지원하기");
            }

            String title = (String)map.get("field");
            String number = (int)map.get("number") + "명";
            String currentNumber;
            if(memberList.size() > 0){
                currentNumber = "마감되었습니다.";
            }else {
                 currentNumber = "현재 " + ((ArrayList) map.get("participant")).size() + "명 지원 중";
            }

            tv_title.setText(title);
            tv_number.setText(number);
            tv_currentNumber.setText(currentNumber);

            applyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isMyBoard){
                        Intent intent = new Intent(DetailBoardActivity.this, ShowApplyPeopleActivity.class);
                        intent.putExtra("id", (String)map.get("id"));
                        intent.putExtra("title", (String)map.get("field"));
                        intent.putExtra("isSelectMode", true);
                        intent.putExtra("number", (int)map.get("number"));
                        intent.putStringArrayListExtra("question", (ArrayList<String>)item.get("question"));
                        startActivityForResult(intent, UPDATE_APPLY_FORM_SELECT_MODE);
                    }else {
                        applyField(map);
                    }
                }
            });

            if(isDecision){
                v.setOnClickListener(null);
                applyBtn.setOnClickListener(null);
                applyBtn.setEnabled(false);
            }


            li_add_field.addView(v);

        }

        if(endFieldList.size() > 0){
            decisionBtn.setEnabled(true);
            decisionBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
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
        if(isMyBoard && !isDecision){
            decisionBtn.setVisibility(View.VISIBLE);
            decisionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    decision();
                }
            });
            recommendBtn.setVisibility(View.VISIBLE);
        }

        String startText = AdditionalFunc.getDateString((Long)item.get("start"));
        String finishText = AdditionalFunc.getDateString((Long)item.get("finish"));
        tv_duration.setText(startText + " ~ " + finishText);

    }

    private void decision(){

        new MaterialDialog.Builder(this)
                .title("확인")
                .content("총 " + endFieldList.size() + "명으로 팀을 구성하시겠습니까?")
                .positiveText("확인")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();

                        progressDialog.show();

                        HashMap<String, String> map = new HashMap<>();
                        map.put("service", "decisionMember");
                        map.put("userId", getUserID(DetailBoardActivity.this));
                        map.put("boardId", boardId);
                        map.put("courseId", courseId);
                        map.put("member", AdditionalFunc.arrayListToString(endFieldList));

                        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {

                            @Override
                            protected void afterThreadFinish(String data) {

                                if("1".equals(data)){
                                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SUCCESS_DECISION));
                                }else{
                                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_FAIL_DECISION));
                                }

                            }
                        }.start();
                    }
                })
                .negativeText("취소")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();

    }

    public void applyField(HashMap<String, Object> h){

        lastTouchField = h;
        String userId = (String)item.get("userId");

        if(getUserID(this).equals(userId)){
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
            map.put("service", "checkAddAble");
            map.put("userId", getUserID(this));
            map.put("courseId", courseId);

            new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {

                @Override
                protected void afterThreadFinish(String data) {

                    if("0".equals(data)){
                        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SHOW_APPLY_FORM));
                    }else if("1".equals(data)){
                        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_ALREADY_WRITE_BOARD));
                    }else if("2".equals(data)){
                        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_ALREADY_APPLY_FIELD));
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
                    intent.putExtra("courseId", courseId);
                    intent.putStringArrayListExtra("question", (ArrayList<String>)item.get("question"));
                    startActivityForResult(intent, UPDATE_APPLY_FORM);
                    break;
                case MSG_MESSAGE_SUCCESS_DECISION:
                    progressDialog.hide();
                    setResult(CourseBoardActivity.UPDATE_COURSE_BOARD);
                    finish();
                    break;
                case MSG_MESSAGE_FAIL_DECISION:
                    progressDialog.hide();
                    new MaterialDialog.Builder(DetailBoardActivity.this)
                            .title("오류")
                            .content("잠시 후 다시 시도해주세요.")
                            .positiveText("확인")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    break;
                case MSG_MESSAGE_ALREADY_WRITE_BOARD:
                    progressDialog.hide();
                    new MaterialDialog.Builder(DetailBoardActivity.this)
                            .title("경고")
                            .content("이미 다른 모집글을 작성하였습니다.")
                            .positiveText("확인")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    break;
                case MSG_MESSAGE_ALREADY_APPLY_FIELD:
                    progressDialog.hide();
                    new MaterialDialog.Builder(DetailBoardActivity.this)
                            .title("경고")
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
            case UPDATE_APPLY_FORM_SELECT_MODE:
            case UPDATE_APPLY_FORM:
                getFieldList();
                break;
            default:
                break;
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

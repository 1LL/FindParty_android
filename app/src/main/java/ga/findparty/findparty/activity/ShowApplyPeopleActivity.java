package ga.findparty.findparty.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import ga.findparty.findparty.Information;
import ga.findparty.findparty.R;
import ga.findparty.findparty.StartActivity;
import ga.findparty.findparty.profile.ProfileActivity;
import ga.findparty.findparty.util.AdditionalFunc;
import ga.findparty.findparty.util.ParsePHP;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class ShowApplyPeopleActivity extends AppCompatActivity {

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;
    private final int MSG_MESSAGE_FINISH_SELECT_MEMBER = 501;
    private final int MSG_MESSAGE_ERROR_SELECT_MEMBER = 502;

    private Toolbar toolbar;
    private AVLoadingIndicatorView loading;
    private TextView tv_msg;
    private Button endBtn;

    private LinearLayout li_list;

    private boolean isSelectMode;
    private boolean isTeamListMode;
    private String boardFieldId;
    private int number;
    private String fieldTitle;
    private ArrayList<HashMap<String, Object>> list;
    private ArrayList<String> selectList;
    private ArrayList<String> question;

    private MaterialDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_apply_people);

        selectList = new ArrayList<>();
        question = new ArrayList<>();

        Intent intent = getIntent();
        boardFieldId = intent.getStringExtra("id");
        fieldTitle = intent.getStringExtra("title");
        number = intent.getIntExtra("number", 0);
        isSelectMode = intent.getBooleanExtra("isSelectMode", false);
        if(isSelectMode){
            question = intent.getStringArrayListExtra("question");
        }
        isTeamListMode = intent.getBooleanExtra("isTeamListMode", false);
        if(isTeamListMode){
            list = (ArrayList<HashMap<String, Object>>)intent.getSerializableExtra("list");
        }


        init();

    }

    private void init(){

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        ((TextView)findViewById(R.id.toolbar_field_title)).setText(fieldTitle);

        tv_msg = (TextView)findViewById(R.id.tv_msg);
        tv_msg.setVisibility(View.GONE);

        endBtn = (Button)findViewById(R.id.end_btn);
        endBtn.setVisibility(View.GONE);
        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();

                HashMap<String, String> map = new HashMap<>();
                map.put("service", "selectMember");
                map.put("boardFieldId", boardFieldId);
                map.put("member", AdditionalFunc.arrayListToString(selectList));

                new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

                    @Override
                    protected void afterThreadFinish(String data) {

                        if("1".equals(data)){
                            handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_FINISH_SELECT_MEMBER));
                        }else{
                            handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_ERROR_SELECT_MEMBER));
                        }

                    }
                }.start();
            }
        });

        li_list = (LinearLayout)findViewById(R.id.li_list);

        loading = (AVLoadingIndicatorView)findViewById(R.id.loading);

        if(!isTeamListMode) {
            getParticipantList();

            progressDialog = new MaterialDialog.Builder(this)
                    .content("잠시만 기다려주세요.")
                    .progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .theme(Theme.LIGHT)
                    .build();
        }else{
            loading.hide();
            makeList();
        }


    }

    private void getParticipantList(){

        loading.show();

        HashMap<String, String> map = new HashMap<>();
        map.put("service", "applyFieldUserList");
        map.put("boardFieldId", boardFieldId);

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

            @Override
            protected void afterThreadFinish(String data) {

                list = AdditionalFunc.getApplyParticipantList(data);
                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));

            }
        }.start();

    }

    private void makeList(){

        li_list.removeAllViews();

        if(list.size() == 0){
            tv_msg.setVisibility(View.VISIBLE);
        }else{
            tv_msg.setVisibility(View.GONE);
        }

        for(int i=0; i<list.size(); i++){
            final HashMap<String, Object> map  = list.get(i);

            View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.show_apply_people_custom_item, null, false);

            v.findViewById(R.id.rl_profile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ShowApplyPeopleActivity.this, ProfileActivity.class);
                    intent.putExtra("id", (String)map.get("userId"));
                    startActivity(intent);
                }
            });

            TextView tv_name = (TextView)v.findViewById(R.id.tv_name);
            TextView tv_email = (TextView)v.findViewById(R.id.tv_email);
            ImageView profileImg = (ImageView)v.findViewById(R.id.profileImg);
            TextView tv_field = (TextView)v.findViewById(R.id.tv_field);

            LinearLayout li_answerField = (LinearLayout)v.findViewById(R.id.li_answer_field);

            ImageView star1 = (ImageView)v.findViewById(R.id.star1);
            ImageView star2 = (ImageView)v.findViewById(R.id.star2);
            ImageView star3 = (ImageView)v.findViewById(R.id.star3);

            TextView showTimeBtn = (TextView)v.findViewById(R.id.show_time_btn);
            showTimeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<Integer> monList = (ArrayList<Integer>)map.get("mon");
                    ArrayList<Integer> tueList = (ArrayList<Integer>)map.get("tue");
                    ArrayList<Integer> wedList = (ArrayList<Integer>)map.get("wed");
                    ArrayList<Integer> thuList = (ArrayList<Integer>)map.get("thu");
                    ArrayList<Integer> friList = (ArrayList<Integer>)map.get("fri");

                    Intent intent = new Intent(ShowApplyPeopleActivity.this, SelectTimeActivity.class);
                    intent.putIntegerArrayListExtra("mon", monList);
                    intent.putIntegerArrayListExtra("tue", tueList);
                    intent.putIntegerArrayListExtra("wed", wedList);
                    intent.putIntegerArrayListExtra("thu", thuList);
                    intent.putIntegerArrayListExtra("fri", friList);
                    intent.putExtra("isEditAble", false);
                    startActivity(intent);
                }
            });

            TextView tv_content = (TextView)v.findViewById(R.id.tv_content);


            tv_name.setText((String)map.get("name"));
            tv_email.setText((String)map.get("email"));
            Picasso.with(getApplicationContext())
                    .load((String)map.get("img"))
                    .transform(new CropCircleTransformation())
                    .into(profileImg);
            if(isTeamListMode){
                tv_field.setText((String)map.get("field"));
            }else{
                tv_field.setVisibility(View.GONE);
            }

            int skill = (int)map.get("skill");
            switch (skill){
                case 1:
                    star1.setImageResource(R.drawable.star_yellow);
                    star2.setImageResource(R.drawable.star_black);
                    star3.setImageResource(R.drawable.star_black);
                    break;
                case 2:
                    star1.setImageResource(R.drawable.star_yellow);
                    star2.setImageResource(R.drawable.star_yellow);
                    star3.setImageResource(R.drawable.star_black);
                    break;
                case 3:
                    star1.setImageResource(R.drawable.star_yellow);
                    star2.setImageResource(R.drawable.star_yellow);
                    star3.setImageResource(R.drawable.star_yellow);
                    break;
            }

            if(isSelectMode){
                li_answerField.setVisibility(View.VISIBLE);
                li_answerField.removeAllViews();
                ArrayList<String> answerList = (ArrayList<String>)map.get("answer");
                for(int k=0; k<answerList.size(); k++){
                    String q = question.get(k);
                    String a = answerList.get(k);
                    View qav = getLayoutInflater().inflate(R.layout.question_and_answer_custom_item, null, false);

                    TextView tv_question = (TextView)qav.findViewById(R.id.tv_question);
                    TextView tv_answer = (TextView)qav.findViewById(R.id.tv_answer);

                    tv_question.setText(q);
                    tv_answer.setText(a);

                    li_answerField.addView(qav);

                }

            }else{
                li_answerField.setVisibility(View.GONE);
            }


            tv_content.setText((String)map.get("content"));

            Button selectBtn = (Button)v.findViewById(R.id.select_btn);
            if(isSelectMode){
                selectBtn.setVisibility(View.VISIBLE);
            }
            selectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String id = (String)map.get("id");
                    if(selectList.contains(id)){
                        selectList.remove(id);
                        v.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
                        ((Button)v).setText("선택하기");
                    }else{
                        if(selectList.size() < number) {
                            selectList.add(id);
                            v.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                            ((Button) v).setText("취소하기");
                        }else{
                            showNumberWarningAlert();
                        }
                    }
                }
            });

            li_list.addView(v);

        }

    }

    private void showNumberWarningAlert(){

        new MaterialDialog.Builder(this)
                .title("경고")
                .content("모집인원을 초과해서 선택할 수 없습니다.")
                .positiveText("확인")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();

    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_MAKE_LIST:
                    if(isSelectMode){
                        endBtn.setVisibility(View.VISIBLE);
                    }
                    loading.hide();
                    makeList();
                    break;
                case MSG_MESSAGE_FINISH_SELECT_MEMBER:
                    progressDialog.hide();
                    setResult(DetailBoardActivity.UPDATE_APPLY_FORM_SELECT_MODE);
                    finish();
                    break;
                case MSG_MESSAGE_ERROR_SELECT_MEMBER:
                    progressDialog.hide();
                    new MaterialDialog.Builder(ShowApplyPeopleActivity.this)
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

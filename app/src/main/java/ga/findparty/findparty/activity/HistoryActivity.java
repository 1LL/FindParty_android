package ga.findparty.findparty.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
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

public class HistoryActivity extends BaseActivity {

    public static final int UPDATE_HISTORY_LIST = 100;

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;

    private AVLoadingIndicatorView loading;
    private TextView tv_msg;
    private LinearLayout li_listField;

    // FAB
    private FloatingActionsMenu menu;
    private FloatingActionButton addMeetingBtn;

    private String teamId;
    private ArrayList<HashMap<String, Object>> list;
    private ArrayList<HashMap<String, Object>> memberList;
    private boolean isEditAble;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Intent intent = getIntent();
        teamId = intent.getStringExtra("teamId");
        memberList = (ArrayList<HashMap<String, Object>>)intent.getSerializableExtra("memberList");
        for(HashMap<String, Object> map : memberList){
            if(StartActivity.USER_ID.equals((String)map.get("userId"))){
                isEditAble = true;
            }
        }

        list = new ArrayList<>();

        init();

        getHistoryList();

    }

    private void init(){

        loading = (AVLoadingIndicatorView)findViewById(R.id.loading);
        tv_msg = (TextView)findViewById(R.id.tv_msg);
        tv_msg.setVisibility(View.GONE);
        li_listField = (LinearLayout)findViewById(R.id.li_list_field);

        setFab();

    }

    private void setFab(){

        menu = (FloatingActionsMenu)findViewById(R.id.multiple_actions);

        addMeetingBtn = (FloatingActionButton) findViewById(R.id.add_meeting);
        addMeetingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoryActivity.this, AddHistoryActivity.class);
                intent.putExtra("isMeetingMode", true);
                intent.putExtra("memberList", memberList);
                intent.putExtra("teamId", teamId);
                startActivityForResult(intent, UPDATE_HISTORY_LIST);
                menu.toggle();
            }
        });
        addMeetingBtn.setTitle("회의추가");

        if(isEditAble){
            menu.setVisibility(View.VISIBLE);
        }else{
            menu.setVisibility(View.GONE);
        }

    }

    private void getHistoryList(){

        loading.show();
        HashMap<String, String> map = new HashMap<>();
        map.put("service", "getHistoryList");
        map.put("teamId", teamId);

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

            @Override
            protected void afterThreadFinish(String data) {
                list = AdditionalFunc.getHistoryList(data);
                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));
            }
        }.start();

    }

    private void makeList(){

        li_listField.removeAllViews();
        if(list.size() == 0){
            tv_msg.setVisibility(View.VISIBLE);
        }else{
            tv_msg.setVisibility(View.GONE);
        }

        for(HashMap<String, Object> map : list){

//            View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.history_list_custom_item, null, false);
            View v = getLayoutInflater().inflate(R.layout.history_list_custom_item, null, false);

            TextView tv_title = (TextView)v.findViewById(R.id.tv_title);
            RelativeLayout rl_profile = (RelativeLayout)v.findViewById(R.id.rl_profile);
            ImageView profileImg = (ImageView)v.findViewById(R.id.profileImg);
            TextView tv_name = (TextView)v.findViewById(R.id.tv_name);
            TextView tv_email = (TextView)v.findViewById(R.id.tv_email);
            TextView tv_content = (TextView)v.findViewById(R.id.tv_content);
            TextView tv_date = (TextView)v.findViewById(R.id.tv_date);

            String id = (String)map.get("id");
            String teamId = (String)map.get("teamId");
            final String userId = (String)map.get("userId");
            String name = (String)map.get("name");
            String email = (String)map.get("email");
            String img = (String)map.get("img");
            String title = (String)map.get("title");
            String content = (String)map.get("content");
            Long date = (Long)map.get("date");

            tv_title.setText(title);
            Picasso.with(getApplicationContext())
                    .load(img)
                    .transform(new CropCircleTransformation())
                    .into(profileImg);
            tv_name.setText(name);
            tv_email.setText(email);
            tv_content.setText(content);
            tv_date.setText(AdditionalFunc.getDateString(date));

            rl_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HistoryActivity.this, ProfileActivity.class);
                    intent.putExtra("id", userId);
                    startActivity(intent);
                }
            });



            li_listField.addView(v);

        }

    }


    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_MAKE_LIST:
                    loading.hide();
                    makeList();
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case UPDATE_HISTORY_LIST:
                getHistoryList();
                break;
            default:
                break;
        }
    }

}

package ga.findparty.findparty.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
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

    private Toolbar toolbar;
    private AVLoadingIndicatorView loading;
    private TextView tv_msg;

    private LinearLayout li_list;

    private String boardFieldId;
    private String fieldTitle;
    private ArrayList<HashMap<String, Object>> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_apply_people);

        Intent intent = getIntent();
        boardFieldId = intent.getStringExtra("id");
        fieldTitle = intent.getStringExtra("title");

        init();

    }

    private void init(){

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        ((TextView)findViewById(R.id.toolbar_field_title)).setText(fieldTitle);

        tv_msg = (TextView)findViewById(R.id.tv_msg);
        tv_msg.setVisibility(View.GONE);

        li_list = (LinearLayout)findViewById(R.id.li_list);

        loading = (AVLoadingIndicatorView)findViewById(R.id.loading);

        getParticipantList();

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

            tv_content.setText((String)map.get("content"));

            li_list.addView(v);

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

}

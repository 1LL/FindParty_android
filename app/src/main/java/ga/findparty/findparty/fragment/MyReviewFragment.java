package ga.findparty.findparty.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;

import ga.findparty.findparty.Information;
import ga.findparty.findparty.R;
import ga.findparty.findparty.StartActivity;
import ga.findparty.findparty.activity.ReviewListActivity;
import ga.findparty.findparty.activity.ShowApplyPeopleActivity;
import ga.findparty.findparty.profile.ProfileActivity;
import ga.findparty.findparty.util.AdditionalFunc;
import ga.findparty.findparty.util.ParsePHP;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class MyReviewFragment extends BaseFragment {

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;

    // UI
    private View view;
    private Context context;

    private ScrollView parentSV;
    private AVLoadingIndicatorView loading;
    private TextView tv_msg;
    private LinearLayout li_listField;
    private ArrayList<HorizontalScrollView> memberScrollViewList;

    private ArrayList<HashMap<String, Object>> list;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_my_review, container, false);
        context = container.getContext();

        memberScrollViewList = new ArrayList<>();

        initUI();

        getTeamList();

        return view;
    }

    private void initUI(){

        parentSV = (ScrollView)view.findViewById(R.id.sv_parent);
        parentSV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                for(HorizontalScrollView sv : memberScrollViewList){
                    sv.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            }
        });
        loading = (AVLoadingIndicatorView)view.findViewById(R.id.loading);
        tv_msg = (TextView)view.findViewById(R.id.tv_msg);
        tv_msg.setVisibility(View.GONE);
        li_listField = (LinearLayout)view.findViewById(R.id.li_list_field);

    }

    public void makeList(){

        memberScrollViewList.clear();
        li_listField.removeAllViews();

        int count = 0;

        for(int i=0; i<list.size(); i++){
            final HashMap<String, Object> map  = list.get(i);

            Long start = (Long)map.get("boardStart");
            Long finish = (Long)map.get("boardFinish");

            if(AdditionalFunc.getDday(finish) < 0){
                count+=1;
            }else{
                continue;
            }

            View v = LayoutInflater.from(context).inflate(R.layout.team_list_custom_item, null, false);

            TextView tv_courseTitle = (TextView)v.findViewById(R.id.tv_course_title);
            final String courseTitle = (String)map.get("courseTitle");
            String courseClass = (String)map.get("courseClass");
            tv_courseTitle.setText(courseTitle + "-" + courseClass);

            RelativeLayout rl_profile = (RelativeLayout)v.findViewById(R.id.rl_profile);
            rl_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.putExtra("id", (String)map.get("boardUserId"));
                    startActivity(intent);
                }
            });

            ImageView profileImg = (ImageView)v.findViewById(R.id.profileImg);
            TextView tv_name = (TextView)v.findViewById(R.id.tv_name);
            TextView tv_email = (TextView)v.findViewById(R.id.tv_email);
            Picasso.with(context)
                    .load((String)map.get("boardUserImg"))
                    .transform(new CropCircleTransformation())
                    .into(profileImg);
            tv_name.setText((String)map.get("boardUserName"));
            tv_email.setText((String)map.get("boardUserEmail"));

            final ArrayList<HashMap<String, Object>> memberList = (ArrayList<HashMap<String,Object>>)map.get("memberList");
            final ArrayList<HashMap<String, Object>> reviewList = new ArrayList<>();
            HashMap<String , Object> tempMap = new HashMap<>();
            tempMap.put("userId", (String)map.get("boardUserId"));
            tempMap.put("img", (String)map.get("boardUserImg"));
            tempMap.put("name", (String)map.get("boardUserName"));
            tempMap.put("email", (String)map.get("boardUserEmail"));
            tempMap.put("field", "대표");
            tempMap.put("review", (String)map.get("review"));
            reviewList.add(tempMap);


            HorizontalScrollView sv = (HorizontalScrollView)v.findViewById(R.id.sv_member);
            sv.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
            memberScrollViewList.add(sv);

            LinearLayout li_memberField = (LinearLayout)v.findViewById(R.id.li_member_field);
            li_memberField.removeAllViews();

            for(HashMap<String, Object> m : memberList){
                View mv = LayoutInflater.from(context).inflate(R.layout.team_list_user_custom_item, null, false);

                ImageView m_profileImg = (ImageView)mv.findViewById(R.id.profileImg);
                TextView m_tv_name = (TextView)mv.findViewById(R.id.tv_name);
                TextView m_tv_field = (TextView)mv.findViewById(R.id.tv_field);

                Picasso.with(context)
                        .load((String)m.get("img"))
                        .transform(new CropCircleTransformation())
                        .into(m_profileImg);
                m_tv_name.setText((String)m.get("name"));
                m_tv_field.setText((String)m.get("field"));

                mv.setTag(m);
                li_memberField.addView(mv);

                tempMap = new HashMap<>();
                tempMap.put("userId", (String)m.get("userId"));
                tempMap.put("img", (String)m.get("img"));
                tempMap.put("name", (String)m.get("name"));
                tempMap.put("email", (String)m.get("email"));
                tempMap.put("field", (String)m.get("field"));
                tempMap.put("review", (String)m.get("review"));
                reviewList.add(tempMap);

            }

            ImageView showApplyMemberBtn = (ImageView)v.findViewById(R.id.show_apply_member_btn);
            showApplyMemberBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ShowApplyPeopleActivity.class);
                    intent.putExtra("title", courseTitle);
                    intent.putExtra("isTeamListMode", true);
                    intent.putExtra("list", memberList);
                    startActivity(intent);
                }
            });

            TextView tv_duration = (TextView)v.findViewById(R.id.tv_duration);
            String startText = AdditionalFunc.getDateString((Long)map.get("boardStart"));
            String finishText = AdditionalFunc.getDateString((Long)map.get("boardFinish"));
            tv_duration.setText(startText + " ~ " + finishText);

            TextView tv_count = (TextView)v.findViewById(R.id.tv_count);
            tv_count.setText((memberList.size()+1) + "명");


            Button reviewBtn = (Button)v.findViewById(R.id.reviewBtn);
            reviewBtn.setVisibility(View.VISIBLE);
            reviewBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ReviewListActivity.class);
                    intent.putExtra("list", reviewList);
                    startActivity(intent);
                }
            });

            li_listField.addView(v);

        }

        if(count == 0){
            tv_msg.setVisibility(View.VISIBLE);
        }else{
            tv_msg.setVisibility(View.GONE);
        }

    }

    private void getTeamList(){

        loading.show();

        HashMap<String, String> map = new HashMap<>();
        map.put("service", "getTeamList");
        map.put("userId", getUserID(this));

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

            @Override
            protected void afterThreadFinish(String data) {
                list = AdditionalFunc.getTeamList(data);
                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));
            }
        }.start();

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

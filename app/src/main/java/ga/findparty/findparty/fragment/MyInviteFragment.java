package ga.findparty.findparty.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;

import ga.findparty.findparty.Information;
import ga.findparty.findparty.R;
import ga.findparty.findparty.activity.DetailBoardActivity;
import ga.findparty.findparty.activity.ReviewListActivity;
import ga.findparty.findparty.activity.ShowApplyPeopleActivity;
import ga.findparty.findparty.profile.ProfileActivity;
import ga.findparty.findparty.util.AdditionalFunc;
import ga.findparty.findparty.util.ParsePHP;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class MyInviteFragment extends BaseFragment {

    public final static int UPDATE_LIST = 200;

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;
    private final int MSG_MESSAGE_REMOVE_SUCCESS = 501;
    private final int MSG_MESSAGE_REMOVE_FAIL = 502;

    // UI
    private View view;
    private Context context;

    private ScrollView parentSV;
    private AVLoadingIndicatorView loading;
    private MaterialDialog progressDialog;
    private TextView tv_msg;
    private LinearLayout li_listField;

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

        view = inflater.inflate(R.layout.fragment_my_invite, container, false);
        context = container.getContext();

        initUI();

        getInviteList();

        return view;
    }

    private void initUI(){

        progressDialog = new MaterialDialog.Builder(context)
                .content("잠시만 기다려주세요.")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .theme(Theme.LIGHT)
                .build();

        loading = (AVLoadingIndicatorView)view.findViewById(R.id.loading);
        tv_msg = (TextView)view.findViewById(R.id.tv_msg);
        tv_msg.setVisibility(View.GONE);
        li_listField = (LinearLayout)view.findViewById(R.id.li_list_field);

    }

    public void makeList(){

        li_listField.removeAllViews();

        if(list.size() > 0){
            tv_msg.setVisibility(View.GONE);
        }else{
            tv_msg.setVisibility(View.VISIBLE);
        }

        for(int i=0; i<list.size(); i++){
            final HashMap<String, Object> map  = list.get(i);
            final String id = (String)map.get("id");
            final String boardId = (String)map.get("boardId");
            final String userId =(String)map.get("userId");
            final String courseId = (String)map.get("courseId");

            View v = LayoutInflater.from(context).inflate(R.layout.invite_list_custom_item, null, false);

            RelativeLayout rl_profile = (RelativeLayout)v.findViewById(R.id.rl_profile);
            ImageView profileImg = (ImageView)v.findViewById(R.id.profileImg);
            TextView tv_name = (TextView)v.findViewById(R.id.tv_name);
            TextView tv_course = (TextView)v.findViewById(R.id.tv_course);
            TextView tv_date = (TextView)v.findViewById(R.id.tv_date);
            ImageView dotMenu = (ImageView)v.findViewById(R.id.dot_menu);
            Button showBtn = (Button)v.findViewById(R.id.showBtn);

            rl_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.putExtra("id", (String)map.get("userId"));
                    startActivity(intent);
                }
            });
            Picasso.with(context)
                    .load((String)map.get("img"))
                    .transform(new CropCircleTransformation())
                    .into(profileImg);
            tv_name.setText((String)map.get("name"));
            tv_course.setText((String)map.get("courseTitle"));

            tv_date.setText(AdditionalFunc.getDateString((long)map.get("date")));

            dotMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(v.getContext(), v);
                    popup.getMenuInflater().inflate(R.menu.menu_delete, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.menu_delete:
                                    removeInvite(id);
                                    break;
                            }

                            return true;
                        }
                    });
                    popup.show();
                }
            });

            showBtn.setText("상세보기");
            showBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DetailBoardActivity.class);
                    intent.putExtra("boardId", boardId);
                    intent.putExtra("userId", userId);
                    intent.putExtra("courseId", courseId);
                    startActivity(intent);
                }
            });

            li_listField.addView(v);

        }

    }

    private void removeInvite(String id){

        progressDialog.show();

        HashMap<String, String> map = new HashMap<>();
        map.put("service", "removeInvite");
        map.put("id", id);

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

            @Override
            protected void afterThreadFinish(String data) {
                if("1".equals(data)){
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_REMOVE_SUCCESS));
                }else{
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_REMOVE_FAIL));
                }
            }
        }.start();

    }

    public void getInviteList(){

        loading.show();

        HashMap<String, String> map = new HashMap<>();
        map.put("service", "getInviteList");
        map.put("userId", getUserID(this));

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

            @Override
            protected void afterThreadFinish(String data) {
                list = AdditionalFunc.getInviteList(data);
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
                    progressDialog.hide();
                    makeList();
                    break;
                case MSG_MESSAGE_REMOVE_SUCCESS:
                    getInviteList();
                    break;
                case MSG_MESSAGE_REMOVE_FAIL:
                    progressDialog.hide();
                    new MaterialDialog.Builder(context)
                            .title("알림")
                            .content("잠시 후 다시 시도해주세요.")
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

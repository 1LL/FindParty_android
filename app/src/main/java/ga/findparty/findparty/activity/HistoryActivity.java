package ga.findparty.findparty.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
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
    private final int MSG_MESSAGE_DELETE_SUCCESS = 501;
    private final int MSG_MESSAGE_DELETE_FAIL = 502;

    private AVLoadingIndicatorView loading;
    private MaterialDialog progressDialog;
    private TextView tv_msg;
    private LinearLayout li_listField;

    // FAB
    private FloatingActionsMenu menu;
    private FloatingActionButton addPresentBtn;
    private FloatingActionButton addMeetingBtn;
    private FloatingActionButton addContentBtn;
    private FloatingActionButton addHomeworkBtn;

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
            if(getUserID(this).equals((String)map.get("userId"))){
                isEditAble = true;
            }
        }

        list = new ArrayList<>();

        init();

        getHistoryList();

    }

    private void init(){

        progressDialog = new MaterialDialog.Builder(this)
                .content("잠시만 기다려주세요.")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .theme(Theme.LIGHT)
                .build();
        loading = (AVLoadingIndicatorView)findViewById(R.id.loading);
        tv_msg = (TextView)findViewById(R.id.tv_msg);
        tv_msg.setVisibility(View.GONE);
        li_listField = (LinearLayout)findViewById(R.id.li_list_field);

        setFab();

    }

    private void setFab(){

        menu = (FloatingActionsMenu)findViewById(R.id.multiple_actions);

        addPresentBtn = (FloatingActionButton) findViewById(R.id.add_present);
        addPresentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoryActivity.this, AddHistoryActivity.class);
                intent.putExtra("isPresentMode", true);
                intent.putExtra("memberList", memberList);
                intent.putExtra("teamId", teamId);
                startActivityForResult(intent, UPDATE_HISTORY_LIST);
                menu.toggle();
            }
        });
        addPresentBtn.setTitle("발표추가");

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

        addContentBtn = (FloatingActionButton)findViewById(R.id.add_content);
        addContentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HistoryActivity.this, AddHistoryActivity.class);
                intent.putExtra("teamId", teamId);
                startActivityForResult(intent, UPDATE_HISTORY_LIST);
                menu.toggle();
            }
        });
        addContentBtn.setTitle("콘텐츠추가");

        addHomeworkBtn = (FloatingActionButton)findViewById(R.id.add_homework);
        addHomeworkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HistoryActivity.this, AddHistoryActivity.class);
                intent.putExtra("teamId", teamId);
                intent.putExtra("isHWMode", true);
                intent.putExtra("memberList", memberList);
                startActivityForResult(intent, UPDATE_HISTORY_LIST);
                menu.toggle();
            }
        });
        addHomeworkBtn.setTitle("개별과제추가");


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
            ImageView dotMenu = (ImageView)v.findViewById(R.id.dot_menu);
            TextView tv_content = (TextView)v.findViewById(R.id.tv_content);
            TextView referenceBtn = (TextView)v.findViewById(R.id.reference_btn);
            TextView tv_date = (TextView)v.findViewById(R.id.tv_date);

            final String id = (String)map.get("id");
            String teamId = (String)map.get("teamId");
            final String userId = (String)map.get("userId");
            String name = (String)map.get("name");
            String email = (String)map.get("email");
            String img = (String)map.get("img");
            String title = (String)map.get("title");
            String content = (String)map.get("content");
            final String[] reference = (String[])map.get("reference");
            Long date = (Long)map.get("date");
            ArrayList<HashMap<String, Object>> participant = (ArrayList<HashMap<String, Object>>)map.get("participant");

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

            dotMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(HistoryActivity.this, v);
                    popup.getMenuInflater().inflate(R.menu.menu_delete, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.menu_delete:
                                    deleteHistory(id);
                                    break;
                            }

                            return true;
                        }
                    });
                    popup.show();
                }
            });

            if(userId.equals(getUserID(this))){
                dotMenu.setVisibility(View.VISIBLE);
            }else{
                dotMenu.setVisibility(View.GONE);
            }

            // participant
            View line1 = v.findViewById(R.id.line1);
            if(participant.size() == 0){
                line1.setVisibility(View.GONE);
            }
            HorizontalScrollView sv = (HorizontalScrollView)v.findViewById(R.id.sv_participant);
            LinearLayout li_participantField = (LinearLayout)v.findViewById(R.id.li_participant_field);
            li_participantField.removeAllViews();
            for(HashMap<String, Object> p : participant){

                final String p_userId = (String)p.get("userId");

                View pv = getLayoutInflater().inflate(R.layout.team_list_user_custom_item, null, false);

                LinearLayout root = (LinearLayout)pv.findViewById(R.id.root);
                ImageView pvProfileImg = (ImageView)pv.findViewById(R.id.profileImg);
                TextView pvTv_name = (TextView)pv.findViewById(R.id.tv_name);
                TextView pvTv_field = (TextView)pv.findViewById(R.id.tv_field);

                root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HistoryActivity.this, ProfileActivity.class);
                        intent.putExtra("id", p_userId);
                        startActivity(intent);
                    }
                });
                Picasso.with(getApplicationContext())
                        .load((String)p.get("img"))
                        .transform(new CropCircleTransformation())
                        .into(pvProfileImg);
                pvTv_name.setText((String)p.get("name"));
                pvTv_field.setText((String)p.get("status"));

                li_participantField.addView(pv);

            }

            if(reference.length > 0){
                referenceBtn.setVisibility(View.VISIBLE);
                referenceBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new MaterialDialog.Builder(HistoryActivity.this)
                                .title("참고자료")
                                .items(reference)
                                .itemsCallback(new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                        String url = text.toString();
                                        if (!url.startsWith("http://") && !url.startsWith("https://"))
                                            url = "http://" + url;
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                        startActivity(browserIntent);
                                    }
                                })
                                .positiveText("닫기")
                                .show();
                    }
                });
            }else{
                referenceBtn.setVisibility(View.GONE);
            }

            li_listField.addView(v);

        }

    }

    private void deleteHistory(final String id){

        new MaterialDialog.Builder(this)
                .title("확인")
                .content("해당 게시글을 삭제하시겠습니까?")
                .positiveText("삭제")
                .negativeText("취소")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        progressDialog.show();
                        HashMap<String, String> map = new HashMap<>();
                        map.put("service", "removeHistory");
                        map.put("id", id);

                        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

                            @Override
                            protected void afterThreadFinish(String data) {
                                if("1".equals(data)) {
                                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_DELETE_SUCCESS));
                                }else{
                                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_DELETE_FAIL));
                                }
                            }
                        }.start();

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
                    loading.hide();
                    makeList();
                    break;
                case MSG_MESSAGE_DELETE_SUCCESS:
                    progressDialog.hide();
                    getHistoryList();
                    break;
                case MSG_MESSAGE_DELETE_FAIL:
                    progressDialog.hide();
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

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

}

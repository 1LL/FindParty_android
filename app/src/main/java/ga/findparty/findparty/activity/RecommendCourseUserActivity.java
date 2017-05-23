package ga.findparty.findparty.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;

import ga.findparty.findparty.BaseActivity;
import ga.findparty.findparty.Information;
import ga.findparty.findparty.R;
import ga.findparty.findparty.util.AdditionalFunc;
import ga.findparty.findparty.util.OnAdapterSupport;
import ga.findparty.findparty.util.OnLoadMoreListener;
import ga.findparty.findparty.util.ParsePHP;

public class RecommendCourseUserActivity extends BaseActivity implements OnAdapterSupport {

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;
    private final int MSG_MESSAGE_MAKE_ENDLESS_LIST = 501;
    private final int MSG_MESSAGE_PROGRESS_HIDE = 502;
    private final int MSG_MESSAGE_INVITE_SUCCESS = 503;
    private final int MSG_MESSAGE_INVITE_FAIL = 504;

    private AVLoadingIndicatorView loading;
    private MaterialDialog progressDialog;

    private FrameLayout root;
    private TextView tv_msg;

    private String courseId;
    private String boardId;
    private int page = 0;
    private ArrayList<HashMap<String, String>> tempList;
    private ArrayList<HashMap<String, String>> list;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private RecommendCourseUserListCustomAdapter adapter;
    private boolean isLoadFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend_course_user);

        Intent intent = getIntent();
        courseId = intent.getStringExtra("courseId");
        boardId = intent.getStringExtra("boardId");

        init();

    }

    private void init(){

        root = (FrameLayout)findViewById(R.id.root);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecommendCourseUserActivity.super.onBackPressed();
            }
        });
        tv_msg = (TextView)findViewById(R.id.tv_msg);
        tv_msg.setVisibility(View.GONE);

        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(mLinearLayoutManager);

        list = new ArrayList<>();
        tempList = new ArrayList<>();

        loading = (AVLoadingIndicatorView)findViewById(R.id.loading);
        progressDialog = new MaterialDialog.Builder(this)
                .content("잠시만 기다려주세요.")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .theme(Theme.LIGHT)
                .build();

        getRecommendList();

    }

    private void initLoadValue(){
        page = 0;
        isLoadFinish = false;
    }

    private void getRecommendList(){
        if(!isLoadFinish) {
            loading.show();
            HashMap<String, String> map = new HashMap<>();
            map.put("service", "getRecommendCourseUserList");
            map.put("myUserId", getUserID(this));
            map.put("page", Integer.toString(page));
            map.put("courseId", courseId);

            new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {

                @Override
                protected void afterThreadFinish(String data) {

                    if (page <= 0) {
                        list.clear();

                        list = AdditionalFunc.getRecommendCourseUserList(data);

                        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));
                    } else {

                        tempList.clear();

                        tempList = AdditionalFunc.getRecommendCourseUserList(data);

                        if (tempList.size() < 30) {
                            isLoadFinish = true;
                        }
                        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_ENDLESS_LIST));

                    }

                }
            }.start();
        }else{
            if(adapter != null){
                adapter.setLoaded();
            }
        }
    }

    public void makeList(){

        if(list.size() == 0){
            tv_msg.setVisibility(View.VISIBLE);
        }else{
            tv_msg.setVisibility(View.GONE);
        }

        adapter = new RecommendCourseUserListCustomAdapter(getApplicationContext(), list, rv, this, this);

        rv.setAdapter(adapter);

        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                page+=1;
                getRecommendList();
            }
        });

        adapter.notifyDataSetChanged();

    }

    private void addList(){

        for(int i=0; i<tempList.size(); i++){
            list.add(tempList.get(i));
            adapter.notifyItemInserted(list.size());
        }

        adapter.setLoaded();

    }

    public void invite(String targetId){

        progressDialog.show();

        HashMap<String, String> map = new HashMap<>();
        map.put("service", "saveInvite");
        map.put("targetId", targetId);
        map.put("boardId", boardId);

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {

            @Override
            protected void afterThreadFinish(String data) {

                if("1".equals(data)){
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_INVITE_SUCCESS));
                }else{
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_INVITE_FAIL));
                }

            }
        }.start();
    }

    @Override
    public void showView() {

    }

    @Override
    public void hideView() {

    }

    @Override
    public void redirectActivityForResult(Intent intent) {

    }

    @Override
    public void redirectActivity(Intent intent) {
        startActivity(intent);
    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_MAKE_LIST:
                    progressDialog.hide();
                    loading.hide();
                    makeList();
                    break;
                case MSG_MESSAGE_MAKE_ENDLESS_LIST:
                    progressDialog.hide();
                    loading.hide();
                    addList();
                    break;
                case MSG_MESSAGE_PROGRESS_HIDE:
                    progressDialog.hide();
                    loading.hide();
                    break;
                case MSG_MESSAGE_INVITE_SUCCESS:
                    progressDialog.hide();
                    new MaterialDialog.Builder(RecommendCourseUserActivity.this)
                            .title("알림")
                            .content("성공적으로 초대 메시지를 전송하였습니다.")
                            .positiveText("확인")
                            .show();
                    break;
                case MSG_MESSAGE_INVITE_FAIL:
                    progressDialog.hide();
                    new MaterialDialog.Builder(RecommendCourseUserActivity.this)
                            .title("알림")
                            .content("잠시 후 다시 시도해주세요.")
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

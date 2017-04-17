package ga.findparty.findparty.profile;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;

import ga.findparty.findparty.BaseActivity;
import ga.findparty.findparty.Information;
import ga.findparty.findparty.R;
import ga.findparty.findparty.StartActivity;
import ga.findparty.findparty.util.AdditionalFunc;
import ga.findparty.findparty.util.OnAdapterSupport;
import ga.findparty.findparty.util.OnLoadMoreListener;
import ga.findparty.findparty.util.ParsePHP;

public class RecommendUserListActivity extends BaseActivity  implements OnAdapterSupport {

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;
    private final int MSG_MESSAGE_MAKE_ENDLESS_LIST = 501;
    private final int MSG_MESSAGE_PROGRESS_HIDE = 502;
    private final int MSG_MESSAGE_SAVE_SUCCESS = 503;
    private final int MSG_MESSAGE_SAVE_FAIL = 504;

    private AVLoadingIndicatorView loading;
    private MaterialDialog progressDialog;

    private FrameLayout root;
    private TextView tv_msg;

    private String field;
    private String recipientId;
    private String userId;
    private int page = 0;
    private ArrayList<HashMap<String, String>> tempList;
    private ArrayList<HashMap<String, String>> list;

    private boolean isUserMode;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private RecommendUserListCustomAdapter adapter;
    private boolean isLoadFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend_user_list);

        Intent intent = getIntent();
        isUserMode = intent.getBooleanExtra("isUserMode", false);
        if(isUserMode){
            userId = intent.getStringExtra("userId");
        }else {
            field = intent.getStringExtra("field");
            recipientId = intent.getStringExtra("recipientId");
        }

        init();

    }

    void init(){

        root = (FrameLayout)findViewById(R.id.root);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecommendUserListActivity.super.onBackPressed();
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
            if(isUserMode){
                map.put("service", "getUserRecommendList");
                map.put("userId", userId);
            }else {
                map.put("service", "getRecommendList");
                map.put("recipientId", recipientId);
                map.put("field", field);
            }
            map.put("page", Integer.toString(page));


            new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {

                @Override
                protected void afterThreadFinish(String data) {

                    if (page <= 0) {
                        list.clear();

                        if(isUserMode){
                            list = AdditionalFunc.getUserRecommendList(data);
                        }else {
                            list = AdditionalFunc.getRecommendList(data);
                        }

                        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));
                    } else {

                        tempList.clear();
                        if(isUserMode){
                            tempList = AdditionalFunc.getUserRecommendList(data);
                        }else {
                            tempList = AdditionalFunc.getRecommendList(data);
                        }
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

        adapter = new RecommendUserListCustomAdapter(getApplicationContext(), list, rv, this, this);

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

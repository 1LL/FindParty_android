package ga.findparty.findparty.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
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

public class CourseBoardActivity extends BaseActivity implements OnAdapterSupport{

    public final static int UPDATE_COURSE_BOARD = 100;

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;
    private final int MSG_MESSAGE_MAKE_ENDLESS_LIST = 501;
    private final int MSG_MESSAGE_PROGRESS_HIDE = 502;
    private final int MSG_MESSAGE_SHOW_LOADING = 503;

    private Toolbar toolbar;
    private AVLoadingIndicatorView loading;
    private MaterialDialog progressDialog;
    private TextView tv_msg;

    // FAB
    private FloatingActionsMenu menu;
    private FloatingActionButton searchBtn;
    private FloatingActionButton addBtn;

    private String courseId;
    private String courseTitle;
    private int page = 0;
    private String search;
    private ArrayList<HashMap<String, Object>> tempList;
    private ArrayList<HashMap<String, Object>> list;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private BoardListCustomAdapter adapter;
    private boolean isLoadFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_board);

        Intent intent = getIntent();
        courseId = intent.getStringExtra("courseId");
        courseTitle = intent.getStringExtra("title");

        init();

    }

    void init(){

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        ((TextView)findViewById(R.id.toolbar_course_title)).setText(courseTitle);

        tv_msg = (TextView)findViewById(R.id.tv_msg);
        tv_msg.setVisibility(View.GONE);

        setFab();

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

        getBoardList();

    }

    private void setFab(){

        menu = (FloatingActionsMenu)findViewById(R.id.multiple_actions);
        FloatingActionButton gotoUp = (FloatingActionButton)findViewById(R.id.gotoUp);
        gotoUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rv != null){
                    rv.smoothScrollToPosition(0);
                }
                menu.toggle();
            }
        });
        gotoUp.setTitle("맨위로");

        searchBtn = (FloatingActionButton) findViewById(R.id.search);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(CourseBoardActivity.this)
                        .title("검색")
                        .inputType(InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                                InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                        .theme(Theme.LIGHT)
                        .positiveText("검색")
                        .negativeText("취소")
                        .neutralText("초기화")
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                search = "";
                                initLoadValue();
                                progressDialog.show();
                                getBoardList();
                            }
                        })
                        .input("검색어를 입력해주세요", search, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                search = input.toString();
                                initLoadValue();
                                progressDialog.show();
                                getBoardList();
                            }
                        })
                        .show();
                menu.toggle();
            }
        });
        searchBtn.setTitle("검색");

        addBtn = (FloatingActionButton) findViewById(R.id.add);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddCourseBoardActivity.class);
                intent.putExtra("courseId", courseId);
                startActivityForResult(intent, UPDATE_COURSE_BOARD);
                menu.toggle();
            }
        });
        addBtn.setTitle("모집 글쓰기");

    }


    private void initLoadValue(){
        page = 0;
        isLoadFinish = false;
    }

    private void getBoardList(){
        if(!isLoadFinish) {
            handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SHOW_LOADING));

            HashMap<String, String> map = new HashMap<>();
            map.put("service", "getBoardList");
            map.put("courseId", courseId);
            map.put("page", Integer.toString(page));
            if (search != null && (!"".equals(search))) {
                map.put("search", search);
            }
            new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {

                @Override
                protected void afterThreadFinish(String data) {

                    if (page <= 0) {
                        list.clear();

                        list = AdditionalFunc.getBoardListInfo(data);

                        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));
                    } else {

                        tempList.clear();
                        tempList = AdditionalFunc.getBoardListInfo(data);
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

        if(list.size()>0){
            tv_msg.setVisibility(View.GONE);
        }else{
            tv_msg.setVisibility(View.VISIBLE);
        }

        adapter = new BoardListCustomAdapter(getApplicationContext(), list, rv, this, this);

        rv.setAdapter(adapter);

        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                page+=1;
                getBoardList();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case UPDATE_COURSE_BOARD:
                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SHOW_LOADING));
                initLoadValue();
                search = "";
                getBoardList();
                break;
            default:
                break;
        }
    }

    @Override
    public void showView() {
        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }

    @Override
    public void hideView() {
        toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
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
                case MSG_MESSAGE_SHOW_LOADING:
                    loading.show();
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

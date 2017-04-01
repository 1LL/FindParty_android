package ga.findparty.findparty.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

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
import ga.findparty.findparty.fragment.MyClassFragment;
import ga.findparty.findparty.util.AdditionalFunc;
import ga.findparty.findparty.util.OnAdapterSupport;
import ga.findparty.findparty.util.OnLoadMoreListener;
import ga.findparty.findparty.util.ParsePHP;

public class AddCourseActivity extends BaseActivity implements OnAdapterSupport{

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;
    private final int MSG_MESSAGE_MAKE_ENDLESS_LIST = 501;
    private final int MSG_MESSAGE_PROGRESS_HIDE = 502;
    private final int MSG_MESSAGE_SAVE_SUCCESS = 503;
    private final int MSG_MESSAGE_SAVE_FAIL = 504;

    private Toolbar toolbar;
    private AVLoadingIndicatorView loading;
    private MaterialDialog progressDialog;

    // FAB
    private FloatingActionsMenu menu;
    private FloatingActionButton searchBtn;

    private int page = 0;
    private String search;
    private ArrayList<HashMap<String, String>> tempList;
    private ArrayList<HashMap<String, String>> list;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private CourseListCustomAdapter adapter;
    private boolean isLoadFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        init();

    }

    void init(){

        toolbar = (Toolbar) findViewById(R.id.toolbar);

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

        getCourseList();

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
                new MaterialDialog.Builder(AddCourseActivity.this)
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
                                getCourseList();
                            }
                        })
                        .input("수업명을 입력해주세요", search, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                search = input.toString();
                                initLoadValue();
                                progressDialog.show();
                                getCourseList();
                            }
                        })
                        .show();
                menu.toggle();
            }
        });
        searchBtn.setTitle("검색");

    }

    private void initLoadValue(){
        page = 0;
        isLoadFinish = false;
    }

    private void getCourseList(){
        if(!isLoadFinish) {
            loading.show();
            HashMap<String, String> map = new HashMap<>();
            map.put("service", "getCourseList");
            map.put("school", StartActivity.USER_SCHOOL);
            map.put("page", Integer.toString(page));
            if (search != null && (!"".equals(search))) {
                map.put("search", search);
            }
            new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {

                @Override
                protected void afterThreadFinish(String data) {

                    if (page <= 0) {
                        list.clear();

                        list = AdditionalFunc.getCourseInfo(data);

                        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));
                    } else {

                        tempList.clear();
                        tempList = AdditionalFunc.getCourseInfo(data);
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

        adapter = new CourseListCustomAdapter(getApplicationContext(), list, rv, this, this);

        rv.setAdapter(adapter);

        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                page+=1;
                getCourseList();
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

    }

    private void addCourse(String id){

        progressDialog.show();

        HashMap<String, String> map = new HashMap<>();
        map.put("service", "saveUserCourse");
        map.put("school", StartActivity.USER_SCHOOL);
        map.put("userId", StartActivity.USER_ID);
        map.put("courseId", id);

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

            @Override
            protected void afterThreadFinish(String data) {
                if("1".equals(data)){
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SAVE_SUCCESS));
                }else{
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SAVE_FAIL));
                }
            }
        }.start();

    }

    public void checkAddCourse(HashMap<String, String> item){

        final String id = item.get("id");
        String department = item.get("department");
        String no = item.get("no");
        String _class = item.get("class");
        String title = item.get("title");
        String classification = item.get("classification");
        String day = item.get("day");
        String room = item.get("room");
        String lecturer = item.get("lecturer");

        final String text = "개설학과 : " + department + "\n" +
                "학수번호 : " + no + "\n" +
                "분반 : " + _class + "\n" +
                "수업명 : " + title + "\n" +
                "이수구분 : " + classification + "\n" +
                "요일 및 강의시간 : " + day + "\n" +
                "강의실 : " + room + "\n" +
                "교수 : " + lecturer;

        new MaterialDialog.Builder(this)
                .title("확인")
                .content(text)
                .theme(Theme.LIGHT)
                .positiveText("추가")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        addCourse(id);
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
                case MSG_MESSAGE_SAVE_SUCCESS:
                    progressDialog.hide();
                    loading.hide();
                    setResult(MyClassFragment.ADD_COURSE);
                    new MaterialDialog.Builder(AddCourseActivity.this)
                            .title("안내")
                            .content("수업을 정삭적으로 추가하였습니다.")
                            .theme(Theme.LIGHT)
                            .positiveText("확인")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    break;
                case MSG_MESSAGE_SAVE_FAIL:
                    progressDialog.hide();
                    loading.hide();
                    new MaterialDialog.Builder(AddCourseActivity.this)
                            .title("안내")
                            .content("수업을 정삭적으로 추가하지 못했습니다. 잠시 후 다시 시도해주세요.")
                            .theme(Theme.LIGHT)
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

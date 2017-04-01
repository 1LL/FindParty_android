package ga.findparty.findparty.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;

import ga.findparty.findparty.Information;
import ga.findparty.findparty.R;
import ga.findparty.findparty.StartActivity;
import ga.findparty.findparty.activity.AddCourseActivity;
import ga.findparty.findparty.util.AdditionalFunc;
import ga.findparty.findparty.util.DividerItemDecoration;
import ga.findparty.findparty.util.OnAdapterSupport;
import ga.findparty.findparty.util.OnLoadMoreListener;
import ga.findparty.findparty.util.ParsePHP;

public class MyClassFragment extends BaseFragment implements OnAdapterSupport {

    public final static int ADD_COURSE = 100;

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;
    private final int MSG_MESSAGE_SHOW_LOADING = 501;
    private final int MSG_MESSAGE_PROGRESS_HIDE = 502;

    private AVLoadingIndicatorView loading;
    private MaterialDialog progressDialog;

    // UI
    private View view;
    private Context context;
    private FloatingActionButton addBtn;
    private TextView tv_msg;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private UserCourseListCustomAdapter adapter;

    private ArrayList<HashMap<String, String>> list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_my_class, container, false);
        context = container.getContext();

        init();

        getUserCourseList();

        return view;
    }

    private void init(){

        tv_msg = (TextView)view.findViewById(R.id.tv_msg);
        tv_msg.setVisibility(View.GONE);

        addBtn = (FloatingActionButton)view.findViewById(R.id.fab_add);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddCourseActivity.class);
                getActivity().startActivityForResult(intent, ADD_COURSE);
            }
        });
        addBtn.setTitle("수업 추가");

        mLinearLayoutManager = new LinearLayoutManager(context);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) view.findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(mLinearLayoutManager);
        rv.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST));

        list = new ArrayList<>();

        loading = (AVLoadingIndicatorView)view.findViewById(R.id.loading);
        progressDialog = new MaterialDialog.Builder(context)
                .content("잠시만 기다려주세요.")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .theme(Theme.LIGHT)
                .build();

    }

    private void makeList(){

        if(list.size()>0){
            tv_msg.setVisibility(View.GONE);
        }else{
            tv_msg.setVisibility(View.VISIBLE);
        }

        adapter = new UserCourseListCustomAdapter(context, list, rv, this, this);

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();

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
                case MSG_MESSAGE_SHOW_LOADING:
                    loading.show();
                    break;
                case MSG_MESSAGE_PROGRESS_HIDE:
                    progressDialog.hide();
                    break;
                default:
                    break;
            }
        }
    }

    public void removeUserCourse(String id, final int position){

        HashMap<String, String> map = new HashMap<>();
        map.put("service", "deleteUserCourse");
        map.put("id", id);

        progressDialog.show();
        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

            @Override
            protected void afterThreadFinish(String data) {
                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_PROGRESS_HIDE));
                if("1".equals(data)){
                    list.remove(position);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyItemRemoved(position);
                        }
                    });
                }else{
                    new MaterialDialog.Builder(context)
                            .title("오류")
                            .content("수업을 정상적으로 삭제하지 못했습니다. 잠시 후 다시 시도해주세요.")
                            .positiveText("확인")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .theme(Theme.LIGHT)
                            .show();
                }
            }
        }.start();

    }

    public void checkRemoveUserCourse(final String id, String title, final int position){

        new MaterialDialog.Builder(context)
                .title("확인")
                .content(title + " 수업을 삭제하시겠습니까?")
                .positiveText("삭제")
                .negativeText("취소")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        removeUserCourse(id, position);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .theme(Theme.DARK)
                .show();

    }

    public void getUserCourseList(){

        HashMap<String, String> map = new HashMap<>();
        map.put("service", "getUserCourse");
        map.put("school", StartActivity.USER_SCHOOL);
        map.put("userId", StartActivity.USER_ID);

        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SHOW_LOADING));

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

            @Override
            protected void afterThreadFinish(String data) {
                list.clear();
                list = AdditionalFunc.getUserCourseInfo(data);
                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));
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

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}

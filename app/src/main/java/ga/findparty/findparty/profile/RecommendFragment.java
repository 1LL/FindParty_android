package ga.findparty.findparty.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import ga.findparty.findparty.Information;
import ga.findparty.findparty.R;
import ga.findparty.findparty.StartActivity;
import ga.findparty.findparty.activity.AddCourseActivity;
import ga.findparty.findparty.fragment.BaseFragment;
import ga.findparty.findparty.fragment.UserCourseListCustomAdapter;
import ga.findparty.findparty.util.AdditionalFunc;
import ga.findparty.findparty.util.DividerItemDecoration;
import ga.findparty.findparty.util.OnAdapterSupport;
import ga.findparty.findparty.util.ParsePHP;

public class RecommendFragment extends BaseFragment implements OnAdapterSupport {


    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;
    private final int MSG_MESSAGE_MAKE_LIST_UPDATE = 503;
    private final int MSG_MESSAGE_SHOW_LOADING = 501;
    private final int MSG_MESSAGE_PROGRESS_HIDE = 502;

    private AVLoadingIndicatorView loading;
    private MaterialDialog progressDialog;

    // UI
    private View view;
    private Context context;
    private TextView tv_msg;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private RecommendCustomAdapter adapter;

    private String recipientId;
    private ArrayList<HashMap<String, Object>> list;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        recipientId = getArguments().getString("recipientId");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_recommend, container, false);
        context = container.getContext();

        init();

        getRecommendedList(false);

        return view;
    }

    private void init(){

        tv_msg = (TextView)view.findViewById(R.id.tv_msg);
        tv_msg.setVisibility(View.GONE);

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

    public String getRecipientId(){
        return recipientId;
    }

    private void makeList(){

        if(list.size()>0){
            tv_msg.setVisibility(View.GONE);
        }else{
            tv_msg.setVisibility(View.VISIBLE);
        }

        adapter = new RecommendCustomAdapter(context, list, rv, this, this);

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
                case MSG_MESSAGE_MAKE_LIST_UPDATE:
                    loading.hide();
                    adapter.notifyDataSetChanged();
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

    public void getRecommendedList(final boolean isUpdate){

        HashMap<String, String> map = new HashMap<>();
        map.put("service", "getRecommendedList");
        map.put("recipientId", recipientId);

        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SHOW_LOADING));

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

            @Override
            protected void afterThreadFinish(String data) {

                try {
                    // PHP에서 받아온 JSON 데이터를 JSON오브젝트로 변환
                    JSONObject jObject = new JSONObject(data);
                    // results라는 key는 JSON배열로 되어있다.
                    JSONArray results = jObject.getJSONArray("result");
                    String countTemp = (String) jObject.get("num_result");
                    int count = Integer.parseInt(countTemp);

                    //recommendedList.clear();
                    list.clear();

                    for (int i = 0; i < count; ++i) {
                        JSONObject temp = results.getJSONObject(i);
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("field", (String) temp.get("field"));
                        map.put("count", Integer.parseInt((String) temp.get("count")));
                        list.add(map);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(isUpdate){
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST_UPDATE));
                }else{
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));
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

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}

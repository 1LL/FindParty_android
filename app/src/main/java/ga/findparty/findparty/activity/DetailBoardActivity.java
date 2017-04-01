package ga.findparty.findparty.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import ga.findparty.findparty.BaseActivity;
import ga.findparty.findparty.Information;
import ga.findparty.findparty.R;
import ga.findparty.findparty.StartActivity;
import ga.findparty.findparty.util.AdditionalFunc;
import ga.findparty.findparty.util.OnAdapterSupport;
import ga.findparty.findparty.util.OnLoadMoreListener;
import ga.findparty.findparty.util.ParsePHP;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class DetailBoardActivity extends BaseActivity implements OnAdapterSupport {

    public static final int UPDATE_APPLY_FORM = 100;

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_FILL_FORM = 500;
    private final int MSG_MESSAGE_MAKE_LIST = 501;

    private AVLoadingIndicatorView loadingContent;
    private AVLoadingIndicatorView loadingDuration;
    private AVLoadingIndicatorView loadingList;

    // UI
    private ImageView profileImage;
    private TextView tv_name;
    private TextView tv_email;
    private TextView tv_content;
    private TextView tv_interest;
    private TextView tv_duration;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private DetailBoardCustomAdapter adapter;


    private String boardId;
    private HashMap<String, Object> item;
    private ArrayList<HashMap<String, Object>> fieldList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_board);

        Intent intent = getIntent();
        boardId = intent.getStringExtra("boardId");

        fieldList = new ArrayList<>();

        init();

        getBoardList();
        getFieldList();

    }

    private void init(){

        profileImage = (ImageView)findViewById(R.id.profileImg);
        tv_name = (TextView)findViewById(R.id.tv_name);
        tv_email = (TextView)findViewById(R.id.tv_email);
        tv_content = (TextView)findViewById(R.id.tv_content);
        tv_interest = (TextView)findViewById(R.id.tv_interest);
        tv_duration = (TextView)findViewById(R.id.tv_duration);

        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(mLinearLayoutManager);

        loadingContent = (AVLoadingIndicatorView)findViewById(R.id.loading_content);
        loadingDuration = (AVLoadingIndicatorView)findViewById(R.id.loading_duration);
        loadingList = (AVLoadingIndicatorView)findViewById(R.id.loading_list);

        loadingContent.show();
        loadingDuration.show();
        loadingList.show();

    }

    public void makeList(){

        adapter = new DetailBoardCustomAdapter(getApplicationContext(), fieldList, rv, this, this);

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    private void fillForm(){

        Picasso.with(getApplicationContext())
                .load((String)item.get("img"))
                .transform(new CropCircleTransformation())
                .into(profileImage);
        tv_name.setText((String)item.get("name"));
        tv_email.setText((String)item.get("email"));
        tv_content.setText((String)item.get("content"));
        tv_interest.setText(AdditionalFunc.interestListToString((ArrayList<String>)item.get("interest")));

        String startText = AdditionalFunc.getDateString((Long)item.get("start"));
        String finishText = AdditionalFunc.getDateString((Long)item.get("finish"));
        tv_duration.setText(startText + " ~ " + finishText);

    }

    public void applyField(HashMap<String, Object> map){

        String userId = (String)item.get("userId");

        if(StartActivity.USER_ID.equals(userId)){
            new MaterialDialog.Builder(this)
                    .title("경고")
                    .content("본인 글에는 지원할 수 없습니다.")
                    .positiveText("확인")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }else{
            Intent intent = new Intent(getApplicationContext(), ApplyFormActivity.class);
            intent.putExtra("field", (String)map.get("field"));
            startActivityForResult(intent, UPDATE_APPLY_FORM);
        }

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

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_FILL_FORM:
                    fillForm();
                    loadingContent.hide();
                    loadingDuration.hide();
                    break;
                case MSG_MESSAGE_MAKE_LIST:
                    makeList();
                    loadingList.hide();
                    break;
                default:
                    break;
            }
        }
    }

    private void getBoardList(){

        HashMap<String, String> map = new HashMap<>();
        map.put("service", "getBoardList");
        map.put("id", boardId);

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {

            @Override
            protected void afterThreadFinish(String data) {

                item = AdditionalFunc.getBoardListInfo(data).get(0);
                if(item.size() > 0) {
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_FILL_FORM));
                }else{

                }

            }
        }.start();

    }

    private void getFieldList(){

        HashMap<String, String> map = new HashMap<>();
        map.put("service", "getBoardFieldList");
        map.put("boardId", boardId);

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {

            @Override
            protected void afterThreadFinish(String data) {

                fieldList = AdditionalFunc.getBoardFieldListInfo(data);
                if(fieldList.size() > 0) {
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));
                }else{

                }

            }
        }.start();

    }

}

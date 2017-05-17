package ga.findparty.findparty.profile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;

import ga.findparty.findparty.BaseActivity;
import ga.findparty.findparty.R;
import ga.findparty.findparty.util.DividerItemDecoration;
import ga.findparty.findparty.util.OnAdapterSupport;
import ga.findparty.findparty.util.OnLoadMoreListener;

public class ReviewUserListActivity extends BaseActivity implements OnAdapterSupport {


    private FrameLayout root;
    private Button closeBtn;
    //private AVLoadingIndicatorView loading;
    private TextView tv_msg;


    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private ReviewUserListCustomAdapter adapter;

    private HashMap<String, Object> ratingList;
    private ArrayList<HashMap<String, Object>> reviewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_user_list);

        Intent intent = getIntent();

        reviewList = (ArrayList<HashMap<String,Object>>)intent.getSerializableExtra("list");
        ratingList = (HashMap<String, Object>)intent.getSerializableExtra("question");

        init();

    }

    private void init(){


        root = (FrameLayout)findViewById(R.id.root);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReviewUserListActivity.super.onBackPressed();
            }
        });
        closeBtn = (Button)findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReviewUserListActivity.super.onBackPressed();
            }
        });

        //loading = (AVLoadingIndicatorView)findViewById(R.id.loading);

        tv_msg = (TextView)findViewById(R.id.tv_msg);
        tv_msg.setVisibility(View.GONE);

        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(mLinearLayoutManager);
        rv.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL_LIST));

        makeList();

    }

    public void makeList(){

        if(reviewList.size() > 0){
            tv_msg.setVisibility(View.GONE);
        }else{
            tv_msg.setVisibility(View.VISIBLE);
        }

        adapter = new ReviewUserListCustomAdapter(getApplicationContext(), reviewList, rv, this, this);

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    public HashMap<String, Object> getRatingList(){
        return ratingList;
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
}

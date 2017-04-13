package ga.findparty.findparty.profile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import ga.findparty.findparty.R;

public class RecommendListActivity extends AppCompatActivity {


    private FrameLayout root;
    private Button closeBtn;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend_list);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        init();

    }

    private void init(){

        root = (FrameLayout)findViewById(R.id.root);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecommendListActivity.super.onBackPressed();
            }
        });
        closeBtn = (Button)findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecommendListActivity.super.onBackPressed();
            }
        });



    }

}

package ga.findparty.findparty.profile;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import ga.findparty.findparty.BaseActivity;
import ga.findparty.findparty.R;

public class ReviewDetailActivity extends BaseActivity {

    private FrameLayout root;
    private Button closeBtn;
    private LinearLayout li_listField;

    private ArrayList<String[]> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_detail);

        data = (ArrayList<String[]>)getIntent().getSerializableExtra("data");

        init();
    }

    private void init(){

        root = (FrameLayout)findViewById(R.id.root);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReviewDetailActivity.super.onBackPressed();
            }
        });
        closeBtn = (Button)findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReviewDetailActivity.super.onBackPressed();
            }
        });

        li_listField = (LinearLayout)findViewById(R.id.li_list_field);

        makeList();

    }

    private void makeList(){



    }

}

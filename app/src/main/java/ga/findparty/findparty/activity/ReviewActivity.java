package ga.findparty.findparty.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

import ga.findparty.findparty.R;

public class ReviewActivity extends AppCompatActivity {

    private LinearLayout li_listField;
    private Button submitBtn;

    private ArrayList<String> ratingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        ratingList = new ArrayList<>();

        init();

    }

    private void init(){

        li_listField = (LinearLayout)findViewById(R.id.li_list_field);
        submitBtn = (Button)findViewById(R.id.submit);

    }

}

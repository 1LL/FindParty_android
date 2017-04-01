package ga.findparty.findparty.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

import ga.findparty.findparty.BaseActivity;
import ga.findparty.findparty.R;

public class ApplyFormActivity extends BaseActivity {

    private RelativeLayout root;
    private TextView tv_field;
    private ImageView start1;
    private ImageView start2;
    private ImageView start3;
    private TextView timeBtn;
    private MaterialEditText editContent;
    private Button applyBtn;

    private String field;
    private int level=0;
    private String impossibleTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_form);

        Intent intent = getIntent();
        field = intent.getStringExtra("field");

        init();

    }

    private void init(){

        root = (RelativeLayout)findViewById(R.id.activity_apply_form);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApplyFormActivity.super.onBackPressed();
            }
        });

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tag = (int)v.getTag();
                switch (tag){
                    case 1:
                        level = 1;
                        start1.setImageResource(R.drawable.star_yellow);
                        start2.setImageResource(R.drawable.star_black);
                        start3.setImageResource(R.drawable.star_black);
                        break;
                    case 2:
                        level = 2;
                        start1.setImageResource(R.drawable.star_yellow);
                        start2.setImageResource(R.drawable.star_yellow);
                        start3.setImageResource(R.drawable.star_black);
                        break;
                    case 3:
                        level = 3;
                        start1.setImageResource(R.drawable.star_yellow);
                        start2.setImageResource(R.drawable.star_yellow);
                        start3.setImageResource(R.drawable.star_yellow);
                        break;
                }
                checkApplyable();
            }
        };

        tv_field = (TextView)findViewById(R.id.tv_field);
        tv_field.setText(field);
        start1 = (ImageView)findViewById(R.id.star1);
        start1.setTag(1);
        start1.setOnClickListener(listener);
        start2 = (ImageView)findViewById(R.id.star2);
        start2.setTag(2);
        start2.setOnClickListener(listener);
        start3 = (ImageView)findViewById(R.id.star3);
        start3.setTag(3);
        start3.setOnClickListener(listener);
        timeBtn = (TextView)findViewById(R.id.time_btn);
        timeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        editContent = (MaterialEditText)findViewById(R.id.edit_content);
        editContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkApplyable();
            }
        });
        applyBtn = (Button)findViewById(R.id.applyBtn);
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        checkApplyable();

    }



    private void checkApplyable(){

        boolean isLevel = level > 0;
        boolean isContent = editContent.isCharactersCountValid();

        boolean setting = isLevel && isContent;

        applyBtn.setEnabled(setting);
        if(setting){
            applyBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        }else {
            applyBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
        }

    }

}

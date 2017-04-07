package ga.findparty.findparty.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import ga.findparty.findparty.BaseActivity;
import ga.findparty.findparty.Information;
import ga.findparty.findparty.R;
import ga.findparty.findparty.StartActivity;
import ga.findparty.findparty.util.AdditionalFunc;
import ga.findparty.findparty.util.ParsePHP;

public class ApplyFormActivity extends BaseActivity {

    public static final int UPDATE_TIME = 100;

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_FINISH = 500;
    private final int MSG_MESSAGE_ERROR = 501;

    private MaterialDialog progressDialog;

    private RelativeLayout root;
    private TextView tv_field;
    private ImageView star1;
    private ImageView star2;
    private ImageView star3;
    private TextView timeBtn;
    private MaterialEditText editContent;
    private Button applyBtn;

    private HashMap<String, Object> item;
    private String courseId;
    private String field;
    private int skill=0;
    private String impossibleTime;

    private ArrayList<Integer> monList;
    private ArrayList<Integer> tueList;
    private ArrayList<Integer> wedList;
    private ArrayList<Integer> thuList;
    private ArrayList<Integer> friList;


    Comparator<Integer> compare = new Comparator<Integer>() {
        @Override public int compare(Integer lhs, Integer rhs) {
            return lhs.compareTo(rhs);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_form);

        monList = new ArrayList<>();
        tueList = new ArrayList<>();
        wedList = new ArrayList<>();
        thuList = new ArrayList<>();
        friList = new ArrayList<>();

        Intent intent = getIntent();
        item = (HashMap<String, Object>)intent.getSerializableExtra("item");
        field = (String)item.get("field");
        courseId = intent.getStringExtra("courseId");

        init();

    }

    private void init(){

        progressDialog = new MaterialDialog.Builder(this)
                .content("잠시만 기다려주세요.")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .theme(Theme.LIGHT)
                .build();

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
                        skill = 1;
                        star1.setImageResource(R.drawable.star_yellow);
                        star2.setImageResource(R.drawable.star_black);
                        star3.setImageResource(R.drawable.star_black);
                        break;
                    case 2:
                        skill = 2;
                        star1.setImageResource(R.drawable.star_yellow);
                        star2.setImageResource(R.drawable.star_yellow);
                        star3.setImageResource(R.drawable.star_black);
                        break;
                    case 3:
                        skill = 3;
                        star1.setImageResource(R.drawable.star_yellow);
                        star2.setImageResource(R.drawable.star_yellow);
                        star3.setImageResource(R.drawable.star_yellow);
                        break;
                }
                checkApplyable();
            }
        };

        tv_field = (TextView)findViewById(R.id.tv_field);
        tv_field.setText(field);
        star1 = (ImageView)findViewById(R.id.star1);
        star1.setTag(1);
        star1.setOnClickListener(listener);
        star2 = (ImageView)findViewById(R.id.star2);
        star2.setTag(2);
        star2.setOnClickListener(listener);
        star3 = (ImageView)findViewById(R.id.star3);
        star3.setTag(3);
        star3.setOnClickListener(listener);
        timeBtn = (TextView)findViewById(R.id.time_btn);
        timeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ApplyFormActivity.this, SelectTimeActivity.class);
                intent.putIntegerArrayListExtra("mon", monList);
                intent.putIntegerArrayListExtra("tue", tueList);
                intent.putIntegerArrayListExtra("wed", wedList);
                intent.putIntegerArrayListExtra("thu", thuList);
                intent.putIntegerArrayListExtra("fri", friList);
                startActivityForResult(intent, UPDATE_TIME);
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
                apply();
            }
        });

        checkApplyable();

    }

    private void apply(){

        progressDialog.show();

        HashMap<String, String> map = new HashMap<>();
        map.put("service", "applyField");
        map.put("courseId", courseId);
        map.put("boardFieldId", (String)item.get("id"));
        map.put("userId", StartActivity.USER_ID);
        map.put("skill", skill+"");
        map.put("content", AdditionalFunc.replaceNewLineString(editContent.getText().toString()));
        map.put("mon", AdditionalFunc.integerArrayListToString(monList));
        map.put("tue", AdditionalFunc.integerArrayListToString(tueList));
        map.put("wed", AdditionalFunc.integerArrayListToString(wedList));
        map.put("thu", AdditionalFunc.integerArrayListToString(thuList));
        map.put("fri", AdditionalFunc.integerArrayListToString(friList));

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {

            @Override
            protected void afterThreadFinish(String data) {

                if("1".equals(data)) {
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_FINISH));
                }else{
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_ERROR));
                }

            }
        }.start();

    }

    private void checkApplyable(){

        boolean isLevel = skill > 0;
        boolean isContent = editContent.isCharactersCountValid();

        boolean setting = isLevel && isContent;

        applyBtn.setEnabled(setting);
        if(setting){
            applyBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        }else {
            applyBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
        }

    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_FINISH:
                    progressDialog.dismiss();
                    setResult(DetailBoardActivity.UPDATE_APPLY_FORM);
                    finish();
                    break;
                case MSG_MESSAGE_ERROR:
                    progressDialog.hide();
                    new MaterialDialog.Builder(ApplyFormActivity.this)
                            .title("오류")
                            .content("잠시 후 다시시도해주세요.")
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case UPDATE_TIME:
                monList = data.getIntegerArrayListExtra("mon");
                tueList = data.getIntegerArrayListExtra("tue");
                wedList = data.getIntegerArrayListExtra("wed");
                thuList = data.getIntegerArrayListExtra("thu");
                friList = data.getIntegerArrayListExtra("fri");
                Collections.sort(monList, compare);
                Collections.sort(tueList, compare);
                Collections.sort(wedList, compare);
                Collections.sort(thuList, compare);
                Collections.sort(friList, compare);
                timeBtn.setText("수정하기");
                break;
            default:
                break;
        }
    }

}

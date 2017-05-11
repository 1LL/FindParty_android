package ga.findparty.findparty.activity;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import ga.findparty.findparty.BaseActivity;
import ga.findparty.findparty.Information;
import ga.findparty.findparty.R;
import ga.findparty.findparty.StartActivity;
import ga.findparty.findparty.profile.SelectInterestActivity;
import ga.findparty.findparty.util.AdditionalFunc;
import ga.findparty.findparty.util.ParsePHP;

public class AddCourseBoardActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener {

    public static final int UPDATE_ADD_FIELD = 100;

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_SAVE_SUCCESS = 503;
    private final int MSG_MESSAGE_SAVE_FAIL = 504;
    private final int MSG_MESSAGE_SHOW_SAMPLE_QUESTION = 505;

    private Button submit;
    private MaterialEditText editContent;
    private TextView durationBtn;
    // UI - Interest Field
    private LinearLayout interestField;
    private TextView interestBtn;
    // UI - Add Field
    private LinearLayout li_addField;
    private TextView addFieldBtn;
    // UI - Question Field
    private LinearLayout li_questionField;
    private TextView addQuestionBtn;

    // DATA
    private String courseId;
    private Long start;
    private Long finish;
    private ArrayList<String> interest;
    private ArrayList<HashMap<String, String>> fieldList;
    private ArrayList<String> questionList;
    private String[] sampleQuestionList;

    private boolean isDuration = false;

    private MaterialDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course_board);

        Intent intent = getIntent();
        courseId = intent.getStringExtra("courseId");
        sampleQuestionList = new String[0];

        interest = new ArrayList<>();
        fieldList = new ArrayList<>();
        questionList = new ArrayList<>();

        init();

    }

    private void init(){

        submit = (Button)findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBoard();
            }
        });
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkAddable();
            }
        };
        editContent = (MaterialEditText)findViewById(R.id.edit_content);
        editContent.addTextChangedListener(textWatcher);
        durationBtn = (TextView)findViewById(R.id.duration_btn);
        durationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        AddCourseBoardActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.setThemeDark(true);
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });

        interestField = (LinearLayout)findViewById(R.id.interest_field);
        interestBtn = (TextView)findViewById(R.id.interest_btn);
        interestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectSelectActivity();
            }
        });

        li_addField = (LinearLayout)findViewById(R.id.li_add_field);
        addFieldBtn = (TextView)findViewById(R.id.add_field_btn);
        addFieldBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddCourseBoardActivity.this, AddFieldActivity.class);
                startActivityForResult(intent, UPDATE_ADD_FIELD);
            }
        });

        li_questionField = (LinearLayout)findViewById(R.id.li_question_field);
        addQuestionBtn = (TextView)findViewById(R.id.add_question_btn);
        addQuestionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(AddCourseBoardActivity.this)
                        .title("입력")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("질문을 입력해주세요.", null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                if(!"".equals(input.toString())) {
                                    questionList.add(AdditionalFunc.replaceNewLineString(input.toString()));
                                    makeQuestionLayout();
                                }
                            }
                        })
                        .neutralText("목록에서 추가")
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                getQuestionList();
                            }
                        })
                        .show();
            }
        });

        progressDialog = new MaterialDialog.Builder(this)
                .content("잠시만 기다려주세요.")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .theme(Theme.LIGHT)
                .build();

        checkAddable();

    }

    private void getQuestionList(){

        if(sampleQuestionList.length == 0){
            progressDialog.show();

            HashMap<String, String> map = new HashMap<>();
            map.put("service", "getQuestionList");

            new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {

                @Override
                protected void afterThreadFinish(String data) {

                    sampleQuestionList = AdditionalFunc.getSampleQuestionList(data);
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SHOW_SAMPLE_QUESTION));

                }
            }.start();
        }else{

            handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SHOW_SAMPLE_QUESTION));

        }


    }

    private void saveBoard(){

        progressDialog.show();

        String userId = getUserID(this);
        String content = editContent.getText().toString();
        content = AdditionalFunc.replaceNewLineString(content);
        String startText = start.toString();
        String finishText = finish.toString();
        String interestText = AdditionalFunc.arrayListToString(interest);
        String fieldText = AdditionalFunc.addFieldToString(fieldList);

        HashMap<String, String> map = new HashMap<>();
        map.put("service", "saveBoard");
        map.put("courseId", courseId);
        map.put("userId", userId);
        map.put("content", content);
        map.put("start", startText);
        map.put("finish", finishText);
        map.put("interest", interestText);
        map.put("boardField", fieldText);

        String question1 = "";
        String question2 = "";
        String question3 = "";

        for(int i=0; i<questionList.size(); i++){
            switch (i){
                case 0:
                    question1 = questionList.get(i);
                    break;
                case 1:
                    question2 = questionList.get(i);
                    break;
                case 2:
                    question3 = questionList.get(i);
                    break;
            }
        }
        map.put("question1", question1);
        map.put("question2", question2);
        map.put("question3", question3);

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {

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

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_SAVE_SUCCESS:
                    progressDialog.hide();
                    setResult(CourseBoardActivity.UPDATE_COURSE_BOARD);
                    new MaterialDialog.Builder(AddCourseBoardActivity.this)
                            .title("안내")
                            .content("수업을 정삭적으로 추가하였습니다.")
                            .theme(Theme.LIGHT)
                            .positiveText("확인")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .show();
                    break;
                case MSG_MESSAGE_SAVE_FAIL:
                    progressDialog.hide();
                    new MaterialDialog.Builder(AddCourseBoardActivity.this)
                            .title("안내")
                            .content("게시글을 정삭적으로 게시하지 못했습니다. 잠시 후 다시 시도해주세요.")
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
                case MSG_MESSAGE_SHOW_SAMPLE_QUESTION:
                    progressDialog.hide();
                    new MaterialDialog.Builder(AddCourseBoardActivity.this)
                            .title("질문 선택")
                            .items(sampleQuestionList)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    questionList.add(AdditionalFunc.replaceNewLineString(sampleQuestionList[which]));
                                    makeQuestionLayout();
                                }
                            })
                            .positiveText("닫기")
                            .show();
                    break;
                default:
                    break;
            }
        }
    }

    private void checkAddable(){

        boolean isContent = editContent.isCharactersCountValid();
        boolean isInterest = interest.size() >= 3;
        boolean isField = fieldList.size() > 0;

        boolean setting = isContent && isDuration && isInterest && isField;

        submit.setEnabled(setting);
        if(setting){
            submit.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        }else {
            submit.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
        }

    }

    private void setDateBtn(TextView tv, String text){

        tv.setText(text);
        tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
        tv.setTypeface(Typeface.DEFAULT);

    }

    private void makeQuestionLayout(){

        li_questionField.removeAllViews();

        for(int i=0; i<questionList.size(); i++){

            View v = getLayoutInflater().inflate(R.layout.add_field_custom_item, null, false);

            TextView tv_text = (TextView)v.findViewById(R.id.tv_text);
            tv_text.setText(questionList.get(i));
            tv_text.setSingleLine(false);
            ImageView deleteBtn = (ImageView)v.findViewById(R.id.delete_btn);
            deleteBtn.setTag(i);
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = (int)v.getTag();
                    questionList.remove(index);
                    makeQuestionLayout();
                }
            });

            li_questionField.addView(v);

        }

        if(questionList.size() >= 3){
            addQuestionBtn.setVisibility(View.GONE);
        }else{
            addQuestionBtn.setVisibility(View.VISIBLE);
        }

    }

    private void setAddFieldLayout(){

        li_addField.removeAllViews();

        for(int i=0; i<fieldList.size(); i++){
            HashMap<String, String> item = fieldList.get(i);

            String title = item.get("title");
            String number = item.get("number");
            String text = title + " " + number + "명";

            View v = getLayoutInflater().inflate(R.layout.add_field_custom_item, null, false);

            TextView tv_text = (TextView)v.findViewById(R.id.tv_text);
            tv_text.setText(text);
            ImageView deleteBtn = (ImageView)v.findViewById(R.id.delete_btn);
            deleteBtn.setTag(i);
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = (int)v.getTag();
                    fieldList.remove(index);
                    setAddFieldLayout();
                }
            });

//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            params.gravity = Gravity.CENTER;
//
//            TextView tv = new TextView(this);
//            tv.setText(text);
//            tv.setLayoutParams(params);
//            tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.profile_content));
//            tv.setGravity(Gravity.CENTER);

            li_addField.addView(v);

        }

    }

    private void setInterestField(){

        if(interest.size() <= 0){
            interestBtn.setVisibility(View.VISIBLE);
            return;
        }else{
            interestBtn.setVisibility(View.GONE);
        }

        interestField.removeAllViews();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpWidth -= 110;

        final float scale =getResources().getDisplayMetrics().density;
        int width = (int) (dpWidth * scale + 0.5f);
        int dp5 = (int) (5 * scale + 0.5f);
        int dp3 = (int) (3 * scale * 0.5f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(dp3, dp3, dp3, dp3);
        int mButtonsSize = 0;
        Rect bounds = new Rect();

        boolean isAdd = false;
        TextView finalText = new TextView(this);
        finalText.setPadding(dp5*2, dp5, dp5*2, dp5);
        finalText.setBackgroundResource(R.drawable.round_button_blue);
        finalText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        finalText.setText("+" + interest.size());
        finalText.setLayoutParams(params);
        finalText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectSelectActivity();
            }
        });

        for(int i=0; i<interest.size(); i++){

            int remainCount = interest.size() - i;
            String remainString = "+" + remainCount;
            finalText.setText(remainString);

            String s = interest.get(i);
            TextView mBtn = new TextView(this);
            mBtn.setPadding(dp5*2, dp5, dp5*2, dp5);
            mBtn.setBackgroundResource(R.drawable.round_button_blue_line);
            mBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.pastel_blue));
            mBtn.setText(s);
            mBtn.setLayoutParams(params);

            Paint textPaint = mBtn.getPaint();
            textPaint.getTextBounds(s, 0, s.length(), bounds);
            int textWidth = bounds.width() + dp5*4 + dp3*2;

            Rect tempBounds = new Rect();
            textPaint = finalText.getPaint();
            textPaint.getTextBounds(remainString, 0, remainString.length(), tempBounds);
            int remainWidth = tempBounds.width() + dp5*4 + dp3*2;

            if(mButtonsSize + textWidth + remainWidth < width){
                interestField.addView(mBtn);
                mButtonsSize += textWidth;
            }else{
                interestField.addView(finalText);
                isAdd = true;
                break;
            }

        }

        if(!isAdd){
            finalText.setText("+0");
            interestField.addView(finalText);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case 1:
                ArrayList<String> temp = (ArrayList<String>)data.getSerializableExtra("interest");

                boolean check = true;

                if(temp.size() == interest.size()) {
                    for (String s : temp) {
                        if (!interest.contains(s)) {
                            check = false;
                            break;
                        }
                    }
                }else{
                    check = false;
                }

                interest = temp;

                if(!check) {
                    showSnackbar("관심분야가 수정되었습니다.");
                    setInterestField();
                }

                checkAddable();
                break;
            case UPDATE_ADD_FIELD:
                HashMap<String, String> item = (HashMap<String, String>)data.getSerializableExtra("item");
                fieldList.add(item);
                setAddFieldLayout();
                checkAddable();
                break;
            default:
                break;
        }
    }

    private void redirectSelectActivity() {
        Intent intent = new Intent(this, SelectInterestActivity.class);
        intent.putStringArrayListExtra("interest", interest);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        isDuration = true;
        start = AdditionalFunc.getMilliseconds(year, monthOfYear+1, dayOfMonth);
        finish = AdditionalFunc.getMilliseconds(yearEnd, monthOfYearEnd+1, dayOfMonthEnd);

        String text = AdditionalFunc.getDateString(start) + "\n~ " + AdditionalFunc.getDateString(finish);
        setDateBtn(durationBtn, text);

        checkAddable();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}

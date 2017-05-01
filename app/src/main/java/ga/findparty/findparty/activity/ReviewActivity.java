package ga.findparty.findparty.activity;

import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;

import ga.findparty.findparty.BaseActivity;
import ga.findparty.findparty.Information;
import ga.findparty.findparty.R;
import ga.findparty.findparty.util.AdditionalFunc;
import ga.findparty.findparty.util.ParsePHP;

public class ReviewActivity extends BaseActivity {

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;

    private LinearLayout li_listField;
    private Button submitBtn;
    private AVLoadingIndicatorView loading;

    private ArrayList<HashMap<String, Object>> ratingList;
    private int[] answerIndexList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        ratingList = new ArrayList<>();
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("question", "첫번째 질문입니다.");
//        String s[] = {
//                "a. 첫번째 답",
//                "b. 두번째 답",
//                "c. 세번째 답"
//        };
//        map.put("answer", s);
//        ratingList.add(map);
//
//        map = new HashMap<>();
//        map.put("question", "두번째 질문입니다.");
//        String s2[] = {
//                "a. 첫번째 답",
//                "b. 두번째 답",
//                "c. 세번째 답"
//        };
//        map.put("answer", s2);
//        ratingList.add(map);

        init();

        getQAList();

    }

    private void init(){

        li_listField = (LinearLayout)findViewById(R.id.li_list_field);
        submitBtn = (Button)findViewById(R.id.submit);
        loading = (AVLoadingIndicatorView)findViewById(R.id.loading);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });

    }

    private void submitAble(){

        boolean able = true;

        for(int i=0; i<answerIndexList.length; i++){
            if(answerIndexList[i] < 0){
                able = false;
            }
        }

        submitBtn.setEnabled(able);
        if(able){
            submitBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        }else{
            submitBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
        }

    }

    private void selectAnswer(TextView tv, String ans){

        tv.setText(ans);
        tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));

    }

    private void getQAList(){

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("service", "getQAList");

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

            @Override
            protected void afterThreadFinish(String data) {

                ratingList = AdditionalFunc.getQAListItem(data);
                answerIndexList = new int[ratingList.size()];
                for(int i=0; i<answerIndexList.length; i++){
                    answerIndexList[i] = -1;
                }
                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));

            }
        }.start();

    }

    private void makeList(){

        li_listField.removeAllViews();

        for(int i=0; i<ratingList.size(); i++){
            HashMap<String, Object> map = ratingList.get(i);

            final int pos = i;
            final String question = (i+1) + ". " + map.get("question");
            final String[] answer = (String[])map.get("answer");

            View v = getLayoutInflater().inflate(R.layout.question_and_answer_2_custom_item, null, false);

            TextView tv_question = (TextView)v.findViewById(R.id.tv_question);
            tv_question.setText(question);

            final TextView tv_answer = (TextView)v.findViewById(R.id.tv_answer);

            tv_answer.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            tv_answer.setText("답변 선택");
            tv_answer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(ReviewActivity.this)
                            .title(question)
                            .items(answer)
                            .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                                @Override
                                public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    if(text != null) {
                                        String a = text.toString();
                                        selectAnswer(tv_answer, a);
                                        answerIndexList[pos] = which;
                                        submitAble();
                                    }
                                    return true;
                                }
                            })
                            .positiveText("확인")
                            .show();
                }
            });

            li_listField.addView(v);

        }

    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MESSAGE_MAKE_LIST:
                    loading.hide();
                    makeList();
                    break;
                default:
                    break;
            }
        }
    }

}

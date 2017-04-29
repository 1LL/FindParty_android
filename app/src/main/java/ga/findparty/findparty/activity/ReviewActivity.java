package ga.findparty.findparty.activity;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.HashMap;

import ga.findparty.findparty.BaseActivity;
import ga.findparty.findparty.R;

public class ReviewActivity extends BaseActivity {

    private LinearLayout li_listField;
    private Button submitBtn;

    private ArrayList<HashMap<String, Object>> ratingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        ratingList = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();
        map.put("question", "첫번째 질문입니다.");
        String s[] = {
                "a. 첫번째 답",
                "b. 두번째 답",
                "c. 세번째 답"
        };
        map.put("answer", s);
        ratingList.add(map);

        map = new HashMap<>();
        map.put("question", "두번째 질문입니다.");
        String s2[] = {
                "a. 첫번째 답",
                "b. 두번째 답",
                "c. 세번째 답"
        };
        map.put("answer", s2);
        ratingList.add(map);

        init();

    }

    private void init(){

        li_listField = (LinearLayout)findViewById(R.id.li_list_field);
        submitBtn = (Button)findViewById(R.id.submit);

        makeList();

    }

    private void selectAnswer(TextView tv, String ans){

        tv.setText(ans);
        tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));

    }

    private void makeList(){

        li_listField.removeAllViews();

        for(int i=0; i<ratingList.size(); i++){
            HashMap<String, Object> map = ratingList.get(i);

            final String question = (i+1) + ". " + map.get("question");
            final String[] answer = (String[])map.get("answer");

            View v = getLayoutInflater().inflate(R.layout.question_and_answer_custom_item, null, false);

            TextView tv_question = (TextView)v.findViewById(R.id.tv_question);
            tv_question.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
            tv_question.setText(question);
            tv_question.setTextSize(14);

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

}

package ga.findparty.findparty.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rey.material.widget.CompoundButton;
import com.rey.material.widget.RadioButton;

import java.util.HashMap;

import ga.findparty.findparty.R;
import ga.findparty.findparty.util.ReviewSelectListener;

public class ReviewItemFragment extends BaseFragment {

    // UI
    private View view;
    private Context context;
    private ReviewSelectListener selectListener;

    private TextView tv_question;
    private LinearLayout li_radio;
    private RadioButton[] rb_answer;

    private int position;
    private String question;
    private String[] answerList;
    private HashMap<String, Object> item;

    private boolean isSelect = false;
    private String answer;
    private int selectAnswerIndex = -1;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
        if(getArguments() != null) {
            position = getArguments().getInt("position");
            item = (HashMap<String, Object>)getArguments().getSerializable("item");
            question = (String)item.get("question");
            answerList = (String[])item.get("answer");
            selectListener = (ReviewSelectListener)getArguments().getSerializable("listener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_review_item, container, false);
        context = container.getContext();

        initUI();

        return view;
    }

    private void initUI(){

        tv_question = (TextView)view.findViewById(R.id.tv_question);
        li_radio = (LinearLayout) view.findViewById(R.id.li_radio);

        makeList();

    }

    private void makeList(){

        String qu = (position+1) + ". " + question;

        tv_question.setText(qu);
        rb_answer = new RadioButton[answerList.length];

        CompoundButton.OnCheckedChangeListener listener = new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    isSelect = true;
                    for(RadioButton rb : rb_answer){
                        rb.setChecked(rb == buttonView);
                        if(rb == buttonView){
                            int index = (int)rb.getTag();
                            answer = answerList[index];
                            selectAnswerIndex = index;
                            selectListener.select(position, answer, selectAnswerIndex);
                        }
                    }
                }
            }
        };

        String index = "가나다라마바사아자차카타파하";
        for(int i=0; i<answerList.length; i++){
            String a = index.charAt(i) + ". " + answerList[i];

            rb_answer[i] = new RadioButton(context);
            rb_answer[i].setTag(i);
            rb_answer[i].setText(a);
            rb_answer[i].setGravity(Gravity.CENTER_VERTICAL);
            rb_answer[i].setOnCheckedChangeListener(listener);
            rb_answer[i].setPadding(10, 10, 10, 10);

            li_radio.addView(rb_answer[i]);

        }

    }

    public boolean isSelect(){
        return isSelect;
    }
    public String getAnswer(){
        return answer;
    }
    public int getSelectAnswerIndex(){
        return selectAnswerIndex;
    }

}

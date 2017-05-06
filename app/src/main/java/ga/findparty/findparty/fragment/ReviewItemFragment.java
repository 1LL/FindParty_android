package ga.findparty.findparty.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;
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
    private MaterialEditText editText;
    private Button nextBtn;

    private int position;
    private String question;
    private String[] answerList;
    private HashMap<String, Object> item;

    private boolean isSelect = false;
    private String answer;
    private int selectAnswerIndex = -1;

    private boolean isEditMode;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
        if(getArguments() != null) {
            isEditMode = getArguments().getBoolean("isEditMode", false);
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
        editText = (MaterialEditText)view.findViewById(R.id.edit_text);
        nextBtn = (Button)view.findViewById(R.id.nextBtn);

        String qu = (position+1) + ". " + question;
        tv_question.setText(qu);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkNextEnable();
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectListener.select(position, getEditAnswer(), -1);
            }
        });

        if(isEditMode){
            editText.setVisibility(View.VISIBLE);
            nextBtn.setVisibility(View.VISIBLE);
        }else{
            editText.setVisibility(View.GONE);
            nextBtn.setVisibility(View.GONE);
            makeAnswerList();
        }

    }

    private void checkNextEnable(){

        if(editText.isCharactersCountValid()){
            nextBtn.setEnabled(true);
            nextBtn.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }else{
            nextBtn.setEnabled(false);
            nextBtn.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_gray));
        }

    }

    private void makeAnswerList(){

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
    public String getEditAnswer(){
        return editText.getText().toString();
    }

}

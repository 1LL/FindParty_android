package ga.findparty.findparty.profile;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;

import ga.findparty.findparty.Information;
import ga.findparty.findparty.R;
import ga.findparty.findparty.fragment.BaseFragment;
import ga.findparty.findparty.util.AdditionalFunc;
import ga.findparty.findparty.util.ParsePHP;

/**
 * Created by tw on 2017-04-05.
 */
public class ReviewFragment extends BaseFragment {

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;

    // BASIC UI
    private View view;
    private Context context;

    private AVLoadingIndicatorView loading;
    private TextView tv_msg;
    private LinearLayout li_listField;

    private ArrayList<HashMap<String, Object>> ratingList;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // resultCode = getArguments().getInt("code");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_review, container, false);
        context = container.getContext();

        initData();
        initUI();

        getQAList();

        return view;

    }

    private void initData(){


    }

    private void initUI(){

        loading = (AVLoadingIndicatorView)view.findViewById(R.id.loading);
        tv_msg = (TextView)view.findViewById(R.id.tv_msg);
        tv_msg.setVisibility(View.GONE);
        li_listField = (LinearLayout)view.findViewById(R.id.li_list_field);


    }

    private void makeList(){

        li_listField.removeAllViews();

        for(int i=0; i<ratingList.size(); i++){
            HashMap<String, Object> map = ratingList.get(i);

            String question = (i+1) + ". " + map.get("question");
            String answer = "";
            String[] answerList = (String[])map.get("answer");

            String index = "가나다라마바사아자차카타파하";
            for(int j=0; j<answerList.length; j++){
                String s = index.charAt(j) + ". " + answerList[j];
                if(j < answerList.length-1){
                    s += "\n";
                }
                answer += s;
            }

            View v = LayoutInflater.from(context).inflate(R.layout.question_and_answer_custom_item, null, false);

            TextView tv_question = (TextView)v.findViewById(R.id.tv_question);
            TextView tv_answer = (TextView)v.findViewById(R.id.tv_answer);

            tv_question.setText(question);
            tv_answer.setText(answer);

            tv_question.setTextColor(ContextCompat.getColor(context, R.color.dark_gray));
            tv_answer.setTextColor(ContextCompat.getColor(context, R.color.gray));

            tv_answer.setPadding(20, 0, 0, 0);

            li_listField.addView(v);
        }

    }

    private void getQAList(){

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("service", "getQAList");

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

            @Override
            protected void afterThreadFinish(String data) {

                ratingList = AdditionalFunc.getQAListItem(data);
                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));

            }
        }.start();

    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
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

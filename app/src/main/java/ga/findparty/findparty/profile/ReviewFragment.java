package ga.findparty.findparty.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final int MSG_MESSAGE_GET_REVIEW = 501;

    // BASIC UI
    private View view;
    private Context context;

    private AVLoadingIndicatorView loading;
    private TextView tv_msg;
    private RelativeLayout rl_detailBtn;
    private TextView tv_countMsg;
    private TextView tv_secretCountMsg;
    private LinearLayout li_listField;

    private String userId;
    private int secretCount=0;

    private HashMap<String, Object> originalRatingList;
    private HashMap<String, Object> ratingList;
    private ArrayList<HashMap<String, Object>> reviewList;
    private boolean isLoadFinish = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        userId = getArguments().getString("id");
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

        reviewList = new ArrayList<>();
        originalRatingList = new HashMap<>();

    }

    private void initUI(){

        loading = (AVLoadingIndicatorView)view.findViewById(R.id.loading);
        tv_msg = (TextView)view.findViewById(R.id.tv_msg);
        tv_msg.setVisibility(View.GONE);
        tv_secretCountMsg = (TextView)view.findViewById(R.id.tv_secret_count_msg);
        tv_secretCountMsg.setVisibility(View.GONE);
        rl_detailBtn = (RelativeLayout)view.findViewById(R.id.rl_detail_btn);
        tv_countMsg = (TextView)view.findViewById(R.id.tv_count_msg);
        tv_countMsg.setVisibility(View.GONE);
        li_listField = (LinearLayout)view.findViewById(R.id.li_list_field);

        rl_detailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isLoadFinish){
                    Intent intent = new Intent(context, ReviewUserListActivity.class);
                    intent.putExtra("list", reviewList);
                    intent.putExtra("question", originalRatingList);
                    startActivity(intent);
                }
            }
        });

    }

    private void makeList(){

        li_listField.removeAllViews();

        tv_countMsg.setText(String.format("현재 총 %d개의 평가가 있습니다.", reviewList.size()));
        tv_countMsg.setVisibility(View.VISIBLE);
        li_listField.addView(tv_countMsg);

        if(secretCount > 0){
            tv_secretCountMsg.setText(String.format("현재 %d개의 비공개 평가가 있습니다.", secretCount));
            tv_secretCountMsg.setVisibility(View.VISIBLE);
            li_listField.addView(tv_secretCountMsg);
        }

        String[] keyList = new String[ratingList.size()];
        ratingList.keySet().toArray(keyList);
        Arrays.sort(keyList);

        for(int i=0; i<keyList.length; i++){
            String key = keyList[i];
            HashMap<String, Object> map = (HashMap<String, Object>)ratingList.get(key);

            String question = (i+1) + ". " + map.get("question");
            String answer = "";
            String[] answerList = (String[])map.get("answer");
            int total = (int)map.get("total");
            int[] answerCount = (int[])map.get("answerCount");

            if(i != keyList.length-1) {
                String index = "가나다라마바사아자차카타파하";
                for (int j = 0; j < answerList.length; j++) {

                    double percentage = 0;
                    if (total > 0) {
                        percentage = ((double) answerCount[j] / total) * 100.0;
                    }
                    String s = String.format("%s. %s(%.1f%%)", index.charAt(j), answerList[j], percentage);
                    //String s = index.charAt(j) + ". " + answerList[j] + "(" + percentage + "%)";

                    if (j < answerList.length - 1) {
                        s += "\n";
                    }
                    answer += s;
                }
            }else{
                answer = "TODO";
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

    private void makeData(){

        secretCount = 0;

        for(HashMap<String, Object> h : reviewList){

            boolean isSecret = (boolean)h.get("isSecret");

            if(isSecret){
                secretCount += 1;
            }
            else {

                ArrayList<String[]> content = (ArrayList<String[]>) h.get("content");
                for (String[] data : content) {

                    String id = data[0];
                    int index = Integer.parseInt(data[1]);

                    if (ratingList.containsKey(id)) {

                        HashMap<String, Object> map = (HashMap<String, Object>) ratingList.get(id);

                        int total = (int) map.get("total");
                        int[] answerCount = (int[]) map.get("answerCount");

                        if (index < answerCount.length) {
                            answerCount[index] += 1;
                            total += 1;

                            map.put("total", total);
                            map.put("answerCount", answerCount);

                            ratingList.put(id, map);
                        }

                    }

                }
            }

        }

    }

    private void getQAList(){

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("service", "getQAList");

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

            @Override
            protected void afterThreadFinish(String data) {

                ratingList = AdditionalFunc.getQAHashItem(data);
                originalRatingList = (HashMap<String, Object>)ratingList.clone();
                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_GET_REVIEW));

            }
        }.start();

    }

    private void getUserReviewList(){

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("service", "getUserReviewList");
        map.put("userId", userId);

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

            @Override
            protected void afterThreadFinish(String data) {

                reviewList = AdditionalFunc.getUserReviewListItem(data);
                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));

            }
        }.start();

    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_GET_REVIEW:
                    getUserReviewList();
                    break;
                case MSG_MESSAGE_MAKE_LIST:
                    loading.hide();
                    makeData();
                    makeList();
                    isLoadFinish = true;
                    break;
                default:
                    break;
            }
        }
    }

}

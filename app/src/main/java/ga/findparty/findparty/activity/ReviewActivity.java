package ga.findparty.findparty.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.matthewtamlin.sliding_intro_screen_library.indicators.DotIndicator;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;

import ga.findparty.findparty.BaseActivity;
import ga.findparty.findparty.Information;
import ga.findparty.findparty.R;
import ga.findparty.findparty.fragment.ReviewItemFragment;
import ga.findparty.findparty.util.AdditionalFunc;
import ga.findparty.findparty.util.CustomViewPager;
import ga.findparty.findparty.util.ParsePHP;
import ga.findparty.findparty.util.ReviewSelectListener;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class ReviewActivity extends BaseActivity implements ReviewSelectListener{

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;
    private final int MSG_MESSAGE_SAVE_SUCCESS = 501;
    private final int MSG_MESSAGE_SAVE_FAIL = 502;

    private LinearLayout li_listField;
    private Button submitBtn;
    private AVLoadingIndicatorView loading;
    private MaterialDialog progressDialog;

    // UI
    private ImageView profileImage;
    private TextView tv_name;
    private TextView tv_email;
    private TextView tv_field;

    private DotIndicator dotIndicator;
    private CustomViewPager viewPager;
    private NavigationAdapter mPagerAdapter;
    private ReviewItemFragment[] reviewItemFragments;
    private int lastPage = 0;

    private ArrayList<HashMap<String, Object>> ratingList;
    private int[] answerIndexList;
    private boolean isSecret;

    private int position;
    private HashMap<String, Object> userItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        Intent intent = getIntent();
        userItem = (HashMap<String, Object>)intent.getSerializableExtra("item");
        position = intent.getIntExtra("position", -1);

        ratingList = new ArrayList<>();

        init();

        getQAList();

    }

    private void init(){

        progressDialog = new MaterialDialog.Builder(this)
                .content("잠시만 기다려주세요.")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .theme(Theme.LIGHT)
                .build();
//        li_listField = (LinearLayout)findViewById(R.id.li_list_field);
//        submitBtn = (Button)findViewById(R.id.submit);
        loading = (AVLoadingIndicatorView)findViewById(R.id.loading);

//        submitBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                checkSecret();
//            }
//        });

        profileImage = (ImageView)findViewById(R.id.profileImg);
        tv_name = (TextView)findViewById(R.id.tv_name);
        tv_email = (TextView)findViewById(R.id.tv_email);
        tv_field = (TextView)findViewById(R.id.tv_field);

        Picasso.with(getApplicationContext())
                .load((String)userItem.get("img"))
                .transform(new CropCircleTransformation())
                .into(profileImage);
        tv_name.setText((String)userItem.get("name"));
        tv_email.setText((String)userItem.get("email"));
        tv_field.setText((String)userItem.get("field"));

    }

    private void checkSecret(){

        String ans[] = {
                "당사자에게 보이게 하기",
                "당사자에게 안 보이게 하기"
        };

        new MaterialDialog.Builder(this)
                .title("해당 평가를 당사자에게 보이도록 하겠습니까?")
                .items(ans)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if(text != null) {
                            if(which == 0){
                                isSecret = false;
                            }else{
                                isSecret = true;
                            }
                            submitToServer();
                        }
                        return true;
                    }
                })
                .positiveText("확인")
                .show();

    }

    private void submitToServer(){

        progressDialog.show();

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("service", "saveReview");
        map.put("table", (String)userItem.get("table"));
        map.put("tableId", (String)userItem.get("tableId"));
        map.put("userId", getUserID(this));

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

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

        reviewItemFragments = new ReviewItemFragment[ratingList.size()+1];

        dotIndicator = (DotIndicator) findViewById(R.id.main_indicator_ad);
        dotIndicator.setSelectedDotColor(Color.parseColor("#FF4081"));
        dotIndicator.setUnselectedDotColor(Color.parseColor("#CFCFCF"));
        dotIndicator.setNumberOfItems(reviewItemFragments.length);
        dotIndicator.setSelectedItem(0, false);
        viewPager = (CustomViewPager) findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(reviewItemFragments.length);
        mPagerAdapter = new NavigationAdapter(getSupportFragmentManager(), ratingList, this);
        viewPager.setAdapter(mPagerAdapter);
        viewPager.setCurrentItem(0);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(position == lastPage+1){
                    viewPager.setCurrentItem(lastPage, true);
                }
            }

            @Override
            public void onPageSelected(int position) {
                dotIndicator.setSelectedItem(position, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

//        li_listField.removeAllViews();
//
//        for(int i=0; i<ratingList.size(); i++){
//            HashMap<String, Object> map = ratingList.get(i);
//
//            final int pos = i;
//            final String question = (i+1) + ". " + map.get("question");
//            final String[] answer = (String[])map.get("answer");
//
//            View v = getLayoutInflater().inflate(R.layout.question_and_answer_2_custom_item, null, false);
//
//            TextView tv_question = (TextView)v.findViewById(R.id.tv_question);
//            tv_question.setText(question);
//
//            final TextView tv_answer = (TextView)v.findViewById(R.id.tv_answer);
//
//            tv_answer.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
//            tv_answer.setText("답변 선택");
//            tv_answer.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    new MaterialDialog.Builder(ReviewActivity.this)
//                            .title(question)
//                            .items(answer)
//                            .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
//                                @Override
//                                public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
//                                    if(text != null) {
//                                        String a = text.toString();
//                                        selectAnswer(tv_answer, a);
//                                        answerIndexList[pos] = which;
//                                        submitAble();
//                                    }
//                                    return true;
//                                }
//                            })
//                            .positiveText("확인")
//                            .show();
//                }
//            });
//
//            li_listField.addView(v);
//
//        }

    }

    @Override
    public void select(final int fragmentPosition, String answer, int selectAnswerIndex, boolean force) {
        if(lastPage == fragmentPosition){
            showSnackbar((fragmentPosition+1) + "/" + ratingList.size() + " 완료");
            String ques = (fragmentPosition+1) +". " + ratingList.get(fragmentPosition).get("question");
            reviewItemFragments[ratingList.size()].addSubmitContent(ques, answer);

            Runnable runnable = new Runnable() {
                public void run() {
                    if (fragmentPosition == lastPage) {
                        lastPage += 1;
                    }
                    viewPager.setCurrentItem(fragmentPosition + 1, true);
                }
            };
            if(force) {
                handler.postDelayed(runnable, 0);
            }else{
                handler.postDelayed(runnable, 500);
            }
        }else{
            reviewItemFragments[ratingList.size()].setSubmitContent(fragmentPosition, answer);
            if(force){
                viewPager.setCurrentItem(fragmentPosition + 1, true);
            }
        }

    }

    @Override
    public void submit(){
        checkSecret();
    }

    private void activityFinish(){
        finish();
    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MESSAGE_MAKE_LIST:
                    loading.hide();
                    makeList();
                    break;
                case MSG_MESSAGE_SAVE_SUCCESS:
                    progressDialog.hide();
                    String review = (String)userItem.get("review");
                    review += (","+getUserID(ReviewActivity.this));
                    userItem.put("review", review);
                    Intent intent = new Intent();
                    intent.putExtra("position", position);
                    intent.putExtra("item", userItem);
                    setResult(ReviewListActivity.UPDATE_LIST, intent);
                    new MaterialDialog.Builder(ReviewActivity.this)
                            .title("완료")
                            .content("평가가 성공적으로 저장되었습니다.")
                            .positiveText("확인")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                    activityFinish();
                                }
                            })
                            .show();
                    break;
                case MSG_MESSAGE_SAVE_FAIL:
                    progressDialog.hide();
                    new MaterialDialog.Builder(ReviewActivity.this)
                            .title("오류")
                            .content("잠시 후 다시 시도해주세요.")
                            .positiveText("확인")
                            .show();
                    break;
                default:
                    break;
            }
        }
    }

    public void setReviewItemFragments(ReviewItemFragment f, int position){
        reviewItemFragments[position] = f;
    }
    public ReviewItemFragment getReviewItemFragment(int position){
        return reviewItemFragments[position];
    }

    private static class NavigationAdapter extends FragmentPagerAdapter {

        private int size;
        private ArrayList<HashMap<String, Object>> list;
        private ReviewActivity activity;

        public NavigationAdapter(FragmentManager fm, ArrayList<HashMap<String, Object>> list, ReviewActivity activity) {
            super(fm);
            this.list = list;
            this.size = list.size()+1;
            this.activity = activity;
        }

        @Override
        public Fragment getItem(int position) {

            final int pattern = position % size;

            ReviewItemFragment f;

            if(activity.getReviewItemFragment(pattern) == null){

                f = new ReviewItemFragment();
                Bundle bdl = new Bundle(1);

                if(pattern == size-1){
                    bdl.putInt("position", pattern);
                    //ReviewSelectListener listener = (ReviewSelectListener)activity;
                    bdl.putSerializable("listener", activity);
                    bdl.putBoolean("isSubmitMode", true);
                }else{
                    bdl.putInt("position", pattern);
                    bdl.putSerializable("item", list.get(pattern));
                    //ReviewSelectListener listener = (ReviewSelectListener)activity;
                    bdl.putSerializable("listener", activity);
                    if(pattern == size-2){
                        bdl.putBoolean("isEditMode", true);
                    }
                }

                f.setArguments(bdl);

                activity.setReviewItemFragments(f, pattern);

            }else {
                f = activity.getReviewItemFragment(pattern);
            }

            return f;
        }

        @Override
        public int getCount() {
            return size;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

}

package ga.findparty.findparty.activity;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import ga.findparty.findparty.BaseActivity;
import ga.findparty.findparty.R;
import ga.findparty.findparty.StartActivity;
import ga.findparty.findparty.fragment.MyReviewFragment;
import ga.findparty.findparty.profile.ProfileActivity;
import ga.findparty.findparty.util.AdditionalFunc;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class ReviewListActivity extends BaseActivity {

    public static final int UPDATE_LIST = 100;

    private LinearLayout li_participantList;

    private ArrayList<HashMap<String, Object>> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_list);

        Intent intent = getIntent();
        list = (ArrayList<HashMap<String, Object>>)intent.getSerializableExtra("list");

        init();

        makeList();

    }

    private void init(){

        li_participantList = (LinearLayout)findViewById(R.id.li_participant_list);

    }

    private void makeList(){

        li_participantList.removeAllViews();

        for(int i=0; i<list.size(); i++){
            final int pos = i;
            final HashMap<String, Object> map = list.get(i);

            final String userId = (String)map.get("userId");

            if(userId.equals(getUserID(this))){
                continue;
            }

            ArrayList<String> alreadyReviewList = AdditionalFunc.stringToArrayList((String)map.get("review"));

            View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.review_participant_list_custom_item, null, false);

            RelativeLayout rl_background = (RelativeLayout)v.findViewById(R.id.rl_background);
            RelativeLayout rl_profile = (RelativeLayout)v.findViewById(R.id.rl_profile);
            ImageView profileImg = (ImageView)v.findViewById(R.id.profileImg);
            TextView tv_name = (TextView)v.findViewById(R.id.tv_name);
            TextView tv_email = (TextView)v.findViewById(R.id.tv_email);
            TextView tv_field = (TextView)v.findViewById(R.id.tv_field);
            Button reviewBtn = (Button)v.findViewById(R.id.reviewBtn);

            rl_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ReviewListActivity.this, ProfileActivity.class);
                    intent.putExtra("id", userId);
                    startActivity(intent);
                }
            });
            Picasso.with(getApplicationContext())
                    .load((String)map.get("img"))
                    .transform(new CropCircleTransformation())
                    .into(profileImg);
            tv_name.setText((String)map.get("name"));
            tv_email.setText((String)map.get("email"));
            tv_field.setText((String)map.get("field"));

            reviewBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ReviewListActivity.this, ReviewActivity.class);
                    intent.putExtra("item", map);
                    intent.putExtra("position", pos);
                    startActivityForResult(intent, UPDATE_LIST);
                }
            });

            if(alreadyReviewList.contains(getUserID(this))){
                rl_background.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
                reviewBtn.setEnabled(false);
                reviewBtn.setText("완료");
            }else{
                rl_background.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                reviewBtn.setEnabled(true);
                reviewBtn.setText("평가하기");
            }

            li_participantList.addView(v);

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case UPDATE_LIST:
                int position = data.getIntExtra("position", -1);
                if(position >= 0){
                    HashMap<String, Object> item = (HashMap<String, Object>)data.getSerializableExtra("item");
                    list.set(position, item);
                    setResult(MyReviewFragment.UPDATE_LIST);
                    makeList();
                }
                break;
            default:
                break;
        }
    }

}

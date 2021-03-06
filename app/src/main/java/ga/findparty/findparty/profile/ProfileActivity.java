package ga.findparty.findparty.profile;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gigamole.navigationtabstrip.NavigationTabStrip;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.HashMap;

import ga.findparty.findparty.BaseActivity;
import ga.findparty.findparty.R;
import ga.findparty.findparty.StartActivity;
import ga.findparty.findparty.fragment.MyTeamFragment;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class ProfileActivity extends BaseActivity {

    public final static int EDIT_PROFILE = 100;
    public final static int UPDATE_RECOMMEND_LIST = 200;

    private String[] titles = {
            "정보",
            "평판",
            "기록",
            "추천"
    };

    private String userId;

    private ViewPager mViewPager;
    private NavigationTabStrip mNavigationTabStrip;
    private InfoFragment infoFragment;
    private ReviewFragment reviewFragment;
    private MyTeamFragment teamFragment;
    private RecommendFragment recommendFragment;

    private RelativeLayout rl_profile;
    private TextView recommendBtn;
    private TextView tv_name;
    private ImageView profileImg;
    private AVLoadingIndicatorView loadingName;
    boolean isProfileVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        userId = intent.getStringExtra("id");

        initUI();

    }

    private void initUI(){

        rl_profile = (RelativeLayout)findViewById(R.id.rl_profile);

        recommendBtn = (TextView)findViewById(R.id.recommend_btn);
        if(getUserID(this).equals(userId)){
            recommendBtn.setVisibility(View.GONE);
        }
        recommendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, RecommendListActivity.class);
                intent.putExtra("userId", userId);
                startActivityForResult(intent, UPDATE_RECOMMEND_LIST);
            }
        });
        tv_name = (TextView)findViewById(R.id.tv_name);
        profileImg = (ImageView)findViewById(R.id.profileImg);
        loadingName = (AVLoadingIndicatorView)findViewById(R.id.loading_name);

        mNavigationTabStrip = (NavigationTabStrip) findViewById(R.id.nts_top);
        mNavigationTabStrip.setTitles(titles);
        mViewPager = (ViewPager) findViewById(R.id.vp);
        mViewPager.setOffscreenPageLimit(titles.length);
        mViewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                final int pattern = position % titles.length;
                Fragment f;

                switch (pattern){
                    case 0:
                        if(infoFragment == null){
                            infoFragment = new InfoFragment();
                            Bundle bdl = new Bundle(1);
                            bdl.putString("id", userId);
                            infoFragment.setArguments(bdl);
                        }
                        f = infoFragment;
                        break;
                    case 1:
                        if(reviewFragment == null){
                            reviewFragment = new ReviewFragment();
                            Bundle bdl = new Bundle(1);
                            bdl.putString("id", userId);
                            reviewFragment.setArguments(bdl);
                        }
                        f = reviewFragment;
                        break;
                    case 2:
                        if(teamFragment == null){
                            teamFragment = new MyTeamFragment();
                            Bundle bdl = new Bundle(1);
                            bdl.putString("userId", userId);
                            teamFragment.setArguments(bdl);
                        }
                        f = teamFragment;
                        break;
                    case 3:
                        if(recommendFragment == null){
                            recommendFragment = new RecommendFragment();
                            Bundle bdl = new Bundle(1);
                            bdl.putString("recipientId", userId);
                            recommendFragment.setArguments(bdl);
                        }
                        f = recommendFragment;
                        break;
                    default:
                        f = new Fragment();
                        break;
                }

                return f;
            }

            @Override
            public int getCount() {
                return titles.length;
            }
        });

        mNavigationTabStrip.setViewPager(mViewPager, 0);
        mNavigationTabStrip.setTabIndex(0, true);

    }

    public void setProfileVisible(){
        isProfileVisible = !isProfileVisible;
        if(isProfileVisible){
            rl_profile.setVisibility(View.VISIBLE);
            tv_name.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            tv_name.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
        }else{
            rl_profile.setVisibility(View.GONE);
            tv_name.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            tv_name.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        }
    }

    public void setProfile(String name, String img){

        tv_name.setText(name + "님");
        loadingName.hide();

        Picasso.with(getApplicationContext())
                .load(img)
                .transform(new CropCircleTransformation())
                .into(profileImg);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case EDIT_PROFILE:
                //InfoFragment fragment = (InfoFragment)((FragmentStatePagerAdapter)mViewPager.getAdapter()).ins;
                infoFragment.updateUI((HashMap<String, Object>) data.getSerializableExtra("item"));
                break;
            case UPDATE_RECOMMEND_LIST:
                recommendFragment.getRecommendedList(true);
                break;
            default:
                break;
        }

    }

}

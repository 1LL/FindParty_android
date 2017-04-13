package ga.findparty.findparty.profile;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gigamole.navigationtabstrip.NavigationTabStrip;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.HashMap;

import ga.findparty.findparty.R;
import ga.findparty.findparty.StartActivity;
import ga.findparty.findparty.fragment.MyTeamFragment;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class ProfileActivity extends AppCompatActivity {

    public final static int EDIT_PROFILE = 100;

    private String[] titles = {
            "정보",
            "평판",
            "기록"
    };

    private String userId;

    private ViewPager mViewPager;
    private NavigationTabStrip mNavigationTabStrip;
    private InfoFragment infoFragment;
    private ReviewFragment reviewFragment;
    private MyTeamFragment teamFragment;

    private TextView recommendBtn;
    private TextView tv_name;
    private ImageView profileImg;
    private AVLoadingIndicatorView loadingName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        userId = intent.getStringExtra("id");

        initUI();

    }

    private void initUI(){

        recommendBtn = (TextView)findViewById(R.id.recommend_btn);
        if(StartActivity.USER_ID.equals(userId)){
            recommendBtn.setVisibility(View.GONE);
        }
        recommendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, RecommendListActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
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
            default:
                break;
        }

    }

}

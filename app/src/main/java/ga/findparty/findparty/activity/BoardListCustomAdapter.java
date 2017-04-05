package ga.findparty.findparty.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import ga.findparty.findparty.R;
import ga.findparty.findparty.profile.ProfileActivity;
import ga.findparty.findparty.util.OnAdapterSupport;
import ga.findparty.findparty.util.OnLoadMoreListener;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;


/**
 * Created by tw on 2016-08-16.
 */
public class BoardListCustomAdapter extends RecyclerView.Adapter<BoardListCustomAdapter.ViewHolder> {

    // UI
    private Context context;
    private CourseBoardActivity activity;

    //    private MaterialNavigationDrawer activity;
    private OnAdapterSupport onAdapterSupport;

    public ArrayList<HashMap<String, Object>> list;

    // 무한 스크롤
    private OnLoadMoreListener onLoadMoreListener;
    private int visibleThreshold = 10;
    private int lastVisibleItem, totalItemCount;
    private boolean loading = false;

    // 생성자
    public BoardListCustomAdapter(Context context, ArrayList<HashMap<String, Object>> list, RecyclerView recyclerView, OnAdapterSupport listener, Activity activity) {
        this.context = context;
        this.list = list;
        this.onAdapterSupport = listener;
        this.activity = (CourseBoardActivity)activity;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            recyclerView.addOnScrollListener(new ScrollListener() {
                @Override
                public void onHide() {
                    hideViews();
                }

                @Override
                public void onShow() {
                    showViews();
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //recycler view에 반복될 아이템 레이아웃 연결
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_board_list_custom_item,null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final HashMap<String,Object> item = list.get(position);
        final int pos = position;

        final String id = (String)item.get("id");
        final String userId = (String)item.get("userId");

        Picasso.with(context)
                .load((String)item.get("img"))
                .transform(new CropCircleTransformation())
                .into(holder.profileImg);

        holder.tv_name.setText((String)item.get("name"));
        holder.tv_email.setText((String)item.get("email"));
        holder.tv_content.setText((String)item.get("content"));

        int current = (int)item.get("current");
        int total = (int)item.get("total");
        holder.tv_count.setText(current + " / " + total);

        holder.rl_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("id", userId);
                onAdapterSupport.redirectActivity(intent);
            }
        });

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailBoardActivity.class);
                intent.putExtra("boardId", id);
                onAdapterSupport.redirectActivity(intent);
            }
        });

    }


    @Override
    public int getItemCount() {
        return this.list.size();
    }

    private void hideViews() {
        if (onAdapterSupport != null) {
            onAdapterSupport.hideView();
        }
    }

    private void showViews() {
        if (onAdapterSupport != null) {
            onAdapterSupport.showView();
        }
    }

    // 무한 스크롤
    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }
    public void setLoaded() {
        loading = false;
    }

    public abstract class ScrollListener extends RecyclerView.OnScrollListener {
        private static final int HIDE_THRESHOLD = 30;
        private int scrolledDistance = 0;
        private boolean controlsVisible = true;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

            totalItemCount = linearLayoutManager.getItemCount();
            lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
            if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                // End has been reached
                // Do something
                loading = true;
                if (onLoadMoreListener != null) {
                    onLoadMoreListener.onLoadMore();
                }
            }
            // 여기까지 무한 스크롤

            if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                onHide();
                controlsVisible = false;
                scrolledDistance = 0;
            } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                onShow();
                controlsVisible = true;
                scrolledDistance = 0;
            }

            if((controlsVisible && dy>0) || (!controlsVisible && dy<0)) {
                scrolledDistance += dy;
            }
            // 여기까지 툴바 숨기기
        }

        public abstract void onHide();
        public abstract void onShow();

    }

    public final static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout root;
        RelativeLayout rl_profile;
        ImageView profileImg;
        TextView tv_name;
        TextView tv_email;
        TextView tv_content;
        TextView tv_count;

        public ViewHolder(View v) {
            super(v);
            root = (LinearLayout)v.findViewById(R.id.root);
            rl_profile = (RelativeLayout)v.findViewById(R.id.rl_profile);
            profileImg = (ImageView)v.findViewById(R.id.profileImg);
            tv_name = (TextView)v.findViewById(R.id.tv_name);
            tv_email = (TextView)v.findViewById(R.id.tv_email);
            tv_content = (TextView)v.findViewById(R.id.tv_content);
            tv_count = (TextView)v.findViewById(R.id.tv_count);
        }
    }

}

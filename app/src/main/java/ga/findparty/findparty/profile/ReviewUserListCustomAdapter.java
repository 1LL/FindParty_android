package ga.findparty.findparty.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import ga.findparty.findparty.R;
import ga.findparty.findparty.activity.CourseBoardActivity;
import ga.findparty.findparty.activity.DetailBoardActivity;
import ga.findparty.findparty.util.AdditionalFunc;
import ga.findparty.findparty.util.OnAdapterSupport;
import ga.findparty.findparty.util.OnLoadMoreListener;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;


/**
 * Created by tw on 2016-08-16.
 */
public class ReviewUserListCustomAdapter extends RecyclerView.Adapter<ReviewUserListCustomAdapter.ViewHolder> {

    // UI
    private Context context;
    private ReviewUserListActivity activity;

    //    private MaterialNavigationDrawer activity;
    private OnAdapterSupport onAdapterSupport;

    public ArrayList<HashMap<String, Object>> list;

    // 무한 스크롤
    private OnLoadMoreListener onLoadMoreListener;
    private int visibleThreshold = 10;
    private int lastVisibleItem, totalItemCount;
    private boolean loading = false;

    // 생성자
    public ReviewUserListCustomAdapter(Context context, ArrayList<HashMap<String, Object>> list, RecyclerView recyclerView, OnAdapterSupport listener, Activity activity) {
        this.context = context;
        this.list = list;
        this.onAdapterSupport = listener;
        this.activity = (ReviewUserListActivity)activity;

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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommend_custom_item,null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final HashMap<String, Object> data = list.get(position);

        holder.tv_count.setVisibility(View.INVISIBLE);

        String date = AdditionalFunc.getDateString((long)data.get("date"));
        holder.tv_field.setText(date);

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ReviewDetailActivity.class);
                intent.putExtra("data", makeData(data));
                activity.redirectActivity(intent);
                // activity.showSnackbar("click ID : " + data.get("id"));
            }
        });

    }

    private ArrayList<String[]> makeData(HashMap<String, Object> h){

        HashMap<String, Object> ratingList = activity.getRatingList();

        ArrayList<String[]> list = new ArrayList<>();

        String indexString = "가나다라마바사아자차카타파하";

        ArrayList<String[]> content = (ArrayList<String[]>)h.get("content");
        for(String[] data : content){

            String id = data[0];
            int index = Integer.parseInt(data[1]);

            if(ratingList.containsKey(id)){

                HashMap<String, Object> map = (HashMap<String, Object>)ratingList.get(id);
                String question = (String)map.get("question");
                String[] answerList = (String[])map.get("answer");
                String answer = indexString.charAt(index) + ". " + answerList[index];

                String[] s = {
                        question,
                        answer
                };
                list.add(s);

            }

        }

        return list;

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

        RelativeLayout root;
        TextView tv_field;
        TextView tv_count;

        public ViewHolder(View v) {
            super(v);
            root = (RelativeLayout) v.findViewById(R.id.root);
            tv_field = (TextView)v.findViewById(R.id.tv_field);
            tv_count = (TextView)v.findViewById(R.id.tv_count);
        }
    }

}

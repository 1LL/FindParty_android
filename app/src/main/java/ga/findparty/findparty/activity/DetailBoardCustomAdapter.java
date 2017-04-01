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
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import ga.findparty.findparty.R;
import ga.findparty.findparty.util.OnAdapterSupport;
import ga.findparty.findparty.util.OnLoadMoreListener;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;


/**
 * Created by tw on 2016-08-16.
 */
public class DetailBoardCustomAdapter extends RecyclerView.Adapter<DetailBoardCustomAdapter.ViewHolder> {

    // UI
    private Context context;
    private DetailBoardActivity activity;

    private OnAdapterSupport onAdapterSupport;

    public ArrayList<HashMap<String, Object>> list;

    // 무한 스크롤
    private OnLoadMoreListener onLoadMoreListener;
    private int visibleThreshold = 10;
    private int lastVisibleItem, totalItemCount;
    private boolean loading = false;

    // 생성자
    public DetailBoardCustomAdapter(Context context, ArrayList<HashMap<String, Object>> list, RecyclerView recyclerView, OnAdapterSupport listener, Activity activity) {
        this.context = context;
        this.list = list;
        this.onAdapterSupport = listener;
        this.activity = (DetailBoardActivity)activity;

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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_board_custom_item,null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final HashMap<String,Object> item = list.get(position);
        final int pos = position;

        String title = (String)item.get("field");
        String number = (String)item.get("number") + "명";
        String currentNumber = "현재 " + ((ArrayList)item.get("participant")).size() + "명 지원 중";

        holder.tv_title.setText(title);
        holder.tv_number.setText(number);
        holder.tv_currentNumber.setText(currentNumber);

        holder.applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.applyField(item);
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

        TextView tv_title;
        TextView tv_number;
        TextView tv_currentNumber;
        Button applyBtn;

        public ViewHolder(View v) {
            super(v);
            tv_title = (TextView)v.findViewById(R.id.tv_title);
            tv_number = (TextView)v.findViewById(R.id.tv_number);
            tv_currentNumber = (TextView)v.findViewById(R.id.tv_current_number);
            applyBtn = (Button)v.findViewById(R.id.applyBtn);
        }
    }

}

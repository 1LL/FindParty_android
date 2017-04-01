package ga.findparty.findparty.fragment;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import ga.findparty.findparty.R;
import ga.findparty.findparty.activity.AddCourseActivity;
import ga.findparty.findparty.util.OnAdapterSupport;
import ga.findparty.findparty.util.OnLoadMoreListener;


/**
 * Created by tw on 2016-08-16.
 */
public class UserCourseListCustomAdapter extends RecyclerView.Adapter<UserCourseListCustomAdapter.ViewHolder> {

    // UI
    private Context context;
    private MyClassFragment fragment;

    private OnAdapterSupport onAdapterSupport;

    public ArrayList<HashMap<String, String>> list;

    // 무한 스크롤
    private OnLoadMoreListener onLoadMoreListener;
    private int visibleThreshold = 10;
    private int lastVisibleItem, totalItemCount;
    private boolean loading = false;

    // 생성자
    public UserCourseListCustomAdapter(Context context, ArrayList<HashMap<String, String>> list, RecyclerView recyclerView, OnAdapterSupport listener, Fragment fragment) {
        this.context = context;
        this.list = list;
        this.onAdapterSupport = listener;
        this.fragment = (MyClassFragment)fragment;

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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_course_list_custom_item,null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final HashMap<String,String> item = list.get(position);
        final int pos = position;

        final String id = item.get("id");
        String department = item.get("department");
        String no = item.get("no");
        String _class = item.get("class");
        final String title = item.get("title");
        String classification = item.get("classification");
        String day = item.get("day");
        String room = item.get("room");
        String lecturer = item.get("lecturer");

        holder.tv_title.setText(title + "-" + _class);
        holder.tv_day.setText(day);

        if("".equals(room)){

            if(!"".equals(lecturer)){
                holder.tv_room.setText(lecturer);
            }

        }else{

            if("".equals(lecturer)){
                holder.tv_room.setText(room);
            }else{
                holder.tv_room.setText(room + ", " + lecturer);
            }

        }

        holder.root.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                fragment.checkRemoveUserCourse(id, title, pos);
                return false;
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

        RelativeLayout root;
        TextView tv_title;
        TextView tv_day;
        TextView tv_room;

        public ViewHolder(View v) {
            super(v);
            root = (RelativeLayout)v.findViewById(R.id.root);
            tv_title = (TextView)v.findViewById(R.id.tv_title);
            tv_day = (TextView)v.findViewById(R.id.tv_day);
            tv_room = (TextView)v.findViewById(R.id.tv_room);
        }
    }

}

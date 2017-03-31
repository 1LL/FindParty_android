package ga.findparty.findparty.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionButton;

import ga.findparty.findparty.R;
import ga.findparty.findparty.activity.AddCourseActivity;

public class MyClassFragment extends BaseFragment {

    // UI
    private View view;
    private Context context;
    private FloatingActionButton addBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_my_class, container, false);
        context = container.getContext();

        initUI();

        return view;
    }

    private void initUI(){

        addBtn = (FloatingActionButton)view.findViewById(R.id.fab_add);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddCourseActivity.class);
                startActivity(intent);
            }
        });
        addBtn.setTitle("수업 추가");

    }


}

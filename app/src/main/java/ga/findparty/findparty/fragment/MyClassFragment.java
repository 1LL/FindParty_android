package ga.findparty.findparty.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ga.findparty.findparty.R;

public class MyClassFragment extends Fragment {

    // UI
    private View view;
    private Context context;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_my_class, container, false);
        context = container.getContext();

        initUI();

        return view;
    }

    private void initUI(){



    }


}

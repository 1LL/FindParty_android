package ga.findparty.findparty.profile;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ga.findparty.findparty.R;
import ga.findparty.findparty.fragment.BaseFragment;

/**
 * Created by tw on 2017-04-05.
 */
public class ReviewFragment extends BaseFragment {

    // BASIC UI
    private View view;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // resultCode = getArguments().getInt("code");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_review, container, false);
        context = container.getContext();

        initData();
        initUI();

        return view;

    }

    private void initData(){


    }

    private void initUI(){

    }

}

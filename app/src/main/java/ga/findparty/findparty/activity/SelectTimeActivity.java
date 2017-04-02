package ga.findparty.findparty.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import ga.findparty.findparty.R;

public class SelectTimeActivity extends AppCompatActivity {

    private Button saveBtn;

    private LinearLayout li_timeTitle;
    private LinearLayout li_mon;
    private LinearLayout li_tue;
    private LinearLayout li_wed;
    private LinearLayout li_thu;
    private LinearLayout li_fri;

    private ArrayList<Integer> monList;
    private ArrayList<Integer> tueList;
    private ArrayList<Integer> wedList;
    private ArrayList<Integer> thuList;
    private ArrayList<Integer> friList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_time);

        Intent intent = getIntent();
        monList = intent.getIntegerArrayListExtra("mon");
        tueList = intent.getIntegerArrayListExtra("tue");
        wedList = intent.getIntegerArrayListExtra("wed");
        thuList = intent.getIntegerArrayListExtra("thu");
        friList = intent.getIntegerArrayListExtra("fri");

        init();

    }

    private void init(){

        saveBtn = (Button)findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putIntegerArrayListExtra("mon", monList);
                intent.putIntegerArrayListExtra("tue", tueList);
                intent.putIntegerArrayListExtra("wed", wedList);
                intent.putIntegerArrayListExtra("thu", thuList);
                intent.putIntegerArrayListExtra("fri", friList);
                setResult(ApplyFormActivity.UPDATE_TIME, intent);
                finish();
            }
        });

        li_timeTitle = (LinearLayout)findViewById(R.id.li_time_title);
        li_mon = (LinearLayout)findViewById(R.id.li_mon);
        li_tue = (LinearLayout)findViewById(R.id.li_tue);
        li_wed = (LinearLayout)findViewById(R.id.li_wed);
        li_thu = (LinearLayout)findViewById(R.id.li_thu);
        li_fri = (LinearLayout)findViewById(R.id.li_fri);

        fillLinearLayout();

    }

    private String getTimeText(int t){

        int j = t%2;
        int currentTime = t/2;
        int nextTime = (t+1)/2;

        String time;
        String startTime;
        String finishTime;

        if(j == 0){

            startTime = currentTime + ":00";
            finishTime = currentTime + ":30";

        }else{

            startTime = currentTime + ":30";
            finishTime = nextTime + ":00";

        }

        time = startTime + "\n ~ " + finishTime;

        return time;

    }

    private void fillLinearLayout(){

        for(int i=0; i<6; i++){

            for(int j=18; j<44; j++){

                final int index = j;

                View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.time_table_custom_item, null, false);

                TextView tv = (TextView)v.findViewById(R.id.tv_title);

                if(i==0){
                    tv.setText(getTimeText(j));
                    tv.setBackgroundResource(R.drawable.board_title);
                }

                tv.setTag(j);

                switch (i){
                    case 0:
                        li_timeTitle.addView(v);
                        break;
                    case 1:
                        if(monList.contains(j)){
                            tv.setBackgroundResource(R.drawable.board_select);
                        }
                        tv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int tag = (int)v.getTag();
                                if(monList.contains(tag)){
                                    v.setBackgroundResource(R.drawable.board);
                                    monList.remove((Integer)tag);
                                }else{
                                    v.setBackgroundResource(R.drawable.board_select);
                                    monList.add(tag);
                                }
                            }
                        });
                        li_mon.addView(v);
                        break;
                    case 2:
                        if(tueList.contains(j)){
                            tv.setBackgroundResource(R.drawable.board_select);
                        }
                        tv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int tag = (int)v.getTag();
                                if(tueList.contains(tag)){
                                    v.setBackgroundResource(R.drawable.board);
                                    tueList.remove((Integer)tag);
                                }else{
                                    v.setBackgroundResource(R.drawable.board_select);
                                    tueList.add(tag);
                                }
                            }
                        });
                        li_tue.addView(v);
                        break;
                    case 3:
                        if(wedList.contains(j)){
                            tv.setBackgroundResource(R.drawable.board_select);
                        }
                        tv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int tag = (int)v.getTag();
                                if(wedList.contains(tag)){
                                    v.setBackgroundResource(R.drawable.board);
                                    wedList.remove((Integer)tag);
                                }else{
                                    v.setBackgroundResource(R.drawable.board_select);
                                    wedList.add(tag);
                                }
                            }
                        });
                        li_wed.addView(v);
                        break;
                    case 4:
                        if(thuList.contains(j)){
                            tv.setBackgroundResource(R.drawable.board_select);
                        }
                        tv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int tag = (int)v.getTag();
                                if(thuList.contains(tag)){
                                    v.setBackgroundResource(R.drawable.board);
                                    thuList.remove((Integer)tag);
                                }else{
                                    v.setBackgroundResource(R.drawable.board_select);
                                    thuList.add(tag);
                                }
                            }
                        });
                        li_thu.addView(v);
                        break;
                    case 5:
                        if(friList.contains(j)){
                            tv.setBackgroundResource(R.drawable.board_select);
                        }
                        tv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int tag = (int)v.getTag();
                                if(friList.contains(tag)){
                                    v.setBackgroundResource(R.drawable.board);
                                    friList.remove((Integer)tag);
                                }else{
                                    v.setBackgroundResource(R.drawable.board_select);
                                    friList.add(tag);
                                }
                            }
                        });
                        li_fri.addView(v);
                        break;
                }

            }

        }

    }

}

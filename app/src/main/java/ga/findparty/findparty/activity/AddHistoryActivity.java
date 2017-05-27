package ga.findparty.findparty.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import ga.findparty.findparty.BaseActivity;
import ga.findparty.findparty.Information;
import ga.findparty.findparty.R;
import ga.findparty.findparty.StartActivity;
import ga.findparty.findparty.profile.ProfileActivity;
import ga.findparty.findparty.util.AdditionalFunc;
import ga.findparty.findparty.util.ParsePHP;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class AddHistoryActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener{

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_SAVE_SUCCESS = 503;
    private final int MSG_MESSAGE_SAVE_FAIL = 504;

    private FrameLayout root;
    private TextView tv_title;
    private MaterialEditText editTitle;
    private TextView tv_content;
    private TextView dateBtn;
    private MaterialEditText editContent;
    private TextView addReferenceBtn;
    private LinearLayout li_referenceField;
    private TextView tv_charge;
    private LinearLayout li_charge_field;
    private TextView chargeBtn;
    private LinearLayout li_participantField;
    private Button saveBtn;

    private boolean isMeetingMode;
    private boolean isPresentMode;
    private boolean isHWMode;
    private String teamId;
    private ArrayList<HashMap<String, Object>> memberList;
    private HashMap<String, String> statusCheck;
    private HashMap<String, Object> chargeMember;
    private ArrayList<String> referenceList;
    private boolean isDate;
    private boolean isCharge;
    private long date;

    private MaterialDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_history);

        Intent intent = getIntent();
        teamId = intent.getStringExtra("teamId");
        isMeetingMode = intent.getBooleanExtra("isMeetingMode", false);
        isPresentMode = intent.getBooleanExtra("isPresentMode", false);
        isHWMode = intent.getBooleanExtra("isHWMode", false);
        if(isMeetingMode || isHWMode || isPresentMode){
            memberList = (ArrayList<HashMap<String, Object>>)intent.getSerializableExtra("memberList");
        }else{
            memberList = new ArrayList<>();
        }
        statusCheck = new HashMap<>();
        referenceList = new ArrayList<>();

        init();

    }


    private void init(){

        root = (FrameLayout)findViewById(R.id.root);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddHistoryActivity.super.onBackPressed();
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkAddable();
            }
        };

        tv_title = (TextView)findViewById(R.id.tv_title);
        editTitle = (MaterialEditText)findViewById(R.id.edit_title);
        editTitle.addTextChangedListener(textWatcher);
        tv_content = (TextView)findViewById(R.id.tv_content);
        editContent = (MaterialEditText)findViewById(R.id.edit_content);
        editContent.addTextChangedListener(textWatcher);
        dateBtn = (TextView)findViewById(R.id.date_btn);
        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        AddHistoryActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.setThemeDark(true);
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });
        addReferenceBtn = (TextView)findViewById(R.id.add_reference_btn);
        addReferenceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(AddHistoryActivity.this)
                        .title("입력")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("url을 입력해주세요.", null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                if(!"".equals(input.toString())) {
                                    referenceList.add(AdditionalFunc.replaceNewLineString(input.toString()));
                                    makeReferenceLayout();
                                }
                            }
                        }).show();
            }
        });
        li_referenceField = (LinearLayout)findViewById(R.id.li_reference_field);
        tv_charge = (TextView)findViewById(R.id.tv_charge);
        li_charge_field = (LinearLayout)findViewById(R.id.li_charge_field);
        chargeBtn = (TextView)findViewById(R.id.charge_btn);
        chargeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String[] memList = new String[memberList.size()];
                for(int i=0; i<memberList.size(); i++){
                    memList[i] = (String)memberList.get(i).get("name");
                }

                String title = "";
                if(isHWMode){
                    title = "과제 담당자를 선택해주세요.";
                }else if(isPresentMode){
                    title = "발표자를 선택해주세요.";
                }

                new MaterialDialog.Builder(AddHistoryActivity.this)
                        .title(title)
                        .items(memList)
                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                chargeMember = memberList.get(which);
                                setDateBtn(chargeBtn, (String)chargeMember.get("name"));
                                isCharge = true;
                                checkAddable();
                                return true;
                            }
                        })
                        .positiveText("확인")
                        .show();
            }
        });
        if(isHWMode || isPresentMode){
            li_charge_field.setVisibility(View.VISIBLE);
        }else{
            li_charge_field.setVisibility(View.GONE);
        }
        li_participantField = (LinearLayout)findViewById(R.id.li_participant_field);
        saveBtn = (Button)findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        if(isMeetingMode){
            editTitle.setText("회의");
            tv_content.setText("회의내용");
            editContent.setHint("회의내용을 입력해주세요.");
            editContent.requestFocus();
            makeListMeeting();
        }
        if(isHWMode){
            tv_charge.setText("담당자");
            editTitle.setText("개별과제");
            tv_content.setText("과제내용");
            editContent.setHint("과제내용을 입력해주세요.");
            editContent.requestFocus();
            makeListSatisfy();
        }
        if(isPresentMode){
            tv_charge.setText("발표자");
            editTitle.setText("발표");
            tv_content.setText("발표내용");
            editContent.setHint("발표내용을 입력해주세요.");
            editContent.requestFocus();
            makeListSatisfy();
        }

        progressDialog = new MaterialDialog.Builder(this)
                .content("잠시만 기다려주세요.")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .theme(Theme.LIGHT)
                .build();

        checkAddable();

    }

    private void makeReferenceLayout(){

        li_referenceField.removeAllViews();

        for(int i=0; i<referenceList.size(); i++){

            View v = getLayoutInflater().inflate(R.layout.add_field_custom_item, null, false);

            TextView tv_text = (TextView)v.findViewById(R.id.tv_text);
            tv_text.setText(referenceList.get(i));
            ImageView deleteBtn = (ImageView)v.findViewById(R.id.delete_btn);
            deleteBtn.setTag(i);
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = (int)v.getTag();
                    referenceList.remove(index);
                    makeReferenceLayout();
                }
            });

            li_referenceField.addView(v);

        }

    }

    private void makeListMeeting(){

        li_participantField.removeAllViews();

        for(int i=0; i<memberList.size(); i++){
            HashMap<String, Object> map = memberList.get(i);
            final String userId = (String)map.get("userId");
            String img = (String)map.get("img");
            String name = (String)map.get("name");
            String email = (String)map.get("email");
            String field = (String)map.get("field");

            View v = getLayoutInflater().inflate(R.layout.history_participant_custom_item, null, false);

            RelativeLayout rl_profile = (RelativeLayout)v.findViewById(R.id.rl_profile);
            ImageView profileImg = (ImageView)v.findViewById(R.id.profileImg);
            TextView tv_name = (TextView)v.findViewById(R.id.tv_name);
            TextView tv_email = (TextView)v.findViewById(R.id.tv_email);
            TextView tv_field = (TextView)v.findViewById(R.id.tv_field);

            rl_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AddHistoryActivity.this, ProfileActivity.class);
                    intent.putExtra("id", userId);
                    startActivity(intent);
                }
            });
            Picasso.with(getApplicationContext())
                    .load(img)
                    .transform(new CropCircleTransformation())
                    .into(profileImg);
            tv_name.setText(name);
            tv_email.setText(email);
            tv_field.setText(field);

            final Button attendBtn = (Button)v.findViewById(R.id.attend_btn);
            final Button lateBtn = (Button)v.findViewById(R.id.late_btn);
            final Button absentBtn = (Button)v.findViewById(R.id.absent_btn);

            attendBtn.setTag(userId);
            lateBtn.setTag(userId);
            absentBtn.setTag(userId);

            attendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    statusCheck.put((String)v.getTag(), "출석");
                    setButtonColor(attendBtn, true);
                    setButtonColor(lateBtn, false);
                    setButtonColor(absentBtn, false);
                    checkAddable();
                }
            });
            lateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    statusCheck.put((String)v.getTag(), "지각");
                    setButtonColor(attendBtn, false);
                    setButtonColor(lateBtn, true);
                    setButtonColor(absentBtn, false);
                    checkAddable();
                }
            });
            absentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    statusCheck.put((String)v.getTag(), "결석");
                    setButtonColor(attendBtn, false);
                    setButtonColor(lateBtn, false);
                    setButtonColor(absentBtn, true);
                    checkAddable();
                }
            });

            li_participantField.addView(v);

        }

    }

    private void makeListSatisfy(){

        li_participantField.removeAllViews();

        for(int i=0; i<memberList.size(); i++){
            HashMap<String, Object> map = memberList.get(i);
            final String userId = (String)map.get("userId");
            String img = (String)map.get("img");
            String name = (String)map.get("name");
            String email = (String)map.get("email");
            String field = (String)map.get("field");

            View v = getLayoutInflater().inflate(R.layout.history_homework_custom_item, null, false);

            RelativeLayout rl_profile = (RelativeLayout)v.findViewById(R.id.rl_profile);
            ImageView profileImg = (ImageView)v.findViewById(R.id.profileImg);
            TextView tv_name = (TextView)v.findViewById(R.id.tv_name);
            TextView tv_email = (TextView)v.findViewById(R.id.tv_email);
            TextView tv_field = (TextView)v.findViewById(R.id.tv_field);

            rl_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AddHistoryActivity.this, ProfileActivity.class);
                    intent.putExtra("id", userId);
                    startActivity(intent);
                }
            });
            Picasso.with(getApplicationContext())
                    .load(img)
                    .transform(new CropCircleTransformation())
                    .into(profileImg);
            tv_name.setText(name);
            tv_email.setText(email);
            tv_field.setText(field);

            final Button satisBtn = (Button)v.findViewById(R.id.satis_btn);
            final Button dissatisBtn = (Button)v.findViewById(R.id.dissatis_btn);

            satisBtn.setTag(userId);
            dissatisBtn.setTag(userId);

            satisBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    statusCheck.put((String)v.getTag(), "만족");
                    setButtonColor(satisBtn, true);
                    setButtonColor(dissatisBtn, false);
                    checkAddable();
                }
            });
            dissatisBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    statusCheck.put((String)v.getTag(), "불만족");
                    setButtonColor(satisBtn, false);
                    setButtonColor(dissatisBtn, true);
                    checkAddable();
                }
            });

            li_participantField.addView(v);

        }

    }

    private void save(){

        progressDialog.show();

        String title = editTitle.getText().toString();
        String content = editContent.getText().toString();

        HashMap<String, String> map = new HashMap<>();
        map.put("service", "saveHistory");
        map.put("teamId", teamId);
        if(isHWMode || isPresentMode){
            map.put("userId", (String)chargeMember.get("userId"));
        }else {
            map.put("userId", getUserID(this));
        }
        if(isHWMode){
            map.put("classification", "homework");
        }else if(isMeetingMode){
            map.put("classification", "meeting");
        }else if(isPresentMode){
            map.put("classification", "present");
        }
        map.put("title", title);
        map.put("content", AdditionalFunc.replaceNewLineString(content));
        map.put("reference", AdditionalFunc.arrayListToString(referenceList));
        map.put("date", Long.toString(date));
        map.put("participant", AdditionalFunc.makeHistoryMeetingParticipantList(statusCheck));

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {

            @Override
            protected void afterThreadFinish(String data) {

                if("1".equals(data)){
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SAVE_SUCCESS));
                }else{
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SAVE_FAIL));
                }

            }
        }.start();

    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_SAVE_SUCCESS:
                    progressDialog.hide();
                    setResult(HistoryActivity.UPDATE_HISTORY_LIST);
                    new MaterialDialog.Builder(AddHistoryActivity.this)
                            .title("안내")
                            .content("정상적으로 추가하였습니다.")
                            .theme(Theme.LIGHT)
                            .positiveText("확인")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .show();
                    break;
                case MSG_MESSAGE_SAVE_FAIL:
                    progressDialog.hide();
                    new MaterialDialog.Builder(AddHistoryActivity.this)
                            .title("안내")
                            .content("정상적으로 추가하지 못했습니다. 잠시 후 다시 시도해주세요.")
                            .theme(Theme.LIGHT)
                            .positiveText("확인")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    break;
                default:
                    break;
            }
        }
    }

    private void setButtonColor(Button btn, boolean check){
        if(check){
            btn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        }else{
            btn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
        }
    }

    private void checkAddable(){

        boolean isTitle = editTitle.isCharactersCountValid();
        boolean isContent = editContent.isCharactersCountValid();
        boolean isStatus = (!isMeetingMode) || (memberList.size() == statusCheck.size());
        boolean isChar = (!isHWMode && !isPresentMode) || isCharge;
        boolean isSatis = (!isHWMode && !isPresentMode) || (memberList.size() == statusCheck.size());

        boolean setting = isTitle && isContent && isStatus && isDate && isChar && isSatis;

        saveBtn.setEnabled(setting);
        setButtonColor(saveBtn, setting);


    }


    private void setDateBtn(TextView tv, String text){

        tv.setText(text);
        tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
        tv.setTypeface(Typeface.DEFAULT);

    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        isDate = true;
        date = AdditionalFunc.getMilliseconds(year, monthOfYear+1, dayOfMonth);

        String text = AdditionalFunc.getDateString(date);
        setDateBtn(dateBtn, text);

        checkAddable();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }


}

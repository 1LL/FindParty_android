package ga.findparty.findparty.activity;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

import ga.findparty.findparty.R;

public class AddFieldActivity extends AppCompatActivity {

    private RelativeLayout root;
    private MaterialEditText editTitle;
    private MaterialEditText editNumber;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_field);

        init();

    }

    void init(){

        root = (RelativeLayout)findViewById(R.id.activity_add_field);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddFieldActivity.super.onBackPressed();
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkSubmitable();
            }
        };
        editTitle = (MaterialEditText)findViewById(R.id.edit_title);
        editTitle.addTextChangedListener(textWatcher);
        editNumber = (MaterialEditText)findViewById(R.id.edit_number);
        editNumber.addTextChangedListener(textWatcher);

        submit = (Button)findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                HashMap<String, String> item = new HashMap<String, String>();
                item.put("title", editTitle.getText().toString());
                item.put("number", editNumber.getText().toString());
                intent.putExtra("item", item);
                setResult(AddCourseBoardActivity.UPDATE_ADD_FIELD, intent);
                finish();
            }
        });

        checkSubmitable();

    }

    private void checkSubmitable(){

        boolean isTitle = editTitle.isCharactersCountValid();
        boolean isNumber = editNumber.isCharactersCountValid();

        boolean setting = isTitle && isNumber;

        submit.setEnabled(setting);
        if(setting){
            submit.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        }else {
            submit.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
        }

    }

}

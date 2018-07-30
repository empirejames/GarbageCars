package com.james.garbagecar;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.james.garbagecar.database.TinyDB;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ThirdFragment extends Fragment {
    String TAG = ThirdFragment.class.getSimpleName();
    private EditText emailEditText;
    private EditText passEditText;
    private TextInputLayout accoutLayout,passwordLayout;
    private Button signUpBtn;
    private FirebaseAuth mAuth;
    private View view;
    private String account, password;
    private CheckBox chkRemeber;
    private TinyDB tinydb;
    boolean remeberMe;
    public ThirdFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.third_fragment, container, false);
        tinydb = new TinyDB(getActivity().getApplicationContext());
        initView(view);
        return view;
    }
    private void initView(View view) {

        //passwordLayout.setErrorEnabled(true);
        //accoutLayout.setErrorEnabled(true);

            if (tinydb.getString("account") != "") {
                emailEditText.setText(tinydb.getString("account"));
                passEditText.setText(tinydb.getString("password"));
                chkRemeber.setChecked(true);
            }




    }
    public void saveUserInfo(boolean isSave, String account, String password) {
        if (isSave) {
            tinydb.putString("account", account);
            tinydb.putString("password", password);
            remeberMe = true;
        } else {
            tinydb.putString("account", "");
            tinydb.putString("password", "");
            remeberMe = false;
        }

    }

    private void alertDialog(String message) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("確認", null)
                .show();
    }
    private void register(final String email, final String password) {
        new AlertDialog.Builder(getActivity())
                .setTitle("登入問題")
                .setMessage("無此帳號，是否要以此帳號與密碼註冊?")
                .setPositiveButton("註冊",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                createUser(email, password);
                            }
                        })
                .setNeutralButton("取消", null)
                .show();
    }
    private void createUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //String message = task.isSuccessful() ? "註冊成功" : "註冊失敗";
                Log.e(TAG, task.getException() + "");
                if (task.isSuccessful()) {
                    alertDialog("註冊成功");
                } else {
                    if (task.getException().toString().contains("The email address is already in use by another account.")) {
                        alertDialog("此 Email信箱已被註冊使用");
                    }
                }
            }
        });
    }
    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}

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
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.james.garbagecar.database.TinyDB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private CarsAdapter carsAdapter;
    boolean remeberMe;
    private ListView listView;
    ArrayList<GarbageCar> garbageCars = new ArrayList<GarbageCar>();
    ArrayList<GarbageCar> filterCars = new ArrayList<GarbageCar>();
    public ThirdFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG,"Third Oncreate : " + garbageCars.size());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.third_fragment, container, false);
        listView = (ListView) view.findViewById(R.id.lv_data);
        garbageCars = ((MainActivity)getActivity()).get_garbageData();
        for(int i =0; i<garbageCars.size();i++){
            if(garbageCars.get(i).getDistance().contains("公尺")){
                filterCars.add(garbageCars.get(i));
            }
        }
        Log.e(TAG,"Third onCreateView : " + garbageCars.size());
        DistanceSort(filterCars);
        Log.e(TAG, filterCars.size() + "");
        if(filterCars.size()==0){
            filterCars.add(new GarbageCar("無","周圍無垃圾車","無","無","無","無","無","無","無") );
        }
        carsAdapter = new CarsAdapter(getActivity(), R.layout.activity_carsdetail_layout, filterCars);
        listView.setAdapter(carsAdapter);
        listView.invalidateViews();

        return view;
    }
    private void DistanceSort(ArrayList<GarbageCar> cars) {
        Collections.sort(cars, new Comparator<GarbageCar>() {
            @Override
            public int compare(GarbageCar cars1, GarbageCar cars2) {
                double a,b;
                a = Double.parseDouble(cars1.getDistance().split("公")[0]);
                b = Double.parseDouble(cars2.getDistance().split("公")[0]);
                //Log.e(TAG,a + " V.S " + b);
                return a < b ? -1 : 1;
            }
        });
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

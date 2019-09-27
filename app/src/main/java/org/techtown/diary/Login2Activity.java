package org.techtown.diary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login2Activity extends AppCompatActivity {

    private EditText Name;
    private EditText Password;
    private TextView Info;
    private Button Login;

    private TextView userRegistration;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        FirebaseApp.initializeApp(this);



        Name =(EditText)findViewById(R.id.name);
        Password = (EditText)findViewById(R.id.password);

        Login =(Button)findViewById(R.id.btnLogin);
        userRegistration = (TextView)findViewById(R.id.register);

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        FirebaseUser user =mAuth.getCurrentUser();

        if(user !=null){
            finish();
            startActivity(new Intent(Login2Activity.this,MainActivity.class));
        }

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                validate(Name.getText().toString(), Password.getText().toString());
            }
        });

        userRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(Login2Activity.this,RegisterActivity.class));
            }
        });


    }

    private void validate(String userName, String userPassword) {
        progressDialog.setMessage("이메일 확인하시오");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(userName, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    Toast.makeText(Login2Activity.this,"로그인 성공", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Login2Activity.this,MainActivity.class));

                }else {

                    Toast.makeText(Login2Activity.this,"로그인 실패", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }
}

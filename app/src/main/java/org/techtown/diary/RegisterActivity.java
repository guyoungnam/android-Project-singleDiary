package org.techtown.diary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText userName, userPassword, userEmail, userAge;
    private Button regButton;
    private TextView userLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setupUIViews();

        mAuth =FirebaseAuth.getInstance();

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(validate()){
                    //데이터 업로드 하기
                    String user_email = userEmail.getText().toString().trim();
                    String user_password = userPassword.getText().toString().trim();

                    mAuth.createUserWithEmailAndPassword(user_email, user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                mAuth.signOut();
                                Toast.makeText(RegisterActivity.this, "성공적으로 등록", Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            } else {
                                Toast.makeText(RegisterActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }
            }
        });

        userLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

    }

    private  void setupUIViews(){

        userName =(EditText)findViewById(R.id.userName);
        userPassword = (EditText)findViewById(R.id.userPassword);
        userEmail =(EditText)findViewById(R.id.userEmail);
        regButton = (Button)findViewById(R.id.regButton);
        userLogin = (TextView)findViewById(R.id.userLogin);
        userAge = (EditText)findViewById(R.id.Age);


    }

    private boolean validate() {

        Boolean result =false;

        String name =userName.getText().toString();
        String password = userPassword.getText().toString();
        String email = userEmail.getText().toString();
        String age = userAge.getText().toString();

        if (name.isEmpty() || password.isEmpty() || email.isEmpty() || age.isEmpty()){
            Toast.makeText(this,"빈칸을 채우세요",Toast.LENGTH_SHORT).show();
        }else {

            result = true;
        }

        return  result;

    }

}

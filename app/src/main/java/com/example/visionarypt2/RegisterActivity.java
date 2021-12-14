package com.example.visionarypt2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {

    ImageView userPhoto;
    static  int RequestCode = 1;
    static  int PReqcode = 1;
    Uri pickedImgUri;
    private EditText userEmail, userPassword, userPassword2, userName;
    private Button registerButton;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_register);

        userPhoto = findViewById(R.id.registerUserPhoto);

        userEmail = findViewById(R.id.registerEmail);
        userPassword = findViewById(R.id.registerPassword);
        userPassword2 = findViewById(R.id.registerConPassword);
        userName = findViewById(R.id.registerName);
        registerButton = findViewById(R.id.btnRegister);


        mAuth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                registerButton.setVisibility(View.INVISIBLE);
                final String email = userEmail.getText().toString();
                final String name = userName.getText().toString();
                final String password = userPassword.getText().toString();
                final String password2 = userPassword2.getText().toString();

                if (email.isEmpty() || name.isEmpty() || password.isEmpty() || !password.equals(password2)){
                    showMessage("Please check all the fields");

                    registerButton.setVisibility(View.VISIBLE);
                }
                else {

                    //all fields are filled
                    CreateUserAccount(email,name,password);

                }
            }
        });

        userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(Build.VERSION.SDK_INT >= 22){
                    checkAndRequestForPermission();
                }
                else{
                    openGallery();
                }


            }
        });


    }

    private void CreateUserAccount(String email, String name, String password) {
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    showMessage("Your account has been created");

                    updateUserInfo(name,pickedImgUri,mAuth.getCurrentUser());
                }
                else {

                    showMessage("Account Failed" + task.getException().getMessage());
                    registerButton.setVisibility(View.VISIBLE);

                }
            }
        });
    }

    private void updateUserInfo(String name, Uri pickedImgUri, FirebaseUser currentUser) {
        StorageReference mstorage = FirebaseStorage.getInstance().getReference().child("user_photos");
        StorageReference imageFilePath = mstorage.child(pickedImgUri.getLastPathSegment());
        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(name)
                                .setPhotoUri(uri)
                                .build();

                        currentUser.updateProfile(profileUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            //information has been updated
                                            showMessage("register successful");
                                            update();
                                        }
                                    }
                                });
                    }
                });

            }
        });
    }

    private void update() {
        Intent homeActivity = new Intent(this,HomePageActivity.class);
        startActivity(homeActivity);
        finish();
    }

    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();

    }

    private void openGallery() {

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, RequestCode);




    }

    private void checkAndRequestForPermission() {

        if(ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE )
                != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(RegisterActivity.this,"Pleast accept for required permission", Toast.LENGTH_SHORT).show();
            }

            else {
                ActivityCompat.requestPermissions(RegisterActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PReqcode);
            }
        }
        else
            openGallery();



    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == RequestCode && data != null) {

            //user has picked an image

            pickedImgUri = data.getData();

            userPhoto.setImageURI(pickedImgUri);

        }
    }

}
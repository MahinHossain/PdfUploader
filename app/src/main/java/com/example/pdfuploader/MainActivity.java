package com.example.pdfuploader;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {
    private Button upload;
    EditText discripET;
    ImageView imageView;

    StorageReference storageReference;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        upload = findViewById(R.id.btnUploadId);
        discripET = findViewById(R.id.descripId);
        imageView = findViewById(R.id.imageId);

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("uploads");

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selectpdfFie();
            }
        });

    }

    private void selectpdfFie() {

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "select pdf"), 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            uploadPdf(data.getData());
        }
    }

    private void uploadPdf(Uri data) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading....  ");
        progressDialog.show();
        StorageReference reference = storageReference.child("upload" + System.currentTimeMillis() + ".pdf");
        reference.putFile(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                while (!uri.isComplete()) ;
                Uri url = uri.getResult();
                Pdfmodel pdfmodel = new Pdfmodel(discripET.getText().toString(), url.toString());
                databaseReference.child(databaseReference.push().getKey()).setValue(pdfmodel);
                progressDialog.dismiss();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                double progressss = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                progressDialog.setMessage("Uploaded" + (int) progressss + "%");


            }
        });
    }
}

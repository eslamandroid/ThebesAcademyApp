package com.eaapps.thebesacademy.Teacher;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.eaapps.thebesacademy.R;
import com.eaapps.thebesacademy.Utils.Constants;
import com.eaapps.thebesacademy.Utils.RetrieveData;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TeacherListFile extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    RecyclerView recycleDoctorFiles;
    RecyclerView.Adapter adapter;
    List<Files> filesList = new ArrayList<>();
    DatabaseReference ref;
    Bundle bundle;
    String myDoctor;
    RetrieveData<Files> filesRetrieveData;
    String[] sectionArr = {"Computer Science", "Information Systems", "Accounting", "Business Administration"};
    String[] levelArr = {"1", "2", "3", "4"};
    String master, level;
    Spinner sSection, sLevel;
    ProgressDialog progressDialog;
    FirebaseAuth mAuth;
    String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();


        if (user != null) {
            uid = user.getUid();
        }

        setContentView(R.layout.activity_teacher_list_file);
        bundle = getIntent().getExtras();
        assert bundle != null;
        myDoctor = bundle.getString(Constants.UID);
        progressDialog = new ProgressDialog(this);

        initToolbar();


        sSection = findViewById(R.id.spinner);
        sSection.setAdapter(new ArrayAdapter<>(this, R.layout.text_spinner_type, sectionArr));
        sSection.setOnItemSelectedListener(this);

        sLevel = findViewById(R.id.spinner1);
        sLevel.setAdapter(new ArrayAdapter<>(this, R.layout.text_spinner_type, levelArr));
        sLevel.setOnItemSelectedListener(this);

        ref = FirebaseDatabase.getInstance().getReference().child("Files");
        filesRetrieveData = new RetrieveData<Files>(TeacherListFile.this) {
        };

        recycleDoctorFiles = findViewById(R.id.recycleFileDoctor);
        recycleDoctorFiles.setHasFixedSize(false);
        recycleDoctorFiles.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(LayoutInflater.from(TeacherListFile.this).inflate(R.layout.custom_list_files, parent, false)) {
                    @Override
                    public String toString() {
                        return super.toString();
                    }
                };
            }


            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                Files files = filesList.get(position);

                View view = holder.itemView;

                TextView title = view.findViewById(R.id.title);
                TextView description = view.findViewById(R.id.description);
                TextView type_file = view.findViewById(R.id.type_file);
                Button downloadFile = view.findViewById(R.id.downloadFile);
                title.setText("Title : " + files.getTitle());
                description.setText("Description : " + files.getDescription());
                type_file.setText(files.getType_file());

                downloadFile.setOnClickListener(v -> {

                    StorageReference httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(files.getUrl());
                    downloadToLocalFile(httpsReference, System.currentTimeMillis() + files.getType_file());

                });

            }

            @Override
            public int getItemCount() {
                return filesList.size();
            }
        };
        recycleDoctorFiles.setAdapter(adapter);

    }

    @Override
    public void onBackPressed() {

    }

    public void fill() {
        Query query = ref.child(master).child(uid).orderByChild("level").equalTo(level);
        filesRetrieveData.RetrieveList(Files.class, query, new RetrieveData.CallBackRetrieveList<Files>() {

            @Override
            public void onDataList(List<Files> object, int countChild) {
                Files files = object.get(0);
                if (files != null) {
                    if (files.getLevel() != null && files.getLevel().equals(level))
                        filesList.add(files);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChangeList(List<Files> object, int position) {

            }

            @Override
            public void onRemoveFromList(int removePosition) {

            }

            @Override
            public void exits(boolean e) {

            }

            @Override
            public void hasChildren(boolean c) {

            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {
        switch (parent.getId()) {
            case R.id.spinner:
                filesList.clear();
                master = sSection.getItemAtPosition(i).toString();
                fill();
                break;
            case R.id.spinner1:
                filesList.clear();
                level = sLevel.getItemAtPosition(i).toString();
                fill();
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void downloadToLocalFile(StorageReference fileRef, String nameFile) {
        if (fileRef != null) {
            progressDialog.setTitle("Downloading...");
            progressDialog.setMessage(null);
            progressDialog.show();

            try {


                long s = 1024 * 1024;

                fileRef.getBytes(s).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {

                        storeFiles(nameFile, bytes);
                        Toast.makeText(TeacherListFile.this, "ssssss", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(TeacherListFile.this, "Upload file before downloading", Toast.LENGTH_LONG).show();
        }
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.btn_back);
        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(TeacherListFile.this, HomeTeacher.class));
        });
    }

    public void storeFiles(String nameFile, byte[] bytes) {
        File file, storage;
        FileOutputStream outputStream;
        try {
            storage = new File(
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
                            + "Thebes Academy");
            if (!storage.exists()) {
                storage.mkdir();
            }
            file = new File(storage, nameFile);

            //Create New File if not present
            outputStream = new FileOutputStream(file);
            outputStream.write(bytes);
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

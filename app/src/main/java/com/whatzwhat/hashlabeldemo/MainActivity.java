package com.whatzwhat.hashlabeldemo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button button;

    HashTextView hashTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prepareView();
    }

    private void prepareView() {
        hashTextView = findViewById(R.id.hash_text);
        button = findViewById(R.id.action_button);
        prepareButtonListener();
        settings();
    }

    private void settings() {
        hashTextView.initPoint = HashTextView.InitialPoint.FIRST;
        hashTextView.mode = HashTextView.Mode.NUMBER;
    }

    private void prepareButtonListener() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                hashTextView.setText("1234567890", 30.0);
            }
        });
    }





}






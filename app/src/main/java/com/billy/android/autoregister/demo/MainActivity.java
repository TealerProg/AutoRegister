package com.billy.android.autoregister.demo;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.billy.app_lib.IOther;
import com.billy.app_lib.OtherManager;
import com.billy.app_lib_interface.CategoryManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView text = (TextView) findViewById(R.id.text);
        StringBuilder sb = new StringBuilder();
        for (String s : CategoryManager.getCategoryNames()) {
            sb.append(s);
            sb.append("\n");
        }
        sb.append("---------------------\n");
        for (IOther s : new OtherManager().getAll()) {
            sb.append(s.getOtherName());
            sb.append("\n");
        }
        sb.append("---------------------\n");
        text.setText(sb.toString());
    }
}

package ua.com.stwet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    Button sBtn;
    static TextView sUser;
    public static String nameUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         sUser = (TextView) findViewById(R.id.searchUser);
        sBtn = (Button) findViewById(R.id.searchBtn);

        sBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showTweets(v);
            }
        });

        sUser.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sUser.setText("");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void showTweets(View view) {
        Intent intent = new Intent(this, TweetsActivity.class);
        startActivity(intent);
    }

    public static String getNameUser() {
        nameUser = sUser.getText().toString();
        return nameUser;
    }
}

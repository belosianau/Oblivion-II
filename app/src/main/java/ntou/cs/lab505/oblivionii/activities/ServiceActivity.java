package ntou.cs.lab505.oblivionii.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import ntou.cs.lab505.oblivionii.R;

public class ServiceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
    }

    public void serviceButtonClick(View view) {

    }

    public void buttonService(View view) {

    }

    public void buttonSetting(View view) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    private void checkServiceState() {

    }
}

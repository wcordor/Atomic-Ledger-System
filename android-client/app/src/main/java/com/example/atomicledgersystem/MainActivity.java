package com.example.atomicledgersystem;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private float accelerationThreshold = 12.0f;

    OkHttpClient client = new OkHttpClient();

    public void postJson() throws IOException {
        String json = "{\"outAccId\":7,"
                + "\"inAccId\":1,"
                + "\"amt\":\"75299.30\"}";

        RequestBody body = RequestBody.create(
                json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url("http://172.25.216.231:8080/transfer").post(body).build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                android.util.Log.e("TRANSFER_TEST", "Network Fail: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonData = response.body().string();
                android.util.Log.i("TRANSFER_TEST", "Server Response: " + jsonData);

                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Last Sync: Success", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        Button testBtn = findViewById(R.id.button);
        testBtn.setOnClickListener(v -> {
            try {
                postJson(); // Calling your existing logic
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // The OS Requirement: Magnitude Calculation
        double magnitude = Math.sqrt(x * x + y * y + z * z);

        if (magnitude > accelerationThreshold) {
            // Trigger your Atomic Ledger sync or refresh here!
            try {
                postJson();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

}

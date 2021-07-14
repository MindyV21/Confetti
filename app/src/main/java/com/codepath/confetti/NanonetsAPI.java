package com.codepath.confetti;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.LocalDate;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NanonetsAPI {

    public static final String TAG = "NanonetsAPI";

    public static void queryNotes(String modelId) {
        int startDay = (int) LocalDate.now().toEpochDay();
        String url = String.format("https://app.nanonets.com/api/v2/Inferences/Model/%s/" +
                        "ImageLevelInferences?start_day_interval=%d&current_batch_day=%d",
                modelId, startDay, startDay);
        Log.d(TAG, url);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("authorization", okhttp3.Credentials.basic("B1HIDqZA0Q_z0xTAqUy62o83M6xGv-xT", ""))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response.toString());
                }

                // response worked !
                //Toast.makeText(getActivity(), "Connection successful", Toast.LENGTH_LONG).show();
                String responseBody = response.body().string();
                response.close();
                Log.d(TAG, responseBody);

                // specific code to update any view within response
            }
        });
    }


}

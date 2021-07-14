package com.codepath.confetti;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NanonetsApi {

    public static final String TAG = "NanonetsApi";

    public static void queryNotes(String apiKey, String modelId) {
        int startDay = (int) LocalDate.now().toEpochDay();
        String url = String.format("https://app.nanonets.com/api/v2/Inferences/Model/%s/" +
                        "ImageLevelInferences?start_day_interval=%d&current_batch_day=%d",
                modelId, startDay, startDay);
        Log.d(TAG, url);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("authorization", okhttp3.Credentials.basic(apiKey, ""))
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
                String responseBody = response.body().string();
                response.close();
                Log.d(TAG, responseBody);

                // specific code to update any view within response
            }
        });
    }

    public static void asyncPredictFile(String apiKey, String modelId, File file) {
        OkHttpClient client = new OkHttpClient();
        MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpeg");
        String url = String.format("https://app.nanonets.com/api/v2/OCR/Model/%s/LabelFile/?async=true",
                modelId);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getAbsolutePath(), RequestBody.create(MEDIA_TYPE_JPG, new File(file.getAbsolutePath())))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", Credentials.basic(apiKey, ""))
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
                String responseBody = response.body().string();
                response.close();
                Log.d(TAG, responseBody);

                // specific code to update any view within response
            }
        });

    }
}

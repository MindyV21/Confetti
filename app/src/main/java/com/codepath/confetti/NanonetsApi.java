package com.codepath.confetti;

import android.util.Log;

import androidx.annotation.NonNull;

import com.codepath.confetti.models.Note;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

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

    private static JSONObject createJsonObject(String responseBody) {
        JSONObject json = null;
        try {
            json = new JSONObject(responseBody);
        } catch (JSONException e) {
            Log.e(TAG, "failed to create json object", e);
        }
        return json;
    }

    // finds all predicted files from nanonets database
    public static void queryNotes(String apiKey, String modelId) {
        int startDay = 18820;
        int endDay = (int) LocalDate.now().toEpochDay();
        String url = String.format("https://app.nanonets.com/api/v2/Inferences/Model/%s/" +
                        "ImageLevelInferences?start_day_interval=%d&current_batch_day=%d",
                modelId, startDay, endDay);
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
            }
        });
    }

    // finds a specific predicted file from the nanonets database
    public static void queryNote(String apiKey, String modelId, String requestFileId, File file) {
        String url = String.format("https://app.nanonets.com/api/v2/Inferences/Model/%s/ImageLevelInferences/%s",
                modelId, requestFileId);

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
                Log.i(TAG, url);
                Log.d(TAG, responseBody);

                JSONObject jsonObject = createJsonObject(responseBody);

                // create Note object from jsonObject
                Note note = new Note();
                try {
                    note.setText(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // create Prediction object from jsonArray


//                // upload Note object to firebase database
//                FirebaseDatabase.getInstance().getReference("Notes")
//                        .child(FirebaseAuth.getInstance().getUid())
//                        .child(requestFileId)
//                        .setValue(note).addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull @NotNull Task<Void> task) {
//                        if (!task.isSuccessful()){
//
//                        }
//                    }
//                });
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

                // query file in nanonets database
                try {
                    // extract data from async upload response
                    JSONObject jsonObject = new JSONObject(responseBody);
                    JSONObject result = jsonObject.getJSONArray("result").getJSONObject(0);
                    String requestFileId = result.getString("request_file_id");
                    String filePath = result.getString("filepath");

                    // query file in nanonets database
                    queryNote(apiKey, modelId, requestFileId, file);
                } catch (JSONException e) {
                    Log.e(TAG, "Error reading from jsonObject to extract requestFileId + url");
                    e.printStackTrace();
                }
            }
        });

    }
}

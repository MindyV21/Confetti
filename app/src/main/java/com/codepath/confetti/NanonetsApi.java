package com.codepath.confetti;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

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

                // gets all model ids
                try {
                    JSONObject jsonObject = new JSONObject(responseBody);
                    JSONArray images = jsonObject.getJSONArray("unmoderated_images");
                    for (int i = 0; i < images.length(); i++) {
                        Log.d(TAG, images.getJSONObject(i).getString("id"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // finds a specific predicted file from the nanonets database
    public static void queryNote(Context context, String apiKey, String modelId, String id, File file) {
        String url = String.format("https://app.nanonets.com/api/v2/Inferences/Model/%s/ImageLevelInferences/%s",
                modelId, id);

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

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(responseBody);


                    // create Note object from jsonObject
                    Note note = new Note();
                    note.setName("change name later");
                    note.getPredictions(jsonObject);

                    // upload note to database
                    Firebase.uploadNote(context, note, id);

                    // save photo in storage
                    Firebase.uploadImage(file, id);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    // sends image file to nanonets to predict data
    public static void asyncPredictFile(Context context, String apiKey, String modelId, File file) {
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
                    String id = result.getString("id");

                    // query file in nanonets database
                    queryNote(context, apiKey, modelId, id, file);
                } catch (JSONException e) {
                    Log.e(TAG, "Error reading from jsonObject to extract id + url");
                    e.printStackTrace();
                }
            }
        });
    }

    // sends image file to nanonets to predict data
    public static void predictFile(Context context, String apiKey, String modelId, File file) {
        OkHttpClient client = new OkHttpClient();
        MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpeg");
        String url = String.format("https://app.nanonets.com/api/v2/OCR/Model/%s/LabelFile/",
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
                Log.d(TAG, "predictFile response: " + responseBody);

                // query file in nanonets database
                try {
                    // extract data from async upload response
                    JSONObject jsonObject = new JSONObject(responseBody);
                    JSONObject result = jsonObject.getJSONArray("result").getJSONObject(0);
                    String id = result.getString("id");
                    Log.d(TAG, "predictFile id: " + id);

                    // create Note object from jsonObject
                    Note note = new Note();
                    note.setName("change name later");
                    try {
                        note.getPredictions(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // upload note to database
                    Firebase.uploadNote(context, note, id);

                    // save photo in storage
                    Firebase.uploadImage(file, id);

//                    // query file in nanonets database
//                    queryNote(context, apiKey, modelId, id, file);
                } catch (JSONException e) {
                    Log.e(TAG, "Error reading from jsonObject to extract id + url");
                    e.printStackTrace();
                }
            }
        });
    }
}

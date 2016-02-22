package layout.homeActivity;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import domain.raghu.myapplication.R;
import domain.raghu.myapplication.Sample;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity {
    String API_URL = "https://api.fullcontact.com/v2/person.json?";
    String API_KEY = "b29103a702edd6a";
    String sourceText;
    TextView outputTextView;
    Context mContext;
    WebView weatherInfo;
    private static final int SPEECH_REQUEST_CODE = 0;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getBaseContext();
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        outputTextView = (TextView) findViewById(R.id.txt_Result);
    }

    public void logout(View v) {
        Intent redirect = new Intent(HomeActivity.this, Sample.class);
        startActivity(redirect);
    }

    private void hideKeyboard(View editableView) {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editableView.getWindowToken(), 0);
    }

    public void translateText(String message) {
//        TextView sourceTextView = (TextView) findViewById(R.id.txt_Email);
//
//        sourceText = sourceTextView.getText().toString();

        String getURL = "https://translate.yandex.net/api/v1.5/tr.json/translate?" +
                "key=trnsl.1.1.20151023T145251Z.bf1ca7097253ff7e." +
                "c0b0a88bea31ba51f72504cc0cc42cf891ed90d2&text=" + message + "&" +
                "lang=en-es&[format=plain]&[options=1]&[callback=set]";//The API service URL
        final String response1 = "";
        OkHttpClient client = new OkHttpClient();
        try {

            Request request = new Request.Builder()
                    .url(getURL)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final JSONObject jsonResult;
                    final String result = response.body().string();

                    try {
                        jsonResult = new JSONObject(result);
                        JSONArray convertedTextArray = jsonResult.getJSONArray("text");
                        final String convertedText = convertedTextArray.get(0).toString();
                        Log.d("okHttp", jsonResult.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideKeyboard(outputTextView);
                                int notificationId = 100;

                                NotificationCompat.WearableExtender wearableExtender =
                                        new NotificationCompat.WearableExtender();
                                Notification notif = new NotificationCompat.Builder(mContext)
                                        .setContentTitle("Converted text")
                                        .setContentText(convertedText)
                                        .setSmallIcon(R.drawable.notificationicon)
                                        .extend(wearableExtender)
                                        .build();
                                NotificationManagerCompat notificationManager =
                                        NotificationManagerCompat.from(mContext);
                                notificationManager.notify(notificationId, notif);
                                outputTextView.setText(convertedText);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });


        } catch (Exception ex) {
            outputTextView.setText(ex.getMessage());

        }

    }

    public void recordAudio(View v) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, SPEECH_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            sourceText = spokenText;
            Log.d("Result", spokenText);
            translateText(sourceText);
            // Do something with spokenText
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}



package com.paybyphone.example.androidclient;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.gson.Gson;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.ssl.HttpsClientHelper;

public class MainActivity extends Activity {

    // Use AsyncTask to avoid using the UI thread to perform long running tasks such as network calls
    class FetchUri extends AsyncTask<String, Void, String> {
        private String resourceUri;
        public FetchUri(String resourceUri) { this.resourceUri = resourceUri; }
        @Override
        protected String doInBackground(String... params) {
            writeLineResult("Started background task to get url");
            try {
                Engine.getInstance().getRegisteredClients().clear();
                Engine.getInstance().getRegisteredClients().add(new HttpsClientHelper(null));

                Gson gson = new Gson();
                writeLineResult("created gson");
                Client client = new Client(Protocol.HTTPS);
                writeLineResult("about to call server with " + resourceUri);
                Response response = client.handle(new Request(Method.GET, resourceUri));
                String urlJson = response.getEntityAsText();
                writeLineResult("returned json: " + urlJson);
                paymentURL = gson.fromJson(urlJson, UrlForPaying.class);
                writeLineResult("url extracted from json: " + paymentURL.getUrl());
            } catch (Exception e) {
                writeLineResult("error type: " + e.getClass().toString());
                writeLineResult("error message: " + e.getMessage());
            }
            return paymentURL.getUrl();
        }
        @Override
        protected void onPostExecute(String result) {
            writeLineResult("got the post execute called with " + result);
            resultText.setText(result);
            writeLineResult("showed the result");
            getTokenButton.setEnabled(true);
            writeLineResult("enabled button");
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Initialize(savedInstanceState);

        getTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            writeLineResult("clicked token request button\n");
            getTokenButton.setEnabled(false);
            writeLineResult("disabled button");
            String resourceUri = TOKEN_URI + "/?amount=" + Uri.encode(amountInput.getText().toString()) +
                    "&phone=" + Uri.encode(phoneInput.getText().toString()) +
                    "&yourpaymentref=" + Uri.encode(refInput.getText().toString()) +
                    "&name=" + Uri.encode(nameInput.getText().toString()) +
                    "&email=" + Uri.encode(emailInput.getText().toString());
            new FetchUri(resourceUri).execute("");
            writeLineResult("executed async process. Leaving click listener.");
        }});
        payForCabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(paymentURL.getUrl()));
            startActivity(intent);
            }
        });
    }
    private void Initialize(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        payForCabButton = (Button) findViewById(R.id.startWebPage);
        getTokenButton = (Button) findViewById(R.id.tokenRequestButton);
        resultText = (TextView) findViewById(R.id.resultText);
        amountInput = (EditText) findViewById(R.id.amountInput);
        phoneInput = (EditText) findViewById(R.id.phoneInput);
        refInput = (EditText) findViewById(R.id.refInput);
        nameInput = (EditText) findViewById(R.id.nameInput);
        emailInput = (EditText) findViewById(R.id.emailInput);
    }
    private void writeLineResult(String text) { Log.v("PickACab", text); }

    final String TOKEN_URI = "https://devpartner.api.paybyphone.com:6643/payments/v1/generatetoken";
    private UrlForPaying paymentURL;
    private Button getTokenButton;
    private Button payForCabButton;
    private TextView resultText;
    private EditText amountInput;
    private EditText phoneInput;
    private EditText refInput;
    private EditText nameInput;
    private EditText emailInput;
}
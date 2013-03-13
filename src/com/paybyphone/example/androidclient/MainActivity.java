// The API calls shown in this application should be made from your back-end servers. As per best practices,
// values passed to and from your mobile app should be transported securely. The server code is located here
// so that all example calls can be shown in one application.

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
import android.widget.Toast;
import com.google.gson.Gson;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.ssl.HttpsClientHelper;

public class MainActivity extends Activity {
    final String BASE_URI = "https://devapi.paybyphone.com:11443/";
    final String TOKEN_URI = BASE_URI + "payments/v1/generatetoken";
    final String PAYMENT_STATUS_URI = BASE_URI + "payments/v1/status";

    // Use AsyncTask to avoid using the UI thread to perform long running tasks such as network calls
    class FetchUri extends AsyncTask<Void, Void, String> {
        private String resourceUri;
        public FetchUri(String resourceUri) { this.resourceUri = resourceUri; }

        @Override
        protected String doInBackground(Void... params) {
            try {
                Response response = new Client(Protocol.HTTPS).handle(new Request(Method.GET, resourceUri));
                if (!response.getStatus().isSuccess()) throw new Exception("Request failed");
                String urlJson = response.getEntityAsText();
                paymentURL = new Gson().fromJson(urlJson, UrlForPaying.class);
            } catch (Exception e) {
                writeLineResult("error type: " + e.getClass().toString());
                writeLineResult("error message: " + e.getMessage());
            }
            return paymentURL.getUrl();
        }

        @Override
        protected void onPostExecute(String result) {
            resultText.setText(result);
            getTokenButton.setEnabled(true);
        }
    }

    class GetStatus extends AsyncTask<Void, Void, String> {
        private String resourceUri;
        GetStatus(String resourceUri) { this.resourceUri = resourceUri; }

        @Override
        protected String doInBackground(Void... params) {
            try {
                Response response = new Client(Protocol.HTTPS).handle(new Request(Method.GET, resourceUri));
                if (!response.getStatus().isSuccess()) throw new Exception("Request failed");
                String urlJson = response.getEntityAsText();
                paymentStatus = new Gson().fromJson(urlJson, PaymentStatus.class);
            } catch (Exception e) {
                writeLineResult("error type: " + e.getClass().toString());
                writeLineResult("error message: " + e.getMessage());
            }
            return paymentStatus.getStatus();
        }

        @Override
        protected void onPostExecute(String result) {
            resultText.setText(result);
            checkPaymentStatusButton.setEnabled(true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Initialize(savedInstanceState);

        getTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTokenButton.setEnabled(false);
                String resourceUri = TOKEN_URI + "/?amount=" + Uri.encode(amountInput.getText().toString()) +
                        "&phone=" + Uri.encode(phoneInput.getText().toString()) +
                        "&yourpaymentref=" + Uri.encode(refInput.getText().toString()) +
                        "&name=" + Uri.encode(nameInput.getText().toString()) +
                        "&email=" + Uri.encode(emailInput.getText().toString());
                new FetchUri(resourceUri).execute();
            }
        });
        payForCabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (paymentURL == null || paymentURL.getUrl() == null || paymentURL.getUrl() == "") {
                    Toast.makeText(getApplicationContext(), "Please request a token first.", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(paymentURL.getUrl()));
                startActivity(intent);
            }
        });
        checkPaymentStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPaymentStatusButton.setEnabled(false);
                String resourceUri = PAYMENT_STATUS_URI + "/blah";
                new GetStatus(resourceUri).execute();
            }
        });
    }

    private void Initialize(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Engine.getInstance().getRegisteredClients().clear();
        Engine.getInstance().getRegisteredClients().add(new HttpsClientHelper(null));

        payForCabButton = (Button) findViewById(R.id.startWebPage);
        getTokenButton = (Button) findViewById(R.id.tokenRequestButton);
        checkPaymentStatusButton = (Button) findViewById(R.id.checkPaymentStatusButton);
        resultText = (TextView) findViewById(R.id.resultText);
        amountInput = (EditText) findViewById(R.id.amountInput);
        phoneInput = (EditText) findViewById(R.id.phoneInput);
        refInput = (EditText) findViewById(R.id.refInput);
        nameInput = (EditText) findViewById(R.id.nameInput);
        emailInput = (EditText) findViewById(R.id.emailInput);
    }

    private void writeLineResult(String text) { Log.v("PickACab", text); }

    private UrlForPaying paymentURL;
    private PaymentStatus paymentStatus;
    private Button getTokenButton;
    private Button payForCabButton;
    private Button checkPaymentStatusButton;
    private TextView resultText;
    private EditText amountInput;
    private EditText phoneInput;
    private EditText refInput;
    private EditText nameInput;
    private EditText emailInput;
}
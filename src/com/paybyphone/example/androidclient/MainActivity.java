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

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.*;
import org.apache.http.client.entity.UrlEncodedFormEntity; 
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.ssl.HttpsClientHelper;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class MainActivity extends Activity {
    //final String BASE_URI = "https://devapi.paybyphone.com:11443/";
	final String BASE_URI = "https://devpartner.api.paybyphone.com/Payments/";
    final String TOKEN_URI = BASE_URI + "v1/tokens";
    final String PAYMENT_STATUS_URI = BASE_URI + "v1/payments/{0}/status";

    // Use AsyncTask to avoid using the UI thread to perform long running tasks such as network calls
    class GetToken extends AsyncTask<Void, Void, String> {
        private String postData;
        public GetToken(String postData) { this.postData = postData; }

        @Override
        protected String doInBackground(Void... params) {
            /*try {
                Response response = new Client(Protocol.HTTPS).handle(new Request(Method.GET, postData));
                if (!response.getStatus().isSuccess()) throw new Exception("Request failed");
                String urlJson = response.getEntityAsText();
                paymentURL = new Gson().fromJson(urlJson, UrlForPaying.class);
            } catch (Exception e) {
                String error1 = "error type: " + e.getClass().toString();
                writeLineResult(error1);
                String error2 = "error message: " + e.getMessage().toString();
                writeLineResult(error2);
                return error1 + "\r" + error2;
            }
            String url = paymentURL.getUrl();
            return url;*/
            
        	HttpClient client = new DefaultHttpClient();
        	HttpPost post = new HttpPost(TOKEN_URI);
        	post.setHeader("Accept", "application/json");
        	post.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");
        	post.setHeader("Host", "myhost.com");
        	post.setHeader("X-ApiKey","24AD8009-0ADF-4801-B4E2-9948FE132097");
        	List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        	nvps.add(new BasicNameValuePair("data[body]", this.postData));
        	AbstractHttpEntity ent;
			try {
				ent = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
				ent.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
	        	ent.setContentEncoding("UTF-8");
	        	post.setEntity(ent);
	        	post.setURI(new URI(TOKEN_URI));
	        	HttpResponse response = client.execute(post);
	        	
	        	return "done"; 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				return "error: " + e.getMessage();
			}    
        }

        @Override
        protected void onPostExecute(String result) {
            resultText.setText(result);
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
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
                if (!response.getStatus().isSuccess()) throw new Exception("Request failed due to: " + response.getStatus().getDescription());
                String urlJson = response.getEntityAsText();
                paymentStatus = new Gson().fromJson(urlJson, PaymentStatus.class);
            } catch (Exception e) {
                String errorType = e.getClass().toString();
                String message = e.getMessage();
                writeLineResult("error type: " + errorType);
                writeLineResult("error message: " + message);
                return errorType + ": " + message;
            }
            return paymentStatus.getStatus();
        }

        @Override
        protected void onPostExecute(String result) {
            resultText.setText(result);
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
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
                String firstName = "";
                String lastName = "";
                String name = nameInput.getText().toString().trim();
                try {
                    firstName = name.split(" ")[0];
                    lastName = name.split(" ")[1];
                }
                catch (Exception e)
                {
                    // don't fail if the name is not entered correctly
                }
                String resourceUri = "amount=" + Uri.encode(amountInput.getText().toString()) +
                    "&phone=" + Uri.encode(phoneInput.getText().toString()) +
                    "&yourpaymentref=" + Uri.encode(paymentRefInput.getText().toString()) +
                    "&firstname=" + Uri.encode(firstName) +
                    "&lastname=" + Uri.encode(lastName) +
                    "&country=" + Uri.encode(countryCodeInput.getText().toString()) +
                    "&currency=" + Uri.encode(currencyInput.getText().toString()) +
                    //"&vendorid=" + Uri.encode(vendorIdInput.getText().toString()) +
                    "&email=" + Uri.encode(emailInput.getText().toString());
                new GetToken(resourceUri).execute();
            }
        });
        payForCabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (paymentURL == null || paymentURL.getUrl() == null || paymentURL.getUrl().equals("")) {
                Toast.makeText(getApplicationContext(), "Please request a token first.", Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(paymentURL.getUrl()));
            startActivity(intent);
        }});
        checkPaymentStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPaymentStatusButton.setEnabled(false);
                String resourceUri = PAYMENT_STATUS_URI + "/" + paymentRefInput.getText().toString();
                new GetStatus(resourceUri).execute();
            }
        });
        generateNewRefButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { paymentRefInput.setText(UUID.randomUUID().toString()); Toast.makeText(getApplicationContext(), paymentRefInput.getText(), Toast.LENGTH_SHORT);  }
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
        generateNewRefButton = (Button) findViewById(R.id.generateNewRefButton);
        resultText = (TextView) findViewById(R.id.resultText);
        amountInput = (EditText) findViewById(R.id.amountInput);
        phoneInput = (EditText) findViewById(R.id.phoneInput);
        nameInput = (EditText) findViewById(R.id.nameInput);
        emailInput = (EditText) findViewById(R.id.emailInput);
        countryCodeInput = (EditText) findViewById(R.id.countryCodeInput);
        currencyInput = (EditText) findViewById(R.id.currencyInput);
        paymentRefInput = (EditText) findViewById(R.id.paymentRefInput);
        vendorIdInput = (EditText) findViewById(R.id.vendorIdInput);
    }

    private void writeLineResult(String text) { Log.v("PickACab", text); }

    private UrlForPaying paymentURL;
    private PaymentStatus paymentStatus;
    private Button getTokenButton;
    private Button payForCabButton;
    private Button checkPaymentStatusButton;
    private Button generateNewRefButton;
    private TextView resultText;
    private EditText amountInput;
    private EditText phoneInput;
    private EditText nameInput;
    private EditText emailInput;
    private EditText countryCodeInput;
    private EditText currencyInput;
    private EditText paymentRefInput;
    private EditText vendorIdInput;
}
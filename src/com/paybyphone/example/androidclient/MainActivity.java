package com.paybyphone.example.androidclient;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

    final String TOKEN_URI = "https://devapi.paybyphone.com:11443/payments/v1/generatetoken";
    private Button getTokenButton;
    private Button payForCabButton;
    private TextView resultText;
    private UrlForPaying paymentURL;
    private EditText amountInput;
    private EditText phoneInput;
    private EditText refInput;
    private EditText nameInput;
    private EditText emailInput;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Engine.getInstance().getRegisteredClients().clear();
        Engine.getInstance().getRegisteredClients().add(new HttpsClientHelper(null));

        payForCabButton = (Button) findViewById(R.id.startWebPage);
        getTokenButton = (Button) findViewById(R.id.tokenRequestButton);
        resultText = (TextView) findViewById(R.id.resultText);
        amountInput = (EditText) findViewById(R.id.amountInput);
        phoneInput = (EditText) findViewById(R.id.phoneInput);
        refInput = (EditText) findViewById(R.id.refInput);
        nameInput = (EditText) findViewById(R.id.nameInput);
        emailInput = (EditText) findViewById(R.id.emailInput);

        getTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            try {
                Gson gson = new Gson();
                Client client = new Client(Protocol.HTTPS);
                String resourceUri = TOKEN_URI + "/?amount=" + Uri.encode(amountInput.getText().toString()) +
                        "&phone=" + Uri.encode(phoneInput.getText().toString()) +
                        "&yourpaymentref=" + Uri.encode(refInput.getText().toString()) +
                        "&name=" + Uri.encode(nameInput.getText().toString()) +
                        "&email=" + Uri.encode(emailInput.getText().toString());
                Response response = client.handle(new Request(Method.GET, resourceUri));
                String urlJson = response.getEntityAsText();
                paymentURL = gson.fromJson(urlJson, UrlForPaying.class);
                resultText.setText(paymentURL.getUrl());
            } catch (Exception e) {
                resultText.setText(e.getMessage());
            }
            }
        });
        payForCabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(paymentURL.getUrl()));
            startActivity(intent);
            }
        });
    }
}
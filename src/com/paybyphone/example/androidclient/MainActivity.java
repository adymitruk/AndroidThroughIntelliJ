package com.paybyphone.example.androidclient;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.ssl.HttpsClientHelper;

public class MainActivity extends Activity {

    final String TOKEN_URI = "https://devapi.paybyphone.com:11443/v1/payments/generatetoken/?amount=12.34&phone=6045555555&yourpaymentref=AAAA";
    final String PAYMENT_URI = "https://devsecure.paybyphone.com:8843/payments/make";
    private Button getTokenButton;
    private Button payForCabButton;
    private TextView resultText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Engine.getInstance().getRegisteredClients().clear();
        Engine.getInstance().getRegisteredClients().add(new HttpsClientHelper(null));

        payForCabButton = (Button) findViewById(R.id.startWebPage);
        getTokenButton = (Button) findViewById(R.id.tokenRequestButton);
        resultText = (TextView) findViewById(R.id.resultText);

        getTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Client client = new Client(Protocol.HTTPS);
                    Response response = client.handle(new Request(Method.GET, TOKEN_URI));
                    resultText.setText(response.getEntityAsText() + "\n" + TOKEN_URI);
                } catch (Exception e) {
                    resultText.setText(e.getMessage());
                }
            }
        });
        payForCabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(PAYMENT_URI));
                startActivity(intent);
            }
        });
    }
}



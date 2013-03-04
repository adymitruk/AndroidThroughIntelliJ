package com.paybyphone.example.androidclient;

import android.app.Activity;
import android.os.Bundle;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.ClientResource;

public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Client client = new Client(new Context(), Protocol.HTTP);
        client.getContext().getParameters().add("useForwardedForHeader", "false");

        ClientResource clientResource = new ClientResource("https://devapi.paybyphone.com:11443/v1/payments/generatetoken?amount/12.34");
        clientResource.setRequestEntityBuffering(true);
        clientResource.setNext(client);
        clientResource.get();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}

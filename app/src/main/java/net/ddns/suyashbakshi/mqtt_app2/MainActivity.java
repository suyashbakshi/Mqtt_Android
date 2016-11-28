package net.ddns.suyashbakshi.mqtt_app2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    MqttAndroidClient mqttAndroidClient;
    final String serverURI = "tcp://ec2-54-146-42-83.compute-1.amazonaws.com:1883";
    private TextView tv;
    private TextView serverTv;
    private Button connectBtn;
    private ToggleButton toggleSwitch;

    final String clientID = "mobile";

    final String subTopic = "status/#";
    final String pubTopic = "control/";

//    final String pubMessage = "Hello from Android";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView)findViewById(R.id.tv);
        connectBtn = (Button)findViewById(R.id.connectBtn);
        serverTv = (TextView)findViewById(R.id.serverTv);
        toggleSwitch = (ToggleButton) findViewById(R.id.toggleSwitch);

        toggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                    publish("on");
                else
                    publish("off");
            }

        });

//        tv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                publish();
//            }
//        });

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(),serverURI,clientID);
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.v("FIREBIRD","Connecton_lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.v("FIREBIRD","Message_Arrived " + message.getPayload());
                tv.setText("");
                tv.setText(message.getPayload().toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });



        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String runtimeServerUri = String.valueOf(serverTv.getText());
                    Log.v("FIREBIRD",runtimeServerUri);
                    MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
                    mqttConnectOptions.setServerURIs(new String[]{runtimeServerUri});
                    mqttConnectOptions.setCleanSession(true);
                    mqttAndroidClient.connect(mqttConnectOptions,null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.v("FIREBIRD","Connected to the broker");
                            Toast.makeText(getApplicationContext(),"Connected to Broker",Toast.LENGTH_SHORT).show();
                            subscribe();
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Log.v("FIREBIRD","cant connect to broker");
                            Toast.makeText(getApplicationContext(),"Failed to connect to Broker",Toast.LENGTH_SHORT).show();
                            exception.printStackTrace();
                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private void subscribe() {
        try {
            mqttAndroidClient.subscribe(subTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getApplicationContext(),"Subscribed",Toast.LENGTH_SHORT).show();
                    Log.v("FIREBIRD","Subscribed successfully");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(),"Failed to Subscribe",Toast.LENGTH_SHORT).show();
                    Log.v("FIREBIRD","Failed to subscribe");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    private void publish(String publishMessage){

        MqttMessage message = new MqttMessage(publishMessage.getBytes());
        try {
            mqttAndroidClient.publish(pubTopic, message, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.v("FIREBIRD","Published successful");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.v("FIREBIRD","Publish Failed");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}

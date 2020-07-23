package com.perpule;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EthernetConnection {
    private Socket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    private void talkToClient(String address, int port, String message, String encrypt) throws IOException {
        socket = new Socket(address, port);
        System.out.println("Connected to " + socket.toString());
        out = new DataOutputStream(socket.getOutputStream());

        String responseString ="";
        byte[] cipBytes;
        byte[] decBytes = new byte[1024];
        Cryptography cryp = new Cryptography();

        try {
            if (Boolean.valueOf(encrypt)) {
                cipBytes = cryp.EcrEncrypt(message);
            }
            else {
                System.out.println("No encryption");
                System.out.println("MEssage: " +  message);
                cipBytes = UtilHex.hexStringToByteArray(message);
            }
            System.out.println("Input : " + cipBytes.toString());
            out.write(cipBytes);

            Integer t = 0;
            while (t < 500) {

                in = new DataInputStream(socket.getInputStream());
                int length = in.read(decBytes);
                decBytes = Arrays.copyOf(decBytes, length);

                if (Boolean.valueOf(encrypt)) {
                    responseString = cryp.EcrDecrypt(decBytes);
                }
                else {
                    System.out.println("No encryption");
                    responseString = UtilHex.bytesToHex(decBytes);
                    System.out.println("First response: " + responseString);
                    decBytes = new byte[1024];
                    length = in.read(decBytes);
                    decBytes = Arrays.copyOf(decBytes, length);
                    responseString = UtilHex.bytesToHex(decBytes);
                    System.out.println("Second response: " + responseString + " length: " + length);
                }
                System.out.println("Response : " + responseString);

                Map<String, String> data = new HashMap<String, String>();
                data.put("data", responseString);

                if (!responseString.isEmpty()) {
                    String url = "http://localhost:8083/edc/response";
                    System.out.println(url);
                    ApiCallerResponse resp = ApiCaller.getInstance().setUrl(url)
                            .setRequestMethod(ApiCaller.RequestMethod.POST)
                            .setContentType("application/json")
                            .setData(new Gson().toJson(data))
                            .setConnectionTimeout(1800000)
                            .setReadTimeout(1800000)
                            .call();
                    System.out.println("API Response : " + resp.getResponseMsg());

                    if (resp.getResponseCode() == 200) {
                        break;
                    }
                    break;
                }

                try {
                    TimeUnit.SECONDS.sleep(1);
                    t++;
                } catch (InterruptedException e) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.close();
        socket.close();
    }

    public static void main(String args[]) throws IOException {
        new EthernetConnection().talkToClient(args[0], Integer.valueOf(args[1]), args[2],args[3]);
    }
}
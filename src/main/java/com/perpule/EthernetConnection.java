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

    /**
     * Send data to client device.
     *
     * @param address
     * @param port
     * @param message
     * @param encrypt
     * @throws IOException
     */
    private void talkToClient(String address, int port, String message, String encrypt) throws IOException {
        Socket socket = new Socket(address, port);
        System.out.println("Connected to " + socket.toString());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = null;

        String responseString = "";
        byte[] cipBytes;
        byte[] decBytes = new byte[1024];
        Cryptography cryp = new Cryptography();

        try {
            if (Boolean.parseBoolean(encrypt)) {
                cipBytes = cryp.EcrEncrypt(message);
            } else {
                System.out.println("No encryption");
                System.out.println("Message: " + message);
                cipBytes = UtilHex.hexStringToByteArray(message);
            }
            System.out.println("Input : " + cipBytes.toString());
            out.write(cipBytes);

            int t = 0;
            while (t < 500) {

                in = new DataInputStream(socket.getInputStream());
                int length = in.read(decBytes);
                decBytes = Arrays.copyOf(decBytes, length);

                if (Boolean.parseBoolean(encrypt)) {
                    responseString = cryp.EcrDecrypt(decBytes);
                } else {
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

    /**
     * Check device connection.
     *
     * @param address
     * @param port
     */
    private void checkDevice(String address, int port) {
        Socket socket = null;
        try {
            socket = new Socket(address, port);
            System.out.println("OK");
            if (socket.isConnected()) {
                socket.close();
            }
            return;
        } catch (IOException e) {
            // e.printStackTrace();
        }
        System.out.println("NOT_OK");
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage:\n"
                                   + "* check device conn  : java -jar edc-conn.jar <address> <port>\n"
                                   + "* send data to device: java -jar edc-conn.jar <address> <port> <message> <encrypt>\n");
        } else if (args.length == 2) {
            //System.out.println("Checking device: " + Arrays.toString(args));
            new EthernetConnection().checkDevice(args[0], Integer.parseInt(args[1]));
        } else if (args.length == 4) {
            System.out.println("Sending data to device: " + Arrays.toString(args));
            new EthernetConnection().talkToClient(args[0], Integer.parseInt(args[1]), args[2], args[3]);
        } else {
            System.out.println("Command not recognized!");
        }
    }
}
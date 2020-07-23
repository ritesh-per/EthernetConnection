package com.perpule;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Cryptography {
    private String KEY_FILE_PATH = "/lib/ECRHEX.KEK";

    private byte[] encryptionKey;
    private final String INIT_VECTOR = "00000000";

    public Cryptography(){
        String keyString;

        keyString = encLoadKey();
        if (keyString == null) {
            keyString = "73A61CB70522D170";
        }

        try {
            encryptionKey = Cryptography.decrypt(keyString.getBytes(), "000000000000000000000000".getBytes(), INIT_VECTOR.getBytes());
            encryptionKey = Arrays.copyOf(encryptionKey, 24);
            System.arraycopy(encryptionKey, 0, encryptionKey, 16, 8);
        } catch (Exception e) {
        }
    }

    private String encLoadKey() {
        String myLibraryPath = System.getProperty("user.dir");

        File encFile = new File(myLibraryPath + KEY_FILE_PATH);
        if (encFile.exists() == false) {
            return null;
        }

        try {
            FileInputStream fis = new FileInputStream(encFile);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine, output;

            output = "";
            while ((strLine = br.readLine()) != null) {
                output = output + strLine;
            }
            in.close();

            return output;
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] removePadding(byte[] paddedBytes){
        int paddingCount = 0;
        for (int i = (paddedBytes.length - 1); i > (paddedBytes.length - 8); i--) {
            if (paddedBytes[i] == (byte) 0xFF)       // PKCS1 Padding (0xFF)
                paddingCount++;
            else
                break;
        }
        return Arrays.copyOfRange(paddedBytes, 0, paddedBytes.length - paddingCount);
    }

    // Encrypts and encode in byte array
    public static byte[] encrypt(byte[] plainBytes, byte[] tdesKeyData, byte[] myIV) throws Exception {
        // ---- Use specified 3DES key and IV from other source --------------
        Cipher c3des = Cipher.getInstance("DESede/ECB/NoPadding");
        SecretKeySpec myKey = new SecretKeySpec(tdesKeyData, "DESede");

        //c3des.init(Cipher.ENCRYPT_MODE, myKey, ivspec);
        c3des.init(Cipher.ENCRYPT_MODE, myKey);
        byte [] cipherBytes = c3des.doFinal(plainBytes);
        return cipherBytes;
    }

    // Decrypts and encode in byte arrays
    public static byte[] decrypt(byte[] cipheredBytes, byte[] tdesKeyData, byte[] myIV) throws Exception {
        // ---- Use specified 3DES key and IV from other source --------------
        Cipher c3des = Cipher.getInstance("DESede/ECB/NoPadding");
        SecretKeySpec myKey = new SecretKeySpec(tdesKeyData, "DESede");

        //c3des.init(Cipher.DECRYPT_MODE, myKey, ivspec);
        c3des.init(Cipher.DECRYPT_MODE, myKey);
        byte [] plainBytes = c3des.doFinal(cipheredBytes);
        return removePadding(plainBytes);
    }

    public byte[] EcrEncrypt(String message) throws Exception {
        byte cipBytes[];
        int len;
        byte[] realMsg = UtilHex.hexStringToByteArray(message);

        len = realMsg.length;
        if (len % 8 != 0) {
            len = ((len / 8) + 1) * 8;
            byte[] tempBytes = new byte[len];
            System.arraycopy(realMsg, 0, tempBytes, 0, realMsg.length);
            cipBytes = Cryptography.encrypt(tempBytes, encryptionKey, INIT_VECTOR.getBytes());

        } else {
            cipBytes = Cryptography.encrypt(realMsg, encryptionKey, INIT_VECTOR.getBytes());
        }
        return cipBytes;
    }

    public String EcrDecrypt(byte[] decBytes) throws Exception {
        String messageBuffer = "";

        decBytes = Cryptography.decrypt(decBytes, encryptionKey, INIT_VECTOR.getBytes());

        for (byte num : decBytes) {
            messageBuffer += String.format("%c", num);
        }

        decBytes = new byte[512];   // refresh the memory for receiving next bytes
        return messageBuffer;
    }

    public static void main(String[] args) {
        String a = "false";
        if (Boolean.valueOf(a))
            System.out.println(true);
        else
            System.out.println(false);
    }
}


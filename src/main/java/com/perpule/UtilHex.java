package com.perpule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilHex {

    public static String asciiToStringHex(String ascii) {
        char[] chars = ascii.toCharArray();

        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            hex.append(Integer.toHexString((int) chars[i]));
        }

        return hex.toString();
    }

    public static String decimaltohex(int num) {
        char ch[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        int rem;
        String hexadecimal = "";

        while (num != 0) {
            rem = num % 16;
            hexadecimal = ch[rem] + hexadecimal;
            num = num / 16;
        }

        int len = hexadecimal.length();
        if (len == 1) {
            hexadecimal = "0" + hexadecimal;
        }

        return hexadecimal;
    }

    public static String stringHexToAscii(String hexString) {
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hexString.length() - 1; i += 2) {

            //grab the hex in pairs
            String output = hexString.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char) decimal);

            temp.append(decimal);
        }
        return sb.toString();
    }

    public static String stringHexToAsciiXOR(String hexString) {
        StringBuilder temp = new StringBuilder();
        int prev = 0;
        int xor = 0;
        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hexString.length() - 1; i += 2) {

            //grab the hex in pairs
            String output = hexString.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            xor = xor ^ decimal;
            temp.append(decimal);
        }
        return String.valueOf(xor);
    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static boolean isHex(String hex){
        // Pattern p = Pattern.compile("^[a-zA-Z0-9]+$");
        Pattern p = Pattern.compile("^?([a-f0-9]{6}|[a-f0-9]{1})$");
        Matcher m = p.matcher(hex);
        return m.matches();
    }

    public static void main(String[] args) {
        System.out.println(asciiToStringHex("12345678901"));
        System.out.println(decimaltohex(105));
        System.out.println(stringHexToAscii("6970"));
    }
}


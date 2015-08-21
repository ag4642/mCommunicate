package com.example.ashu4642.mcommunicate.P2P;

/**
 * Created by ashu4642 on 10/16/2014.
 */

import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Utils {
    public static String IP = "http://128.255.25.155/mCommunicate/";

    public static boolean HttpTimeout = false;

    public static String userResponse = "";

    private static String TAG = "utils activity";
    private static String VALID = "valid";
    private final static String p2pInt = "p2p-p2p0";
    public static boolean executeUserService(String service, ArrayList<NameValuePair> nameValuePairs) {
        StringBuilder stringBuilder = executeService(service, nameValuePairs);
        if(TextUtils.isEmpty(stringBuilder)) return false;

        Utils.userResponse = parseUserResponse(stringBuilder);
        if(!validateUserResponse(Utils.userResponse)) {
            Log.d(Utils.TAG, service + " returned invalid response");
            return false;
        }

        Log.d(Utils.TAG, service + " returned valid response");
        return true;
    }
    private static StringBuilder executeService(String service, ArrayList<NameValuePair> nameValuePairs) {
        HttpParams httpParams;
        HttpClient httpClient;
        HttpPost httpPost;
        HttpResponse httpResponse;
        HttpEntity httpEntity;

        InputStream inputStream;
        StringBuilder stringBuilder;

        try {
            Log.d(Utils.TAG, Utils.IP + service);

            Utils.HttpTimeout = false;

            // set connection and read timeout
            httpParams = new BasicHttpParams();
            //HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            //HttpConnectionParams.setSoTimeout(httpParams, 10000);

            httpClient = new DefaultHttpClient(httpParams);

            // create request parameters
            String params = "?";
            for(int i = 0; i < nameValuePairs.size(); i++)
            {
                String name = nameValuePairs.get(i).getName();
                String value = nameValuePairs.get(i).getValue();

                params += name + "=" + value + "&";
            }

            Log.d(Utils.TAG, Utils.IP + service + params);

            // execute request via httpPost
            httpPost = new HttpPost(Utils.IP + service + params);
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            httpResponse = httpClient.execute(httpPost);
            httpEntity = httpResponse.getEntity();

            inputStream = httpEntity.getContent(); // get response content
            if(inputStream == null) return null;

            Log.d(Utils.TAG, "HTTP connection successful");

            stringBuilder = readInputStream(inputStream);
        }
        catch(Exception e) {
            Utils.HttpTimeout = true;

            Log.e(Utils.TAG, "Error in executing " + service + ": " + e.toString());
            return null;
        }

        return stringBuilder;
    }
    private static StringBuilder readInputStream(InputStream inputStream) {
        StringBuilder stringBuilder = null;

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            stringBuilder = new StringBuilder();

            String inputLine = null;
            while((inputLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(inputLine); // append response to StringBuilder
            }
            bufferedReader.close();

            Log.d(Utils.TAG, stringBuilder.toString());
        }
        catch(Exception e) {
            String message = "Error reading HTTP response: " + e.toString();
            Log.e(Utils.TAG, message);
        }

        return stringBuilder; // null if error
    }

    private static String parseUserResponse(StringBuilder stringBuilder) {
        String response = null;

        try {
            JSONObject jsonObj = new JSONObject(stringBuilder.toString());
            Boolean valid = jsonObj.getBoolean("valid");

            if(!valid) response = jsonObj.getString("error"); // return invalid message
            else response = Utils.VALID; // return valid message

            Log.d(Utils.TAG, response);

            return response;
        } catch (JSONException e) {
            String message = "Error parsing user JSON response: " + e.toString();
            Log.e(Utils.TAG, message);

            return null;
        }
    }

    public static String getIPFromMac(String MAC) {
		/*
		 * method modified from:
		 *
		 * http://www.flattermann.net/2011/02/android-howto-find-the-hardware-mac-address-of-a-remote-host/
		 *
		 * */
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {

                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    // Basic sanity check
                    String device = splitted[5];
                    if (device.matches(".*" +p2pInt+ ".*")){
                        String mac = splitted[3];
                        if (mac.matches(MAC)) {
                            return splitted[0];
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public static boolean getConnectionData(String service, ArrayList<NameValuePair> nameValuePairs) {
        StringBuilder stringBuilder = executeService(service, nameValuePairs);
        if(TextUtils.isEmpty(stringBuilder)) return false;

        ViewConnectionsActivity.connectionsList = parseConnectionResponse(stringBuilder);
        if(!validateConnectionsResponse(ViewConnectionsActivity.connectionsList)) {
            Log.d(Utils.TAG, service + " returned invalid response");
            return false;
        }

        Log.d(Utils.TAG, service + " returned valid response");
        return true;
    }

    private static boolean validateUserResponse(String response) {
        return TextUtils.equals(response, Utils.VALID);
    }

    /**
     * Gets connection data from JSON response and creates a list of HashMap objects
     * <id, alias, location, datetime>
     *
     * @param stringBuilder
     * @return List<HashMap<String, String>> if success else null
     */
    private static List<HashMap<String, String>> parseConnectionResponse(StringBuilder stringBuilder) {
        List<HashMap<String, String>> connectionData = null;
        HashMap<String, String> map;

        Log.d(Utils.TAG, "parsing connection response");

        try {
            JSONObject jsonObj = new JSONObject(stringBuilder.toString());

            Boolean valid = (jsonObj.length() > 0);
            if(!valid) return null;

            connectionData = new ArrayList<HashMap<String, String>>();

            Log.d(Utils.TAG, "length: " + jsonObj.length());

            Iterator<String> keys = jsonObj.keys();
            while(keys.hasNext()) {
                String key = keys.next();

                JSONObject connection = jsonObj.getJSONObject(key);
                Log.d(Utils.TAG, key + ": " + connection.toString());

                map = new HashMap<String, String>();
                map.put("id", key);
                map.put("alias", connection.getString("alias"));
                map.put("location", connection.getString("location"));
                map.put("datetime", connection.getString("time_date"));
                connectionData.add(map);
            }

            return connectionData;
        } catch(JSONException e) {
            String message = "Error parsing connections JSON response: " + e.toString();
            Log.e(Utils.TAG, message);

            return null;
        }
    }

    /**
     * Validates connection data returned from PHP request.
     *
     * @param connectionData
     * @return true if not null else false
     */
    private static boolean validateConnectionsResponse(List<HashMap<String, String>> connectionData) {
        return connectionData != null;
    }

    /**
     * Indicates if there is a current user logged in
     *
     * @return true - current user logged exists, false - no current user
     */
    public static boolean isLoggedIn() {
        return LoginActivity.currentUser != null;
    }

    public static String getLocalIPAddress() {
		/*
		 * modified from:
		 *
		 * http://thinkandroid.wordpress.com/2010/03/27/incorporating-socket-programming-into-your-applications/
		 *
		 * */
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();

                    String iface = intf.getName();
                    if(iface.matches(".*" +p2pInt+ ".*")){
                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                            return getDottedDecimalIP(inetAddress.getAddress());
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        } catch (NullPointerException ex) {
            Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        }
        return null;
    }

    private static String getDottedDecimalIP(byte[] ipAddr) {
		/*
		 * ripped from:
		 *
		 * http://stackoverflow.com/questions/10053385/how-to-get-each-devices-ip-address-in-wifi-direct-scenario
		 *
		 * */
        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }
}


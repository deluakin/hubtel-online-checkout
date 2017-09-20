package com.hubtel.payments.Class;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import com.hubtel.payments.Interfaces.HttpDoneListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;

public class HTTPRequest extends AsyncTask<Object, String, String> {

	HttpDoneListener httpDoneListener;
	private Context mParent;
	String url = "";
	
	public HTTPRequest(Context parent, HttpDoneListener httpDoneListener) {
		super();
		this.httpDoneListener = httpDoneListener;
		this.mParent=parent;
	}
	
	@Override
	protected String doInBackground(Object... params) {
		HttpURLConnection urlConnection = null;
		int responseCode = 0;
		try {
			HashMap <String,String> myMap = (HashMap)params[0];
			Set <String> keys = myMap.keySet();
			String url_str = myMap.get("url");
			String clientid = myMap.get("clientid");
			String secretkey = myMap.get("secretkey");
			String data = myMap.get("data");
			URL url = new URL(url_str);
			String auth = clientid + ":" + secretkey;

			byte[] bytedata = auth.getBytes("UTF-8");
			auth = Base64.encodeToString(bytedata, Base64.DEFAULT);

			HttpURLConnection.setFollowRedirects(false);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setConnectTimeout(100000);
			urlConnection.setReadTimeout(100000);
			urlConnection.setDoOutput(false);
			urlConnection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
			urlConnection.setRequestProperty("Content-type", "application/json");
			urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			urlConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			urlConnection.addRequestProperty("Authorization", "Basic " + auth);
			//urlConnection.connect();

			if(data.trim().length() > 0) {
				urlConnection.setRequestMethod("POST");
				OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
				wr.write(data);

				wr.flush();
				wr.close();
			}else{
				urlConnection.setRequestMethod("GET");
			}

			responseCode = urlConnection.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}

			in.close();
			return response.toString();
		} catch (Exception e) {
			Log.e("Hubtel", e.getMessage());
	    } finally{
			urlConnection.disconnect();
	    }
	    return "";
	}
	
	@Override
	protected void onPostExecute(String result) {
		try {
			super.onPostExecute(result);
			httpDoneListener.onRequestCompleted(result);
		} catch(Exception ex){
			Log.e("Hubtel", ex.getMessage());
		}

	}
	 
	@Override
	protected void onPreExecute(){
		 super.onPreExecute();

	 }
	
}

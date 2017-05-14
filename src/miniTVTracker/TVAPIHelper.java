package miniTVTracker;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TVAPIHelper {
	
	private String apiKey = "BB80353BF520E440";
	
	private String baseRequestUrl = "https://api.thetvdb.com/";
	private String sessionToken;
	
	
	private void fetchSessionToken() {
		JSONObject jo = new JSONObject();
		jo.put("apikey", apiKey);
	
		URL obj;
		HttpsURLConnection con;
		
		try {
			String loginUrl = baseRequestUrl + "login";
			obj = new URL (loginUrl);
			con = (HttpsURLConnection)obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			con.setRequestProperty("Accept-Language", "en");
			con.setRequestProperty("Content-type", "application/json");

			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(jo.toJSONString());
			wr.flush();
			wr.close();
			
			
			int responseCode = con.getResponseCode();
			//System.out.println("Login in API Helper. Response: " + responseCode);
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inLine;
			String input = "";
			
			while((inLine = in.readLine()) != null) {
				input += inLine;
			}
			in.close();
			
			JSONParser parser = new JSONParser();
			JSONObject jo2;			
			jo2 = (JSONObject)(parser.parse(input));
			
			sessionToken = (String)jo2.get("token");
			System.out.println("Token is " + sessionToken);
			
		} catch (MalformedURLException mue) {
			System.out.println("Malformed URL in API Helper");
			return;
		} catch (IOException ioe) {
			System.out.println("IOExceptoin in API helper");
			return;
		} catch(ParseException pe) {
			System.out.println("ParseException in API Heper!");
			return;
		}
	}
	
	public JSONArray getShowsforTitle(String title) {
		String searchUrl = baseRequestUrl + "search/series" + "?name=" + title.replace(" ", "%20");
		URL obj;
		HttpsURLConnection con;
		
		try {
			obj = new URL(searchUrl);
			con = (HttpsURLConnection)obj.openConnection();
			
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			con.setRequestProperty("Accept-Language", "en");
			con.setRequestProperty("Authorization", "Bearer " + sessionToken);
			
			int responseCode = con.getResponseCode();
			
			if(responseCode != 200) {
				System.out.println("Unexpected response code in search: " + responseCode);
				System.out.println(con.getResponseMessage());
				return null;
			}
			
			BufferedReader in = new BufferedReader (new InputStreamReader(con.getInputStream()));
			
			String inputLine;
			String response = "";
			while((inputLine = in.readLine()) != null) {
				response += inputLine;
			}
			in.close();
			
			JSONParser parser = new JSONParser();
			JSONObject jo = (JSONObject)parser.parse(response);
			return (JSONArray)jo.get("data");
			
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
			
	}
	
	
	public JSONArray getEpisodesPage(int showID, int pageNumber) {
		String searchUrl = baseRequestUrl + "series/" + showID + "/episodes?page=" + pageNumber;
		URL obj;
		HttpsURLConnection con;
		
		try {
			obj = new URL (searchUrl);
			con = (HttpsURLConnection)obj.openConnection();
			
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			con.setRequestProperty("Accept-Language", "en");
			con.setRequestProperty("Authorization", "Bearer " + sessionToken);
			
			int responseCode = con.getResponseCode();
			if(responseCode != 200) {
				System.out.println("Unexpected response code in getEpisodes: " + responseCode);
				System.out.println(con.getResponseMessage());
				return null;
			}
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			
			String readLine;
			String response = "";
			
			while((readLine = in.readLine()) != null) {
				response += readLine;
			}
			
			JSONParser parser = new JSONParser();
			JSONObject jo = (JSONObject)parser.parse(response);
			
			return (JSONArray)jo.get("data");
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public JSONArray getActorsForShow(int showID) {
		String searchUrl = baseRequestUrl + "series/" + showID + "/actors";
		URL obj;
		HttpsURLConnection con;
		
		try {
			obj = new URL (searchUrl);
			con = (HttpsURLConnection)obj.openConnection();
			
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			con.setRequestProperty("Accept-Language", "en");
			con.setRequestProperty("Authorization", "Bearer " + sessionToken);
			
			int responseCode = con.getResponseCode();
			if(responseCode != 200) {
				System.out.println("Unexpected response code in getEpisodes: " + responseCode);
				System.out.println(con.getResponseMessage());
				return null;
			}
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			
			String readLine;
			String response = "";
			
			while((readLine = in.readLine()) != null) {
				response += readLine;
			}
			
			JSONParser parser = new JSONParser();
			JSONObject jo = (JSONObject)parser.parse(response);
			
			return (JSONArray)jo.get("data");
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	public TVAPIHelper() {
		fetchSessionToken();
		
		
	}

}

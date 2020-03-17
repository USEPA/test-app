package ToxPredictor.Application.Calculations;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;

import com.google.gson.JsonObject;

public class TestLogin {

static String urlLogin = "https://qed.epacdx.net/login/";
static String url = "https://qed.epacdx.net/cts/rest/metabolizer/run";

	
//  private List<String> cookies;
//  private HttpsURLConnection conn;

  private final String USER_AGENT = "Mozilla/5.0";

  
  
  void go() {

	  String csrftoken=getToken();
	  
	  JsonObject jo=new JsonObject();
	  jo.addProperty("username", "qeduser");
	  jo.addProperty("password", "ecoDomain2019");
	  jo.addProperty("csrfmiddlewaretoken", csrftoken);

	  try {
	    HttpPost httpPost = new HttpPost(new URI(urlLogin));
	    
	    StringEntity entity = new StringEntity(jo.toString());
	    httpPost.setEntity(entity);
	    httpPost.addHeader("Referer",urlLogin);
	    httpPost.addHeader("csrfmiddlewaretoken",csrftoken);
		
		
		BasicCookieStore bcookieStore = new BasicCookieStore();
	    BasicClientCookie cookie = new BasicClientCookie("csrfmiddlewaretoken", csrftoken);
	    cookie.setDomain(".epacdx.net");
	    cookie.setPath("/");
	    bcookieStore.addCookie(cookie);

	    HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(bcookieStore).build();
	    HttpResponse response= client.execute(httpPost);
		System.out.println(csrftoken);		 
		  
	  } catch (Exception ex) {
		  ex.printStackTrace();
	  }

  }
  
  void go2() {

	  String csrftoken=getToken();
	  
	  JsonObject jo=new JsonObject();
	  jo.addProperty("username", "qeduser");
	  jo.addProperty("password", "ecoDomain2019");
	  jo.addProperty("csrfmiddlewaretoken", csrftoken);

	  try {
	    HttpPost httpPost = new HttpPost(new URI(urlLogin));
	    
	    StringEntity entity = new StringEntity(jo.toString());
	    httpPost.setEntity(entity);
	    httpPost.addHeader("Referer",urlLogin);
	    httpPost.addHeader("csrfmiddlewaretoken",csrftoken);
		
		
		BasicCookieStore bcookieStore = new BasicCookieStore();
	    BasicClientCookie cookie = new BasicClientCookie("csrfmiddlewaretoken", csrftoken);
	    cookie.setDomain(".epacdx.net");
	    cookie.setPath("/");
	    bcookieStore.addCookie(cookie);

	    HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(bcookieStore).build();
	    HttpResponse response= client.execute(httpPost);
		System.out.println(csrftoken);		 
		  
	  } catch (Exception ex) {
		  ex.printStackTrace();
	  }

  }
  
  String getToken() {
	  CookieManager cookieManager = new CookieManager();
	  cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
	  CookieHandler.setDefault(cookieManager);

	  //creates url for the given string 
	  URL url = null;
	  try {
		  url = new URL(urlLogin);
		  //open's a connection with the url specified and returns URLConnection object
		  URLConnection  urlConnection = url.openConnection(); 
		  // get's the contents from this url specifies
		  urlConnection.getContent(); 
	  } catch (MalformedURLException e) {
		  // TODO Auto-generated catch block
		  e.printStackTrace();
	  } catch (IOException e) {
		  e.printStackTrace();
	  }			

	  //returns the cookie store(bunch of cookies)
	  CookieStore cookieStore = (CookieStore) cookieManager.getCookieStore();

	  //getting cookies which returns in the form of List of type HttpCookie
	  List<HttpCookie> listOfcookies = cookieStore.getCookies();

	  for(HttpCookie httpCookie: listOfcookies){
		  System.out.println("Cookie Name : "+httpCookie.getName()+" Cookie Value : "+httpCookie.getValue());
	  }

	  String csrftoken=cookieStore.getCookies().get(0).getValue();
	  
	  return csrftoken;
  }
  
  public static void main(String[] args) throws Exception {
	TestLogin testLogin = new TestLogin();
	testLogin.go();
	
  }
}


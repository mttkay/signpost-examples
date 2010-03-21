import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class FireEagleMain {

    public static void main(String[] args) throws Exception {

        OAuthConsumer consumer = new CommonsHttpOAuthConsumer("2qnk0OzpuzzU",
                "Ctp1QtFbtSaFhOJbOLMCUPio9c75zIaG");

        OAuthProvider provider = new CommonsHttpOAuthProvider(
                "https://fireeagle.yahooapis.com/oauth/request_token",
                "https://fireeagle.yahooapis.com/oauth/access_token",
                "https://fireeagle.yahoo.net/oauth/authorize");

        System.out.println("Fetching request token from Fire Eagle...");

        // we do not support callbacks, thus pass OOB
        String authUrl = provider.retrieveRequestToken(consumer, "http://www.example.com");

        System.out.println("Request token: " + consumer.getToken());
        System.out.println("Token secret: " + consumer.getTokenSecret());

        System.out.println("Now visit:\n" + authUrl + "\n... and grant this app authorization");
        System.out.println("Enter the verification code and hit ENTER when you're done");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String code = br.readLine();

        System.out.println("Fetching access token from Fire Eagle...");

        provider.retrieveAccessToken(consumer, code);

        System.out.println("Access token: " + consumer.getToken());
        System.out.println("Token secret: " + consumer.getTokenSecret());

        HttpPost request = new HttpPost("https://fireeagle.yahooapis.com/api/0.1/update");
        StringEntity body = new StringEntity("city=hamburg&label="
                + URLEncoder.encode("Send via Signpost!", "UTF-8"));
        body.setContentType("application/x-www-form-urlencoded");
        request.setEntity(body);

        consumer.sign(request);

        System.out.println("Sending update request to Fire Eagle...");

        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(request);

        System.out.println("Response: " + response.getStatusLine().getStatusCode() + " "
                + response.getStatusLine().getReasonPhrase());
    }
}

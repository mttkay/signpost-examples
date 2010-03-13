import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;

public class NetflixMain {

    public static final String CONSUMER_KEY = "uam9fsma9a87w7jm3v2r9n47";
    public static final String CONSUMER_SECRET = "Uze4zNHbJM";
    public static final String APPLICATION_NAME = "signpost-test";

    public static final String NETFLIX_REQUEST_TOKEN_URL = "http://api.netflix.com/oauth/request_token";
    public static final String NETFLIX_ACCESS_TOKEN_URL = "http://api.netflix.com/oauth/access_token";
    public static final String NETFLIX_AUTHORIZE_URL = "https://api-user.netflix.com/oauth/login";

    public static void main(String[] args) throws Exception {

        OAuthConsumer consumer = new DefaultOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);

        OAuthProvider provider = new DefaultOAuthProvider(NETFLIX_REQUEST_TOKEN_URL,
            NETFLIX_ACCESS_TOKEN_URL, NETFLIX_AUTHORIZE_URL);

        System.out.println("Fetching request token from Netflix...");

        // we do not support callbacks, thus pass OOB
        String authUrl = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);
        authUrl = OAuth.addQueryParameters(authUrl, OAuth.OAUTH_CONSUMER_KEY, CONSUMER_KEY,
            "application_name", APPLICATION_NAME);

        System.out.println("Request token: " + consumer.getToken());
        System.out.println("Token secret: " + consumer.getTokenSecret());

        System.out.println("Now visit:\n" + authUrl + "\n... and grant this app authorization");
        System.out.println("Enter the PIN code and hit ENTER when you're done:");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String pin = br.readLine();

        System.out.println("Fetching access token from Twitter...");

        provider.retrieveAccessToken(consumer, pin);

        System.out.println("Access token: " + consumer.getToken());
        System.out.println("Token secret: " + consumer.getTokenSecret());

        URL url = new URL("http://api.netflix.com/catalog/titles");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();

        consumer.sign(request);

        System.out.println("Sending request...");
        request.connect();

        System.out.println("Response: " + request.getResponseCode() + " "
                + request.getResponseMessage());

    }
}
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SalesforceMain {

    // Replace with your own values. Obtain by going to Salesforce: Setup | Develop | Remote Access
    private static final String  CONSUMER_KEY    = "3MVG9ytVT1SanXDmWxKyYiK4y8PyWwP.2ZSwq_9jWJiZLGbPietWG6DX2i7zUgXdjGeVvR6qpgwUrcjS8TaqQ";
    private static final String  CONSUMER_SECRET = "6876498509896764894";
    private static final String  SFDC_HOST       = "login.salesforce.com";
    private static final ApiType API_TYPE        =  ApiType.PARTNER;

    private static final String REQUEST_TOKEN_ENDPOINT_URL = "https://" + SFDC_HOST + "/_nc_external/system/security/oauth/RequestTokenHandler";
    private static final String ACCESS_TOKEN_ENDPOINT_URL  = "https://" + SFDC_HOST + "/_nc_external/system/security/oauth/AccessTokenHandler";
    private static final String AUTHORIZATION_WEBSITE_URL  = "https://" + SFDC_HOST + "/setup/secur/RemoteAccessAuthorizationPage.apexp?oauth_consumer_key=" + CONSUMER_KEY;
    private static final String SFDC_LOGIN_URL             = "https://" + SFDC_HOST + "/services/OAuth/" + API_TYPE.code + "/22.0";

    public static void main(String[] args) throws Exception {
        final OAuthConsumer consumer = new DefaultOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
        final OAuthProvider provider = new DefaultOAuthProvider(REQUEST_TOKEN_ENDPOINT_URL, ACCESS_TOKEN_ENDPOINT_URL, AUTHORIZATION_WEBSITE_URL);

        System.out.println("Fetching request token...");

        final String authUrl = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);

        System.out.println("Request token: " + consumer.getToken());
        System.out.println("Token secret: " + consumer.getTokenSecret());

        System.out.println("Now visit:\n" + authUrl + "\n... and grant this app authorization");
        System.out.println("Enter the verification code and hit ENTER when you're done:");

        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        final String verificationCode = br.readLine();

        System.out.println("Fetching access token...");

        provider.retrieveAccessToken(consumer, verificationCode.trim());

        System.out.println("Access token: " + consumer.getToken());
        System.out.println("Token secret: " + consumer.getTokenSecret());

        // Subsequent calls to Salesforce will fail if verification code is set
        // after getting the access token, so remove it from additional parameters
        consumer.setAdditionalParameters(null);

        // Salesforce does not use the OAuth token directly for regular API calls.
        // You must first make a special login API call to get a serverUrl and sessionId
        final URL loginUrl = new URL(SFDC_LOGIN_URL);
        final HttpURLConnection request = (HttpURLConnection) loginUrl.openConnection();
        request.setRequestMethod("POST");

        consumer.sign(request);

        System.out.println("Sending request...");
        request.connect();

        System.out.println("Response: " + request.getResponseCode() + " " + request.getResponseMessage());

        final String loginResult = new Scanner(request.getInputStream()).useDelimiter("\\A").next();
        System.out.println("Login Result: " + loginResult);

        // Poor man's XML parsing. We only care about these two values in the response, so just using simple regex.
        final Pattern loginResultPattern = Pattern.compile(".*<serverUrl>(.*)</serverUrl>.*<sessionId>(.*)</sessionId>.*");
        final Matcher loginResultMatcher = loginResultPattern.matcher(loginResult);
        loginResultMatcher.matches();
        final String serverUrl = loginResultMatcher.group(1);
        final String sessionId = loginResultMatcher.group(2);

        System.out.println("Server Url: " + serverUrl);
        System.out.println("Session Id: " + sessionId);
    }

    private static enum ApiType {
        PARTNER('u'),
        ENTERPRISE('c');

        public final char code;

        ApiType(char code) {
            this.code = code;
        }
    }
}

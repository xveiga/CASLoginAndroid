package com.example.caslogin.data;

import android.util.Log;

import com.example.caslogin.data.model.LoggedInUser;
import com.example.caslogin.data.utils.RegexUtil;
import com.example.caslogin.data.utils.httpclient.HttpClient;
import com.example.caslogin.data.utils.httpclient.HttpClientFactory;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    private static final String LOG_TAG = "LoginDataSource";

    private static final String moodleServiceParam = "https://moodle.udc.es/login/index.php?authCAS=CAS";
    private static final String moodleLoginConfirm = "\"wwwroot\":\"https:\\/\\/moodle.udc.es\",\"sesskey\":";
    private static final String moodlePrefsPage = "https://moodle.udc.es/user/preferences.php";
    private static final String moodleTokenPage = "https://moodle.udc.es/user/managetoken.php?sesskey=";

    /*
    Login process:
        1. Create HttpClient instance, it will contains all cookies for session tracking.
        It's necessary to indicate the URL of the service, to prevent unwanted redirects to the
        login page again (see CASLogin notes for more information).
        2. Let CASLogin authenticate. The HttpClient then will have access to that service.
        3. Use that HttpClient instance to navigate to the moodle user preferences (moodlePrefsPage).
        4. Find the link to the "security keys" (tokens) page, and get the sesskey parameter on the url.
        5. Get the token management page (moodleTokenPage), appending the sesskey.
        6. Parse the elements of the table to retrieve the token (could also save the reset links).
        7. Let CASLogin logout to invalidate the session on the server.
        8. Destroy HttpClient instance, will free memory if curl is being used.
        9. Return Result.Success with wanted data.
     */
    public Result<LoggedInUser> login(String username, String password) {
        try {
            // 1. Create JavaHttpClient and CASLogin instances
            HttpClient http = HttpClientFactory.getInstance();
            CASLogin cas = new CASLogin(http);

            // 2. Authenticate
            //cas.login(username, password); // Does not work with moodle, it asks for auth again.
            cas.login(moodleServiceParam, username, password, moodleLoginConfirm);

            // 3. Navigate to preferences page
            String result = http.httpsGet(moodlePrefsPage);

            // 4. Get the session key for next step
            String sessKey = RegexUtil.findRegex(result, "(?<=,\"sesskey\":\")[a-zA-Z0-9]*(?=\",)", 0);

            // 5. Navigate to security keys page
            result = http.httpsGet(moodleTokenPage + sessKey);

            // 6. Parse the elements of the token table (for now just first token)
            String token = RegexUtil.findRegex(result, "(?<=<td class=\"cell c0\" style=\"text-align:left;\">)[a-zA-Z0-9]*?(?=<\\/td>)", 0);

            // TODO: Remove, user data should be retrieved using moodleinfo function.
            String fullname = RegexUtil.findRegex(result, "(?<=<em><span aria-hidden=\"true\" class=\"fa fa-user\"><\\/span>).*?(?=<\\/em>)", 0);

            LoggedInUser user = new LoggedInUser((long) 0, token, fullname);

            // 7. Log out session for moodle service
            cas.logout(moodleServiceParam);

            // 8. Destroy HttpClient instance
            http.destroy();

            return new Result.Success<>(user);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception: " + e.toString());
            e.printStackTrace();
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // No need to logout as it is a "permanent" token that needs to be reset manually.
    }
}

package com.example.caslogin.data;

import android.util.Log;

import com.example.caslogin.data.model.LoggedInUser;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String curlTest();

    private static final String LOG_TAG = "LoginDataSource";

    private static final String moodleServiceParam = "https://moodle.udc.es/login/index.php?authCAS=CAS";

    /*
    Login process:
        1. Create NativeHttpClient instance, it will contains all cookies for session tracking.
        It's necessary to indicate the URL of the service, to prevent unwanted redirects to the
        login page again (see CASLogin notes for more information).
        2. Let CASLogin authenticate. The NativeHttpClient then will have access to that service.
        3. Use that NativeHttpClient instance to navigate to the moodle user preferences
        (https://moodle.udc.es/user/preferences.php).
        4. Find the link to the "security keys" (tokens) page, and get the sesskey parameter on the url.
        5. Get the token management page (https://moodle.udc.es/user/managetoken.php?sesskey=),
        appending the sesskey.
        6. Parse the elements of the table to retrieve the token (could also save the reset links).
        7. Let CASLogin logout to invalidate the session on the server.
        8. Return Result.Success with wanted data.
     */
    public Result<LoggedInUser> login(String username, String password) {
        Log.v("login", username);
        try {
            /*
            // 1. Create NativeHttpClient and CASLogin instances
            NativeHttpClient http = new NativeHttpClient();
            CASLogin cas = new CASLogin(http);

            // 2. Authenticate
            cas.login(moodleServiceParam, username, password);
            http.setHostActive(true);
            http.setHost("moodle.udc.es");

            // 3. Navigate to preferences page
            String prefsPage = http.httpsGet("https://moodle.udc.es/user/preferences.php");
            Log.v(LOG_TAG, prefsPage);
            LoggedInUser user = new LoggedInUser((long) 0, "Jane Doe");

            // 7. Log out session for moodle service
            cas.logout(moodleServiceParam);*/

            // CURL TEST. TODO: Use a factory to choose between Java HTTP client and curl
            String curlResult = curlTest();
            Log.e(LOG_TAG, curlResult);
            if (curlResult == null || curlResult.isEmpty()) throw new Exception("Curl Failure, check logs");
            LoggedInUser user = new LoggedInUser((long) 0, curlResult.substring(0, 50));
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

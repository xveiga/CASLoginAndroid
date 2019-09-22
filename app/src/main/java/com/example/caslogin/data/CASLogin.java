package com.example.caslogin.data;

import android.util.Log;

import com.example.caslogin.data.utils.HttpClient;
import com.example.caslogin.data.utils.HttpConstants;
import com.example.caslogin.data.utils.exceptions.HttpClientException;
import com.example.caslogin.data.utils.exceptions.URLEncodingException;
import com.example.caslogin.data.utils.exceptions.UnexpectedHTTPStatusCode;

import java.io.IOException;
import java.net.URLEncoder;

import static com.example.caslogin.data.utils.RegexUtil.findRegex;

public class CASLogin {

    /**
     *  This class contains methods to allow login and logout functions on UDC's Central Authentication System (CAS).
     *  It requires a HttpClient instance, which is used to navigate through CAS's web page. The instance remains authenticated afterwards.
     */

    /*
        Login Process:
            1. Navigate to CAS page specifying the service we want to log in. If no service is specified
            the login will be successful but "generic". When connecting to some services
            (moodle.udc.es or espazos.udc.es) we may get redirected back to login again just for that service.
            2. Extract login form action URL, it will be different depending on the server or include a cookie.
            3. Include username, password and hidden fields in POST request to that URL.
            4. Parse response to detect successful or failed login.

         Logout Process:
            1. Navigate to CAS logout page, optionally specifying the service.
            2. Parse response to detect successful logout.
     */

    private static final String baseURL = "https://cas.udc.es"; // Base URL for service
    private static final String loginPage = "/login"; // Login page
    private static final String logoutPage = "/logout"; // Logout page

    private static final String formActionRegex = "<form id=\"fm1\" class=\"fm-v\" action=\"(.*?)\" method=\"post\">"; // Regex to find login form POST URL
    private static final String[] formKeys = {"username", "password", "lt", "_eventId", "submit_button"}; // Form values expected

    private static final String loginConfirmation = "Inicio de sesión correcto"; // Page must contain this to consider it a successful login
    private static final String loginWrongCredentials = "As credenciais proporcionadas non parecen correctas.";
    private static final String logoutConfirmation = "A súa sesión foi pechada correctamente"; // Same as above for logout

    private HttpClient httpClient; // HttpClient instance that will be authenticated

    public CASLogin(HttpClient client) {
        httpClient = client;
    }

    public void login(String service, String username, String password) throws IOException, UnexpectedHTTPStatusCode, URLEncodingException, HttpClientException, CASLoginException {
        String url = "";
        if (service != null && !service.isEmpty())
            url = URLEncoder.encode("?service=", HttpConstants.HTTP_ENCODING.name());
        Log.v("CASLogin", "urlparams:" + url);
        httpClient.setHostActive(true);
        httpClient.setHost("cas.udc.es");
        String loginContent = httpClient.httpsGet(baseURL + loginPage + url);
        String actionUrl = findRegex(loginContent, formActionRegex, 1);
        String[] formValues = {null, null, "e1s1", "submit", "Iniciar+sesión"};
        formValues[0] = username;
        formValues[1] = password;
        String parameters = httpClient.encodeParameters(formKeys, formValues); // Encode parameters to send through HTTP.
        String loginResponse = httpClient.httpsPostForm(baseURL + actionUrl, parameters.toCharArray()); //TODO: Remove toCharArray. Was used in Java implementation to protect passwords.
        Log.v("CASLogin", "webpage:" + loginResponse);
        if (!loginResponse.contains(loginConfirmation)) {
            if (loginResponse.contains(loginWrongCredentials))
                throw new CASLoginException("Wrong credentials");
            else
                throw new CASLoginException("Unknown error during login");
        }
    }

    public void logout(String service) throws IOException, UnexpectedHTTPStatusCode, CASLoginException {
        String url = "";
        if (service != null && !service.isEmpty())
            url = URLEncoder.encode("?service=", HttpConstants.HTTP_ENCODING.name());
        String logoutContent = httpClient.httpsGet(baseURL + logoutPage + url);
        Log.v("CASLogin", "webpage:" + logoutContent);
        if (!logoutContent.contains(logoutConfirmation))
            throw new CASLoginException("Unknown error during logout");
    }

    public HttpClient getHttpClient () {
        return httpClient;
    }

}
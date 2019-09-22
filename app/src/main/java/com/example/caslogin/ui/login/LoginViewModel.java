package com.example.caslogin.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.os.AsyncTask;
import android.util.Patterns;

import com.example.caslogin.data.LoginRepository;
import com.example.caslogin.data.Result;
import com.example.caslogin.data.model.LoggedInUser;
import com.example.caslogin.R;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password) {
        // can be launched in a separate asynchronous job
        new LoginTask().execute(username, password);
    }

    public void loginDataChanged(String username, String password) {
        if (username.isEmpty()) {
            loginFormState.setValue(new LoginFormState(null, null));
        } else if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (password.isEmpty()) {
            loginFormState.setValue(new LoginFormState(null, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        //return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        if (username == null || username.contains("@")) {
            return false;
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 0;
    }

    private class LoginTask extends AsyncTask<String, Void, Void> {

        private Result<LoggedInUser> result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... v) {
            // This method runs asynchronously
            result = loginRepository.login(v[0], v[1]);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            // This method runs on the UI thread
            if (result instanceof Result.Success) {
                LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
            } else {
                loginResult.setValue(new LoginResult(R.string.login_failed));
            }
            super.onPostExecute(v);
        }

        @Override
        protected void onCancelled(Void v) {
            super.onPostExecute(v);
        }
    }
}

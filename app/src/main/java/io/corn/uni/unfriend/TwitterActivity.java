package io.corn.uni.unfriend;

import android.app.ProgressDialog;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;


public class TwitterActivity extends ActionBarActivity implements TwitterList.OnFragmentInteractionListener, Notifiable, twitter_result.OnFragmentInteractionListener{

    private TwitterLoginButton loginButton;
    private View twitterListView;
    TwitterSession session;
    TwitterList twitterList;
    ProgressDialog pd;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter);

        twitterListView = findViewById(R.id.twitter_list);
        twitterListView.setVisibility(View.GONE);

        twitterList = (TwitterList) getFragmentManager().findFragmentById(R.id.twitter_list);

        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        loginButton.setVisibility(View.GONE);
        showAuthButton();

    }

    private void showAuthButton() {
        if (!isAuthed()){
            loginButton.setVisibility(View.VISIBLE);
            loginButton.setCallback(new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {
                    session = result.data;
                    Twitter.getSessionManager().setActiveSession(session);
                    loginButton.setVisibility(View.GONE);
                    goToListView();
                }

                @Override
                public void failure(TwitterException exception) {
                    System.err.println("Failed to login to Twitter!");
                    // TODO: exit the app or something
                }
            });
        } else {

            goToListView();
        }
    }

    private boolean isAuthed() {
        final TwitterSession session = Twitter.getSessionManager().getActiveSession();
        return session != null && session.getAuthToken() != null;
    }
    private void goToListView() {
        assert isAuthed(): "The user should be authenticated";
        showLogout();
        pd = new ProgressDialog(this);
        pd.setTitle("Loading");
        pd.setMessage("Wait while loading...");
        pd.show();
        // TODO: start processing the list and hide the thingy when doen
        FriendContainer fc = new FriendContainer();
        FriendsRetriever fr = new FriendsRetriever(fc, twitterList, this);
        twitterList.attachFriendContainer(fc);
        fr.getIds(Twitter.getSessionManager().getActiveSession().getUserId());
        twitterListView.setVisibility(View.VISIBLE);
    }

    private void showLogout() {
        final View sign_out_bt = findViewById(R.id.twitter_signout);
        sign_out_bt.setVisibility(View.VISIBLE);
        sign_out_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Twitter.getSessionManager().clearActiveSession();
                sign_out_bt.setVisibility(View.INVISIBLE);
                twitterListView.setVisibility(View.GONE);
                showAuthButton();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_twitter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(String id) {

    }

    @Override
    public void allDone() {
//        pd.hide();
        pd.dismiss();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}

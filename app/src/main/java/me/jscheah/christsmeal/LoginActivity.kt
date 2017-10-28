package me.jscheah.christsmeal

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity

import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async

/**
 * A login screen that offers login via crsid/password.
 */
class LoginActivity : AppCompatActivity() {
    // UI references.
    private var mCrsIdView: AutoCompleteTextView? = null
    private var mPasswordView: EditText? = null
    private var mProgressView: View? = null
    private var mLoginFormView: View? = null

    private var mSharedPrefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        setContentView(R.layout.activity_login)
        // Set up the login form.
        mCrsIdView = raven_id

        mPasswordView = password
        mPasswordView!!.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        val mEmailSignInButton = raven_sign_in_button
        mEmailSignInButton.setOnClickListener { attemptLogin() }

        mLoginFormView = findViewById(R.id.login_form)
        mProgressView = findViewById(R.id.login_progress)
    }


    /**
     * Attempts to sign into Raven with credentials specified by the login form.
     * If there are form errors (missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        raven_sign_in_button.isEnabled = false
        // Reset errors.
        mCrsIdView!!.error = null
        mPasswordView!!.error = null

        // Store values at the time of the login attempt.
        val email = mCrsIdView!!.text.toString()
        val password = mPasswordView!!.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView!!.error = getString(R.string.error_field_required)
            focusView = mPasswordView
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mCrsIdView!!.error = getString(R.string.error_field_required)
            focusView = mCrsIdView
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
            raven_sign_in_button.isEnabled = true
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            async(UI) {
                val loginSuccess = Network.shibbolethLogin(email, password, true)
                if (loginSuccess) {
                    mSharedPrefs!!.edit()
                            .putString(getString(R.string.pref_raven_username), email)
                            .putString(getString(R.string.pref_raven_password), password)
                            .putBoolean(getString(R.string.pref_raven_authenticated), true)
                            .apply()
                    Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                } else {
                    mPasswordView!!.error = getString(R.string.error_incorrect_password)
                    mPasswordView!!.requestFocus()
                }
                showProgress(false)
                raven_sign_in_button.isEnabled = true
            }
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

            mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
            mLoginFormView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
                    (if (show) 0 else 1).toFloat()).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
                }
            })

            mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
            mProgressView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
                    (if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
                }
            })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
            mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
        }
    }
}


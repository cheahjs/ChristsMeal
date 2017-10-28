package me.jscheah.christsmeal

import android.util.Log
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.CookiePersistor
import kotlinx.coroutines.experimental.async
import okhttp3.*
import org.jsoup.Jsoup
import java.util.*


/**
 *
 */
object Network {
    // Constants
    private val TAG = "Network"
    private val RAVEN_AUTH_ROOT_URL = "https://raven.cam.ac.uk/auth/"
    private val RAVEN_AUTH_PAGE_PATH = "authenticate.html"
    private val RAVEN_AUTH_SUBMIT_PATH = "authenticate2.html"
    private val CHRISTS_SHIBBOLETH_URL = "https://intranet.christs.cam.ac.uk/Shibboleth.sso/Login?target=https%3A%2F%2Fintranet.christs.cam.ac.uk%2F"
    private val CHRISTS_INTRANET_HOST = "intranet.christs.cam.ac.uk"
    private val SHIBBOLETH_SSO_REDIRECT_URL = "https://shib.raven.cam.ac.uk/idp/profile/SAML2/Redirect/SSO"
    private val CHRISTS_INTRANET_SUBMIT_URL = "https://intranet.christs.cam.ac.uk/Shibboleth.sso/SAML2/POST"


    private val cookieJar = PersistentCookieJar(SetCookieCache(), object: CookiePersistor {
        override fun saveAll(cookies: MutableCollection<Cookie>?) {}

        override fun removeAll(cookies: MutableCollection<Cookie>?) {}

        override fun clear() {}

        override fun loadAll(): MutableList<Cookie> = LinkedList()
    })

    private val httpClient = OkHttpClient.Builder()
            .cookieJar(cookieJar).build()

    data class NetResult(val status: Boolean, val response: Response?)

    /*
    Expected auth flow:
    - (Shibboleth login) https://intranet.christs.cam.ac.uk/Shibboleth.sso/Login?target=https%3A%2F%2Fintranet.christs.cam.ac.uk%2F
     | - https://shib.raven.cam.ac.uk/idp/profile/SAML2/Redirect/SSO?SAMLRequest=
     | - https://shib.raven.cam.ac.uk/idp/AuthnEngine
     | - https://shib.raven.cam.ac.uk/idp/Authn/RemoteUser
     |\
     | \ (Raven login) Raven authentication required
     | | - https://raven.cam.ac.uk/auth/authenticate.html?
     | | - POST https://raven.cam.ac.uk/auth/authenticate2.html
     | | - https://shib.raven.cam.ac.uk/idp/Authn/RemoteUser?WLS-Response=
     | | - https://shib.raven.cam.ac.uk/idp/Authn/RemoteUser
     | /
     |/
     | - (Shibboleth redirect) https://shib.raven.cam.ac.uk/idp/profile/SAML2/Redirect/SSO
     | - POST https://intranet.christs.cam.ac.uk/Shibboleth.sso/SAML2/POST
     | - https://intranet.christs.cam.ac.uk/
     */

    /**
     * Handles the Raven login page
     *
     * @param originalResponse The [Response] that corresponds to the auth page.
     * @return A [NetResult] with status true if not redirected back to Raven
     */
    private suspend fun ravenLogin(crsid: String, password: String, originalResponse: Response): NetResult {
        Log.i(TAG, "ravenLogin: Starting raven login")
        val doc = Jsoup.parse(async { originalResponse.body()!!.string() }.await())
        val hiddenInputs = doc.select("input[type=hidden]")
        var requestBodyBuilder = FormBody.Builder()
                .add("userid", crsid)
                .add("pwd", password)
        for (inputElement in hiddenInputs) {
            requestBodyBuilder.add(inputElement.attr("name"), inputElement.attr("value"))
        }
        val requestBody = requestBodyBuilder.build()
        try {
            val ravenRequest = Request.Builder()
                    .url(RAVEN_AUTH_ROOT_URL + RAVEN_AUTH_SUBMIT_PATH)
                    .method("POST", requestBody)
                    .build()
            val response = async { httpClient.newCall(ravenRequest).execute() }.await()
            val finalUrl = response.request().url().toString()
            Log.d(TAG, "ravenLogin: Redirected to $finalUrl")
            return NetResult(RAVEN_AUTH_SUBMIT_PATH !in finalUrl, response)
        } catch (e: Exception) {
            Log.e("Network", "Raven login failed", e)
        }

        // Something has gone horribly wrong
        Log.w(TAG, "ravenLogin: Something has gone wrong")
        return NetResult(false, null)
    }

    /**
     * Handles the Raven->Intranet Shibboleth redirect
     *
     * @return A [NetResult] with status true if the redirect succeeds and returns to the intranet
     */
    private suspend fun shibbolethRedirect(originalResponse: Response): NetResult {
        Log.i(TAG, "shibbolethRedirect: Starting Shibboleth->Intranet redirect")
        val doc = Jsoup.parse(async { originalResponse.body()!!.string() }.await())
        val hiddenInputs = doc.select("input[type=hidden]")
        var requestBodyBuilder = FormBody.Builder()
        for (inputElement in hiddenInputs) {
            requestBodyBuilder.add(inputElement.attr("name"), inputElement.attr("value"))
        }
        val requestBody = requestBodyBuilder.build()
        try {
            val ravenRequest = Request.Builder()
                    .url(CHRISTS_INTRANET_SUBMIT_URL)
                    .method("POST", requestBody)
                    .build()
            val response = async { httpClient.newCall(ravenRequest).execute() }.await()
            val finalUrl = response.request().url().toString()
            Log.d(TAG, "shibbolethRedirect: Redirected to $finalUrl")
            return NetResult(CHRISTS_INTRANET_HOST in finalUrl, response)
        } catch (e: Exception) {
            Log.e("Network", "Raven login failed", e)
        }

        // Something has gone horribly wrong
        Log.w(TAG, "shibbolethRedirect: Something has gone wrong")
        return NetResult(false, null)
    }

    /**
     * Initiates a login to the Christs intranet via Shibboleth (via Raven)
     *
     * @param clearCookies Forces a flush of the cookie jar if true
     * @return True if login successful
     */
    suspend fun shibbolethLogin(crsid: String, password: String, clearCookies: Boolean = false): Boolean {
        Log.i(TAG, "shibbolethLogin: Starting login")
        if (clearCookies) {
            Log.i(TAG, "shibbolethLogin: Clearing cookies")
            cookieJar.clear()
        }
        try {
            // Create request for shibboleth login
            val shibRequest = Request.Builder().url(CHRISTS_SHIBBOLETH_URL).build()
            // Execute GET
            val response = async { httpClient.newCall(shibRequest).execute() }.await()
            // Grab the end redirect URL
            val shibFinalUrl = response.request().url().toString()

            Log.d(TAG, "shibbolethLogin: Redirected to $shibFinalUrl")

            when {
                RAVEN_AUTH_ROOT_URL + RAVEN_AUTH_PAGE_PATH in shibFinalUrl -> {
                    // Redirected to raven, requires auth
                    // Attempt to login to Raven
                    val (ravenLoginResult, ravenResponse) = ravenLogin(crsid, password, response)
                    if (!ravenLoginResult) {
                        // Raven login failed, invalid credentials, or network error
                        return false
                    }
                    if (SHIBBOLETH_SSO_REDIRECT_URL in ravenResponse!!.request().url().toString()) {
                        val (redirectResult, redirectResponse) = shibbolethRedirect(ravenResponse)
                        return redirectResult
                    }
                    return true
                }
                RAVEN_AUTH_ROOT_URL + RAVEN_AUTH_SUBMIT_PATH in shibFinalUrl -> {
                    // Invalid credentials?
                    Log.w(TAG, "shibbolethLogin: Redirect to auth2, invalid credentials")
                    return false
                }
                CHRISTS_INTRANET_HOST in shibFinalUrl -> {
                    // Successful login
                    Log.i(TAG, "shibbolethLogin: Redirected back to intranet")
                    return true
                }
                SHIBBOLETH_SSO_REDIRECT_URL in shibFinalUrl -> {
                    val (redirectResult, redirectResponse) = shibbolethRedirect(response)
                    return redirectResult
                }
                else -> {
                }
            }
        } catch (e: Exception) {
            Log.e("Network", "Shibboleth login failed", e)
        }

        // Something has gone horribly wrong
        Log.w(TAG, "shibbolethLogin: Something has gone wrong")
        return false
    }
}
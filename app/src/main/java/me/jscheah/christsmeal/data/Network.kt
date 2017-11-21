package me.jscheah.christsmeal.data

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.CookiePersistor
import kotlinx.coroutines.experimental.async
import me.jscheah.christsmeal.data.models.Booking
import me.jscheah.christsmeal.data.models.Transaction
import okhttp3.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.text.ParseException
import java.text.SimpleDateFormat
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
    private val CHRISTS_TRANSACTION_HISTORY_URL = "https://intranet.christs.cam.ac.uk/mealbooking/transactionhistory.php"
    private val CHRISTS_INTRANET_LOGIN_URL = "https://intranet.christs.cam.ac.uk/mealbooking/login.php"
    private val CHRISTS_MEALBOOKING_URL = "https://intranet.christs.cam.ac.uk/mealbooking/mealbooking.php"
    private val CHRISTS_FAILED_LOGIN_PATH = "failedlogin.php"
    private val CHRISTS_MENU_URL = "https://intranet.christs.cam.ac.uk/mealbooking/sittingmenu.php"

    private val cookieJar = PersistentCookieJar(SetCookieCache(), object: CookiePersistor {
        override fun saveAll(cookies: MutableCollection<Cookie>?) {}

        override fun removeAll(cookies: MutableCollection<Cookie>?) {}

        override fun clear() {}

        override fun loadAll(): MutableList<Cookie> = LinkedList()
    })

    private val httpClient = OkHttpClient.Builder()
            .cookieJar(cookieJar).build()

    private var lastLogin = -1L

    var crsId: String = ""
    var password: String = ""

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

     Meal booking auth flow:
     - https://intranet.christs.cam.ac.uk/mealbooking/login.php
      | - https://intranet.christs.cam.ac.uk/mealbooking/nojavascript.htm
      | - https://intranet.christs.cam.ac.uk/mealbooking/mealbooking.php
     */

    /**
     * Handles the Raven login page
     *
     * @param originalResponse The [Response] that corresponds to the auth page.
     * @return A [NetResult] with status true if not redirected back to Raven
     */
    private suspend fun ravenLogin(originalResponse: Response): NetResult {
        Log.i(TAG, "ravenLogin: Starting raven login")
        val doc = Jsoup.parse(async { originalResponse.body()!!.string() }.await())
        val hiddenInputs = doc.select("input[type=hidden]")
        var requestBodyBuilder = FormBody.Builder()
                .add("userid", crsId)
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
            Log.e("Network", "Raven login failed: ${e.message}", e)
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
            Log.e("Network", "shibbolethRedirect login failed: ${e.message}", e)
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
    suspend fun shibbolethLogin(clearCookies: Boolean = false): Boolean {
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
                    val (ravenLoginResult, ravenResponse) = ravenLogin(response)
                    if (!ravenLoginResult) {
                        // Raven login failed, invalid credentials, or network error
                        return false
                    }
                    if (SHIBBOLETH_SSO_REDIRECT_URL in ravenResponse!!.request().url().toString()) {
                        val (redirectResult, _) = shibbolethRedirect(ravenResponse)
                        if (redirectResult)
                            lastLogin = System.currentTimeMillis()
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
                    lastLogin = System.currentTimeMillis()
                    return true
                }
                SHIBBOLETH_SSO_REDIRECT_URL in shibFinalUrl -> {
                    val (redirectResult, _) = shibbolethRedirect(response)
                    if (redirectResult)
                        lastLogin = System.currentTimeMillis()
                    return redirectResult
                }
                else -> {
                }
            }
        } catch (e: Exception) {
            Log.e("Network", "Shibboleth login failed: ${e.message}", e)
        }

        // Something has gone horribly wrong
        Log.w(TAG, "shibbolethLogin: Something has gone wrong")
        return false
    }

    suspend fun mealBookingLogin(): Boolean {
        try {
            val request = Request.Builder()
                    .url(CHRISTS_INTRANET_LOGIN_URL)
                    .build()
            val response = async { httpClient.newCall(request).execute() }.await()
            val finalUrl = response.request().url().toString()
            Log.d(TAG, "mealBookingLogin: Redirected to $finalUrl")
            if (CHRISTS_FAILED_LOGIN_PATH in finalUrl) {
                Log.i(TAG, "mealBookingLogin: Failed login")
                return false
            }
            getMealBookingPage()
            return true
        } catch (e: Exception) {
            Log.e(TAG, "mealBookingLogin: ", e)
        }
        return false
    }

    private suspend fun getMealBookingPage(): String {
        try {
            val request = Request.Builder()
                    .url(CHRISTS_MEALBOOKING_URL)
                    .build()
            val response = async { httpClient.newCall(request).execute() }.await()
            val finalUrl = response.request().url().toString()
            Log.d(TAG, "getMealBookingPage: Redirected to $finalUrl")
            if (CHRISTS_FAILED_LOGIN_PATH in finalUrl) {
                Log.i(TAG, "getMealBookingPage: Failed login")
            } else {
                return async { response.body()!!.string() }.await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "getMealBookingPage: ", e)
        }
        throw LoginFailedException()
    }

    private fun loginValid() = (lastLogin > 0 && lastLogin + 10*60*1000 > System.currentTimeMillis())

    @Synchronized
    private suspend fun checkLogin(): Boolean {
        if (loginValid())
            return true
        return shibbolethLogin() && mealBookingLogin()
    }

    class LoginFailedException: Exception()
    class NetworkErrorException: Exception()

    data class Balances(val sundries: String, val meals: String, val journeys: String): Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                parcel.readString())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(sundries)
            parcel.writeString(meals)
            parcel.writeString(journeys)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Balances> {
            override fun createFromParcel(parcel: Parcel): Balances {
                return Balances(parcel)
            }

            override fun newArray(size: Int): Array<Balances?> {
                return arrayOfNulls(size)
            }
        }
    }

    suspend fun getBalances(): Balances {
        Log.i(TAG, "getBalances: Starting transaction fetch")
        if (!checkLogin())
            throw LoginFailedException()
        try {
            val request = Request.Builder()
                    .url(CHRISTS_TRANSACTION_HISTORY_URL)
                    .build()
            val response = async { httpClient.newCall(request).execute() }.await()
            val finalUrl = response.request().url().toString()
            Log.d(TAG, "getBalances: Redirected to $finalUrl")
            if (CHRISTS_TRANSACTION_HISTORY_URL !in finalUrl) {
                throw LoginFailedException()
            }
            val doc = Jsoup.parse(async { response.body()!!.string() }.await())

            return parseBalances(doc)
        } catch (e: Exception) {
            Log.e(TAG, "getBalances: ", e)
        }
        throw Exception()
    }

    private fun parseBalances(doc: Document): Balances {
        val balanceText = doc.select("h6[align=center]").find {
            "Current Balances:" in it.text()
        }
        // Sample text: Current Balances:-&nbsp; Sundries= -£79.70&nbsp;&nbsp;&nbsp; Meals= £0.00&nbsp;&nbsp;&nbsp; Journeys= -£9.00&nbsp;
        val splitParts = balanceText!!.text().split('\u00a0').map { it.trim() }
        val sundries = splitParts.find { it.startsWith("Sundries= ") }!!.replace("Sundries= ", "")
        val meals = splitParts.find { it.startsWith("Meals= ") }!!.replace("Meals= ", "")
        val journeys = splitParts.find { it.startsWith("Journeys= ") }!!.replace("Journeys= ", "")
        return Balances(sundries, meals, journeys)
    }

    suspend fun getTransactions(startDate: Date, endDate: Date): Pair<List<Transaction>, Balances> {
        Log.i(TAG, "getTransactions: Starting transaction fetch from $startDate to $endDate")
        if (!checkLogin())
            throw LoginFailedException()
        try {
            val requestBody = FormBody.Builder()
                    .add("Submit", "Get Transactions")
                    .add("hidWinNo", "2")
                    .add("hidDateFrom", SimpleDateFormat("dd/MM/yyyy", Locale.UK).format(startDate))
                    .add("hidDateTo", SimpleDateFormat("dd/MM/yyyy", Locale.UK).format(endDate))
                    .build()
            val request = Request.Builder()
                    .url(CHRISTS_TRANSACTION_HISTORY_URL)
                    .method("POST", requestBody)
                    .build()
            val response = async { httpClient.newCall(request).execute() }.await()
            val finalUrl = response.request().url().toString()
            Log.d(TAG, "getTransactions: Redirected to $finalUrl")
            if (CHRISTS_TRANSACTION_HISTORY_URL !in finalUrl) {
                throw LoginFailedException()
            }
            val doc = Jsoup.parse(async { response.body()!!.string() }.await())

            return Pair(parseTransactionRows(doc), parseBalances(doc))
        } catch (e: Exception) {
            Log.e(TAG, "getTransactions: $e", e)
            throw NetworkErrorException()
        }
    }

    private fun parseTransactionRows(doc: Document): List<Transaction> {
        val dateFormat = SimpleDateFormat("EEE, dd/MM/yyyy", Locale.UK)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.UK)
        val transactionList = LinkedList<Transaction>()

        var rawDate = ""
        var rawTime = ""
        var date: Date? = Date()
        var time: Long
        var groupIndex = 0

        val rows = doc.select(".Grid > tbody > tr")
        for (row in rows) {
            if (row.hasClass("Caption")) {
                groupIndex = 0
                val caption = row.child(0).text()
                rawDate = caption.substring(1).split('\u00a0')[0].trim()
                rawTime = caption.split('\u00a0').last().trim()
                date = dateFormat.parse(rawDate)
                time = try { timeFormat.parse(rawTime).time } catch(e: ParseException) { 0 }
                date = Date(date.time + time)
                Log.d(TAG, "parseTransactionRows: $rawDate ($rawTime) parsed as $date ($time)")
                continue
            }
            if (row.hasClass("Row")) {
                val children = row.children().map { it.text().trim() }
                transactionList.add(Transaction(rawDate, rawTime, date?.time ?: -1,
                        children[1], children[2], children[3], children[4], groupIndex++))
            }
            if (row.hasClass("SubTotal")) {
                continue
            }
        }
        return transactionList
    }

    suspend fun getMealBookings(): List<Booking> {
        Log.i(TAG, "getMealBookings: Starting fetch")
        if (!checkLogin())
            throw LoginFailedException()
        try {
            val request = Request.Builder()
                    .url(CHRISTS_MEALBOOKING_URL)
                    .build()
            val response = async { httpClient.newCall(request).execute() }.await()
            val finalUrl = response.request().url().toString()
            Log.d(TAG, "getMealBookings: Redirected to $finalUrl")
            if (CHRISTS_MEALBOOKING_URL !in finalUrl) {
                throw LoginFailedException()
            }
            val doc = Jsoup.parse(async { response.body()!!.string() }.await())

            return parseMealBookings(doc)
        } catch (e: Exception) {
            Log.e(TAG, "getMealBookings: $e")
            throw NetworkErrorException()
        }
    }

    private fun parseMealBookings(doc: Document): List<Booking> {
        val rows = doc.select(".GridAS1 > tbody > tr.RowAS1")
        val bookingList = LinkedList<Booking>()

        val dateFormat = SimpleDateFormat("EEE, dd/MM/yyyy", Locale.UK)

        for (row in rows) {
            val textChildren = row.children().map { it.text().replace("\u00a0", " ").trim() }
            val children = row.children()
            bookingList.add(
                    Booking(
                            children[11].child(0).attr("value"),
                            textChildren[0],
                            dateFormat.parse(textChildren[0]).time,
                            textChildren[2],
                            textChildren[4].replace(" )", ")"),
                            textChildren[6],
                            textChildren[8],
                            textChildren[10],
                            children[12].child(0).select("input[type=submit][name=btnBook]").isNotEmpty(),
                            children[12].child(0).select("input[type=submit][name=btnGuest]").isNotEmpty(),
                            children[13].child(0).select("input[type=submit]").isNotEmpty(),
                            children[14].child(0).select("input[type=submit]").isNotEmpty(),
                            children[15].child(0).select("input[type=submit]").isNotEmpty(),
                            children[16].child(0).select("input[type=submit]").isNotEmpty(),
                            children[17].child(0).select("input[type=submit]").isNotEmpty(),
                            message = children.last().select("input[type=hidden][name=message]").attr("value").trim()
                    )
            )
        }

        return bookingList.toList()
    }

    suspend fun getMenu(id: String): String {
        Log.i(TAG, "getMenu: Starting menu fetch for $id")
        try {
            val requestBody = FormBody.Builder()
                    .add("btnMenu", "Menu")
                    .add("sitUniq", id)
                    .build()
            val request = Request.Builder()
                    .url(CHRISTS_MENU_URL)
                    .method("POST", requestBody)
                    .build()
            val response = async { httpClient.newCall(request).execute() }.await()
            val finalUrl = response.request().url().toString()
            Log.d(TAG, "getMenu: Redirected to $finalUrl")

            val doc = Jsoup.parse(async { response.body()!!.string() }.await())

            return parseMenu(doc)
        } catch (e: Exception) {
            Log.e(TAG, "getBalances: ", e)
        }
        throw Exception()
    }

    private fun parseMenu(doc: Document): String {
        val rows = doc.select("table > tbody > tr > td > table > tbody > tr > td")
        return rows.joinToString("\n") { x -> x.text()}
    }
}
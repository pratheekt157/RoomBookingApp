package com.nineleaps.conferenceroombooking
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.nineleaps.conferenceroombooking.Helper.GoogleGSO
import com.nineleaps.conferenceroombooking.Helper.NetworkState
import com.nineleaps.conferenceroombooking.bookingDashboard.ui.UserBookingsDashboardActivity
import com.nineleaps.conferenceroombooking.checkConnection.NoInternetConnectionActivity
import com.nineleaps.conferenceroombooking.model.SignIn
import com.nineleaps.conferenceroombooking.signIn.repository.CheckRegistrationRepository
import com.nineleaps.conferenceroombooking.signIn.viewModel.CheckRegistrationViewModel
import com.nineleaps.conferenceroombooking.utils.*
import javax.inject.Inject

class SignIn : AppCompatActivity() {

    @Inject
    lateinit var mCheckRegistrationRepo: CheckRegistrationRepository
    @BindView(R.id.sin_in_progress_bar)
    lateinit var mProgressBar: ProgressBar
    private val RC_SIGN_IN = 0
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private lateinit var prefs: SharedPreferences
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var progressDialog: ProgressDialog
    private lateinit var mCheckRegistrationViewModel: CheckRegistrationViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        crashHandler()
        ButterKnife.bind(this)
        initialize()
        observeData()
    }

    private fun crashHandler() {
        val foreground :ForegroundCounter= ForegroundCounter().createAndInstallCallbacks(application)
        val defaultHandler:Thread.UncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler{ t: Thread?, e: Throwable? ->
            if (foreground.inForeground())
                defaultHandler.uncaughtException(t,e)
            else
                Handler(Looper.getMainLooper()).postAtFrontOfQueue ({ Runtime.getRuntime().exit(0) })
        }
    }

    @OnClick(R.id.sign_in_button)
    fun signIn() {
        startIntentToGoogleSignIn()
    }
    /**
     * function intialize all items of UI, sharedPreference and calls the setAnimationToLayout function to set the animation to the layouts
     */
    fun initialize() {
        initComponentForSignIn()
        prefs = getSharedPreferences(Constants.PREFERENCE, Context.MODE_PRIVATE)
        progressDialog = GetProgress.getProgressDialog(getString(R.string.progress_message_processing), this)
        mCheckRegistrationViewModel = ViewModelProviders.of(this).get(CheckRegistrationViewModel::class.java)
        initRegistrationRepo()
        initializeGoogleSignIn()
    }


    private fun initComponentForSignIn() {
        (application as BaseApplication).getmAppComponent()?.inject(this)
    }
    private fun initRegistrationRepo() {
        mCheckRegistrationViewModel.setCheckRegistrationRepo(mCheckRegistrationRepo)
    }
    /**
     * function will starts a explict intent for the google sign in
     */
    private fun startIntentToGoogleSignIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    /**
     * function will initialize the GoogleSignInClient
     */
    private fun initializeGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("518733103153-e8q3p435l90dghf61ctrdtjtd9kl85hv.apps.googleusercontent.com")
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

    }
    /**
     * function will automatically invoked once the control will return from the explict intent and than call another
     * method to do further task
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
        if (requestCode == Constants.RES_CODE && resultCode == Activity.RESULT_OK) {
            checkRegistration()
        }
    }
    /**
     * function will call a another function which connects to the backend.
     */
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            setTokenToAccessToken(account!!.idToken)
            Log.d("Google",account.idToken)
            if(NetworkState.appIsConnectedToInternet(this)) {
                checkRegistration()
            } else {
                val i = Intent(this@SignIn, NoInternetConnectionActivity::class.java)
                startActivityForResult(i, Constants.RES_CODE)
            }
        } catch (e: ApiException) {
            Log.w(getString(R.string.sign_in_error), "signInResult:failed code=" + e.statusCode)
        }
    }
    /**
     * Sign out from application
     */
    private fun signOut() {
        val mGoogleSignInClient = GoogleGSO.getGoogleSignInClient(this)
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this) {
                }
    }

    private fun saveCustomToken(idToken: String?) {
        val editor = prefs.edit()
        editor.putString(getString(R.string.token), "Bearer $idToken")
        editor.apply()
    }
    /**
     * on back pressed the function will clear the activity stack and will close the application
     */
    override fun onBackPressed() {
        finishAffinity()
        super.onBackPressed()
    }
    /**
     * this function will check whether the user is registered or not
     * if not registered than make an intent to registration activity
     */
    private fun checkRegistration() {
        progressDialog.show()
        mCheckRegistrationViewModel.checkRegistration(GetPreference.getDeviceIdFromPreference(this))
    }

    private fun setTokenToAccessToken(idToken: String?) {
        val preference = getSharedPreferences(Constants.PREFERENCE, Context.MODE_PRIVATE)
        val editor = preference.edit()
        editor.putString(getString(R.string.token), idToken)
        editor.apply()
    }
    /**
     * observe data from server
     */
    private fun observeData() {
        //positive response from server
        mCheckRegistrationViewModel.returnSuccessCode().observe(this, Observer {
            progressDialog.dismiss()
            mProgressBar.visibility = View.GONE
            setValueForSharedPreference(it)
        })
        // Negative response from server
        mCheckRegistrationViewModel.returnFailureCode().observe(this, Observer {
            progressDialog.dismiss()
            mProgressBar.visibility = View.GONE
            ShowToast.show(this, it as Int)
            signOut()
        })
    }
    /**
     * a function which will set the value in shared preference
     */
    private fun setValueForSharedPreference(it: SignIn?) {
        val editor = prefs.edit()
        val code : String = it!!.statusCode.toString()
        editor.putInt(Constants.ROLE_CODE,code.toInt())
        editor.apply()
        saveCustomToken(it.token)
        GetPreference.setJWTToken(this, it.refreshToken!!, it.token!!)
        intentToNextActivity(code.toInt())
    }
    /**
     * this function will intent to some activity according to the received data from backend
     */
    private fun intentToNextActivity(code: Int?) {
        when (code) {
            Constants.HR_CODE, Constants.Facility_Manager, Constants.MANAGER_CODE, Constants.EMPLOYEE_CODE -> {
                startActivity(Intent(this@SignIn, UserBookingsDashboardActivity::class.java))
                finish()
            }
            else -> {
                val builder =
                        GetAleretDialog.getDialog(this, getString(R.string.error), getString(R.string.restart_app))
                builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
                    finish()
                }
                GetAleretDialog.showDialog(builder)
            }
        }
    }
}
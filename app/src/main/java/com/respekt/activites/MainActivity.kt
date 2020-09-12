package com.respekt.activites

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.android.billingclient.api.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.respekt.R
import com.respekt.fragments.HomeFragment
import com.respekt.fragments.MeFragment
import com.respekt.fragments.PlayListFragment
import com.respekt.mediaplayer.LocalEventFromMainActivity
import com.respekt.mediaplayer.LocalEventFromMediaPlayerHolder
import com.respekt.mediaplayer.MediaPlayerHolder
import com.respekt.utils.MeData
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : AppCompatActivity(), PurchasesUpdatedListener {

    private val array = intArrayOf(
        R.raw.video1, R.raw.video2, R.raw.video3, R.raw.video4, R.raw.video5, R.raw.video6
    )
    val skuOneMonth = "com.respekt.one.month"
    private val skuList = listOf(skuOneMonth)

    private var mMediaPlayerHolder: MediaPlayerHolder? = null
    private lateinit var billingClient: BillingClient
    private lateinit var skuDetails: SkuDetails
    private var isExpiredOrCancelled = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setupFirebaseMessaging()
        setHomeFragment()
        randomVideoPlay()
        setupBilling()
        mMediaPlayerHolder = MediaPlayerHolder(this)

        btnHome.setOnClickListener {
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(
                R.id.frame_container,
                HomeFragment.newInstance("", ""),
                "PlayerList"
            )
            transaction.commit()
        }
        btnMe.setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction()
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            transaction.replace(R.id.frame_container, MeFragment.newInstance("", ""))
            transaction.addToBackStack(null)
            transaction.commit()
        }
        btnBeautification.setOnClickListener {
            navigateToPlaylist("BEAUTY MEDITATION", "1")
        }
        btnMusic.setOnClickListener {
            navigateToPlaylist("MUSIC", "3")
        }
        btnShortBreak.setOnClickListener {
            navigateToPlaylist("SHORT BREAK", "2")
        }
        btnSound.setOnClickListener {
            navigateToPlaylist("SOUND", "4")
        }
    }

    fun getBillingClient(): BillingClient {
        return billingClient
    }

    fun getSkuDetails(): SkuDetails {
        return skuDetails
    }

    private fun loadAllSKUs() = if (billingClient.isReady) {
        val params = SkuDetailsParams
            .newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.SUBS)
            .build()
        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchasesList != null) {
                Log.e("responseCode", billingResult.responseCode.toString())
                Log.e(" resultHistory", billingResult.toString())
                Log.e(" purchasesList", purchasesList.toString())
                if (purchasesList.isNotEmpty()) {
                    val queryPurchases = billingClient.queryPurchases(BillingClient.SkuType.SUBS)
                    Log.e("queryPurchases", queryPurchases.purchasesList.toString())
                    val list = queryPurchases.purchasesList.orEmpty()
                    if (list.isNotEmpty()) {
                        MeData.setPurchasedState(MeData.Purchased)
                    } else {
                        MeData.setPurchasedState(MeData.Expire)
                    }
                } else {
                    MeData.setPurchasedState(MeData.NotPurchased)
                }
            }
        }
        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            // Process the result.
            if (skuDetailsList != null) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList.isNotEmpty()) {
                    for (skuDetails in skuDetailsList) {
                        this.skuDetails = skuDetails
                        Log.e("SkuName", skuDetails.toString())
                        //this will return both the SKUs from Google Play Console
                    }
                }
            }
        }

    } else {
        println("Billing Client not ready")
    }

    private fun setupBilling() {
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener(this)
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override
            fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                     The BillingClient is ready. You can query purchases here.
                    loadAllSKUs(); // This is used to fetch purchased items from google play store
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.

//                showToast("Billing service disconnected...")
            }
        })
    }


    private fun navigateToPlaylist(categoryName: String, id: String) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(
            R.id.frame_container,
            PlayListFragment.newInstance(categoryName, id),
            "PlayerList"
        )
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun randomVideoPlay() {
        val path = "android.resource://" + packageName + "/" + array.random()
        videoView.setVideoURI(Uri.parse(path))
        videoView.start()
        videoView.setOnPreparedListener { mp -> mp.isLooping = true }
    }

    private fun setHomeFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, HomeFragment.newInstance("", ""))
        transaction.commit()
    }

    override fun onResume() {
        super.onResume()
        randomVideoPlay()
    }

    override fun onBackPressed() {
        super.onBackPressed()

    }

    fun shuffleVideo() {
        randomVideoPlay()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayerHolder?.release()

    }

    fun getMediaPlayerHolder(): MediaPlayerHolder? = mMediaPlayerHolder

    // Handle user input for button presses.
    fun pause() {
        EventBus.getDefault().post(LocalEventFromMainActivity.PausePlayback())
    }

    fun play() {
        EventBus.getDefault().post(LocalEventFromMainActivity.StartPlayback())
    }

    fun reset() {
        EventBus.getDefault().post(LocalEventFromMainActivity.ResetPlayback())
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LocalEventFromMediaPlayerHolder.UpdateLog) {
//        log(event.formattedMessage)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LocalEventFromMediaPlayerHolder.PlaybackDuration) {
//        mSeekbarAudio.setMax(event.duration)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LocalEventFromMediaPlayerHolder.PlaybackPosition) {
//        if (!isUserSeeking) {
//            mSeekbarAudio.setProgress(event.position, true)
//        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LocalEventFromMediaPlayerHolder.StateChanged) {
        /*Toast.makeText(
            this, java.lang.String.format("State changed to:%s", event.currentState),
            Toast.LENGTH_SHORT
        ).show()*/
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                acknowledgePurchase(purchase.purchaseToken)

            }
        } else if (billingResult?.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.e("Purchased", "Cancelled")
            // Handle an error caused by a user cancelling the purchase flow.

        } else {
            // Handle any other error codes.
        }
    }

    private fun acknowledgePurchase(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        MeData.setPurchasedState(MeData.Purchased)
        billingClient.acknowledgePurchase(params) { billingResult ->
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage
            Log.e("acknowledgePurchase", responseCode.toString() + debugMessage)

        }
    }

    fun setupFirebaseMessaging(){
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("Firebase", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                Log.e("Token:--", token.toString())


            })
    }
}
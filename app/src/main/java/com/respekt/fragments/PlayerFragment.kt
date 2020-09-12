package com.respekt.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.billingclient.api.BillingFlowParams
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.respekt.R
import com.respekt.activites.MainActivity
import com.respekt.mediaplayer.LocalEventFromMainActivity
import com.respekt.mediaplayer.LocalEventFromMediaPlayerHolder
import com.respekt.mediaplayer.MediaPlayerHolder
import com.respekt.models.DataItem
import com.respekt.utils.LastSession
import com.respekt.utils.MeData
import kotlinx.android.synthetic.main.fragment_player.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class PlayerFragment : Fragment(), PurchaseDialogFragment.OnClosePurchasedDialog {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var playItem: DataItem? = null
    private var isFromMiniPlayer: Boolean = false
    private var isUserSeeking = false
    var mContext: Context? = null
    var dailog: PurchaseDialogFragment? = null

    companion object {
        @JvmStatic
        fun newInstance(playItem: DataItem, param2: Boolean) =
            PlayerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("playItem", playItem)
                    putBoolean("isFromMiniPlayer", param2)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getParcelable<DataItem>("playItem")?.let {
            playItem = it
        }
        arguments?.getBoolean("isFromMiniPlayer")?.let {
            isFromMiniPlayer = it
        }
        Log.e(javaClass.name, "onCreate()")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.e(javaClass.name, "onCreateView()")

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e(javaClass.name, "onViewCreated()")
        (activity as MainActivity).shuffleVideo()

        btnShop.setOnClickListener {
            val transaction = activity?.supportFragmentManager?.beginTransaction()
//            transaction?.hide(activity?.supportFragmentManager?.findFragmentByTag("PlayerList")!!)
            transaction?.replace(R.id.frame_container, ShopFragment.newInstance("", ""))
            transaction?.addToBackStack(null)
            transaction?.commit()
        }



        playIfPurchased()

        if ((activity as MainActivity).getMediaPlayerHolder()?.isPlaying!!) {
            btnPlayPause.background = ContextCompat.getDrawable(
                activity!!,
                R.drawable.ic_pause
            )
        } else {
            btnPlayPause.background = ContextCompat.getDrawable(
                activity!!,
                R.drawable.ic_play
            )
        }
        var totalSession = MeData.getTotalSessions()?.toInt()
        totalSession = totalSession?.plus(1)

        val lastSession = MeData.getResentSession()

        lastSession?.list?.let {
            if (playItem?.musicTitle == "For Beginners") {
                it.removeAll { it?.musicTitle == "For Beginners" }
            }
            it.remove(playItem)
            it.add(0, playItem)
            MeData.setResentSession(LastSession(it))
        }

        MeData.setTotalSessions(totalSession.toString())
        tvMusicName.text = playItem?.musicTitle
        tvArtistName.text = playItem?.musicArtist
        tvCategoryName.text = playItem?.categoryName
        btnClick()
        Glide.with(activity!!)
            .load(playItem?.coverPhoto)
            .apply(RequestOptions())
            .fitCenter()
            .error(R.drawable.rounded_pink_layout)
            .placeholder(R.drawable.rounded_pink_layout)
            .into(ivPlay)

    }

    private fun playIfPurchased() {
        if (MeData.getPurchasedState() == MeData.Purchased) {
            dailog?.getPurchasedDialog()?.dismiss()
            (activity as MainActivity).getMediaPlayerHolder()?.load(playItem?.musicFile, false)
            (activity as MainActivity).getMediaPlayerHolder()?.dataItem = playItem
            reset()
        } else {
            if (dailog == null) {
                mContext?.let {
                    dailog = PurchaseDialogFragment()
                    dailog?.openDialog(
                        it,
                        false, this
                    )
                }
            }else{
                dailog?.getPurchasedDialog()?.show()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
    }

    private fun btnClick() {
        btnPlayPause.setOnClickListener {
            if ((activity as MainActivity).getMediaPlayerHolder()?.isPlaying!!) {
                pause()
            } else {
                play()
            }
        }
        btnRepeat.setOnClickListener {
            reset()
            play()
        }
        btnStop.setOnClickListener {
            stop()
        }
    }

    fun getDurationTimer(duration: Int, view: TextView) {
        val minutes: Int = duration / 1000 / 60
        val seconds = (duration / 1000 % 60)
        view.text = String.format("%02d:%02d", minutes, seconds)
    }

    // Handle user input for Seekbar changes.
    fun setupSeekbar() {
        seekbar_audio.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            // This holds the progress value for onStopTrackingTouch.
            var userSelectedPosition = 0
            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                // Only fire seekTo() calls when user stops the touch event.
                if (fromUser) {
                    userSelectedPosition = progress
                    isUserSeeking = true
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isUserSeeking = false
                EventBus.getDefault().post(
                    LocalEventFromMainActivity.SeekTo(
                        userSelectedPosition
                    )
                )
            }
        })
    }

    fun pause() {
        EventBus.getDefault().post(LocalEventFromMainActivity.PausePlayback())
    }

    fun play() {
        EventBus.getDefault().post(LocalEventFromMainActivity.StartPlayback())
    }

    fun stop() {
        EventBus.getDefault().post(LocalEventFromMainActivity.StopPlayback())
    }

    fun reset() {
        EventBus.getDefault().post(LocalEventFromMainActivity.ResetPlayback())
    }

    override fun onResume() {
        super.onResume()
        if (!(activity as MainActivity).getMediaPlayerHolder()?.isPlaying!!)
            playIfPurchased()
        Log.e(javaClass.name, "onResume()")
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
        Log.e(javaClass.name, "onStart()")
        EventBus.getDefault().register(this)

    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    // Event subscribers.
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LocalEventFromMediaPlayerHolder.UpdateLog) {
//        log(event.formattedMessage)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LocalEventFromMediaPlayerHolder.PlaybackDuration) {
        seekbar_audio.max = event.duration
        getDurationTimer(event.duration, tvTotalDuration)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LocalEventFromMediaPlayerHolder.PlaybackPosition) {
        if (!isUserSeeking) {
            seekbar_audio.progress = event.position
            getDurationTimer(event.position, tvCurrentDuration)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LocalEventFromMediaPlayerHolder.PreparedDone) {
//        reset()
        setupSeekbar()
        play()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LocalEventFromMediaPlayerHolder.StateChanged) {
        when (event.currentState) {
            MediaPlayerHolder.PlayerState.PLAYING -> {
                btnPlayPause.background = ContextCompat.getDrawable(
                    activity!!,
                    R.drawable.ic_pause
                )
            }
            MediaPlayerHolder.PlayerState.PAUSED -> {
                btnPlayPause.background = ContextCompat.getDrawable(
                    activity!!,
                    R.drawable.ic_play
                )
            }
            MediaPlayerHolder.PlayerState.COMPLETED -> {
                btnPlayPause.background = ContextCompat.getDrawable(
                    activity!!,
                    R.drawable.ic_play
                )
            }
            MediaPlayerHolder.PlayerState.RESET -> {
                btnPlayPause.background = ContextCompat.getDrawable(
                    activity!!,
                    R.drawable.ic_play
                )
            }
            MediaPlayerHolder.PlayerState.STOP -> {
                btnPlayPause.background = ContextCompat.getDrawable(
                    activity!!,
                    R.drawable.ic_play
                )
            }
        }
        println("State:--" + java.lang.String.format("State changed to:%s", event.currentState))
    }


    override fun onClose() {
        fragmentManager?.popBackStack()
    }

    override fun onSubscribeClicked() {
        val billingClient = (activity as MainActivity).getBillingClient()
        val skuDetails = (activity as MainActivity).getSkuDetails()
        if (billingClient != null && skuDetails != null) {
            val flowParams: BillingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build()
            val responseCode =
                billingClient.launchBillingFlow(activity as MainActivity, flowParams).responseCode
        }
    }

    override fun onPrivacyClicked() {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse("https://respekt.co/pages/privacy-policy-respekt-app")
        activity?.startActivity(openURL)
    }

    override fun onTermsClicked() {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse("https://respekt.co/pages/terms-of-service-respekt-app")
        activity?.startActivity(openURL)
    }
}
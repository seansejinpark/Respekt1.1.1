package com.respekt.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.BillingFlowParams
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.respekt.R
import com.respekt.activites.MainActivity
import com.respekt.mediaplayer.LocalEventFromMainActivity
import com.respekt.mediaplayer.LocalEventFromMediaPlayerHolder
import com.respekt.mediaplayer.MediaPlayerHolder
import com.respekt.models.DataItem
import com.respekt.utils.MarginItemDecorationHorizontal
import com.respekt.utils.MeData
import com.respekt.utils.ResentListAdapter
import kotlinx.android.synthetic.main.fragment_me.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MeFragment : Fragment(), (DataItem) -> Unit, PurchaseDialogFragment.OnClosePurchasedDialog {
    private var param1: String? = null
    private var param2: String? = null
    var mContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun bindList(list: MutableList<DataItem?>) {
        var adapter = ResentListAdapter(activity!!, list, this)
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        rvPlayList?.layoutManager = layoutManager
        rvPlayList?.addItemDecoration(
            MarginItemDecorationHorizontal(
                resources.getDimension(R.dimen._10sdp).toInt()
            )
        )
        rvPlayList?.itemAnimator = DefaultItemAnimator()

        rvPlayList?.adapter = adapter
        adapter.notifyDataSetChanged()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_me, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        rvPlayList.hide()
        (activity as MainActivity).shuffleVideo()
        MeData.getTotalMinutes()?.let {
            tvTotalMinutes.text = "" + (((it.toLong() / 1000) / 60))
        }
        tvTotalSession.text = MeData.getTotalSessions()
        Log.e("Size", MeData.getResentSession()?.list?.size.toString())
        MeData.getResentSession()?.let { it ->
            bindList(it.list)
        }
        playerBtnClick()

        if (MeData.getPurchasedState() == MeData.Purchased) {
            btnSubscription.visibility = View.GONE
        } else {
            btnSubscription.visibility = View.VISIBLE
        }

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
        (activity as MainActivity).getMediaPlayerHolder()?.isPlaying?.let {
            if (it) {
                layoutRunningMusic.visibility = View.VISIBLE
                val dataItem = (activity as MainActivity).getMediaPlayerHolder()?.dataItem
                Glide.with(activity!!)
                    .load(dataItem?.coverPhoto)
                    .apply(RequestOptions())
                    .fitCenter()
                    .error(R.drawable.rounded_pink_layout)
                    .placeholder(R.drawable.rounded_pink_layout)
                    .into(ivMusic)
                tvMusicName.text = dataItem?.musicTitle
                tvMusicduration.text = dataItem?.musicDuration
                return
            } else {
                layoutRunningMusic.visibility = View.GONE
            }
        }

        btnShop.setOnClickListener {
            val transaction = activity?.supportFragmentManager?.beginTransaction()
//            transaction?.hide(activity?.supportFragmentManager?.findFragmentByTag("PlayerList")!!)
            transaction?.replace(R.id.frame_container, ShopFragment.newInstance("", ""))
            transaction?.addToBackStack(null)
            transaction?.commit()
        }
        btnSubscription.setOnClickListener {
            mContext?.let {
                PurchaseDialogFragment().openDialog(it, true, this)
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

    private fun playerBtnClick() {
        btnPlayPause.setOnClickListener {
            if ((activity as MainActivity).getMediaPlayerHolder()?.isPlaying!!) {
                pause()
            } else {
                play()
            }
        }

        layoutRunningMusic.setOnClickListener {
            /*val dataItem = (activity as MainActivity).getMediaPlayerHolder()?.dataItem
            dataItem?.let {
                val transaction = activity?.supportFragmentManager?.beginTransaction()
                transaction?.hide(activity?.supportFragmentManager?.findFragmentByTag("PlayerList")!!)
                transaction?.replace(R.id.frame_container, PlayerFragment.newInstance(it, true))
                transaction?.addToBackStack(null)
                transaction?.commit()
            }*/

        }

    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MeFragment().apply {
                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()
    }

    override fun invoke(p1: DataItem) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.replace(
            R.id.frame_container,
            PlayerFragment.newInstance(p1, false),
            "PlayerFragment"
        )
        transaction?.addToBackStack(null)
        transaction?.commit()
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
//        seekbar_audio.max = event.duration
//        getDurationTimer(event.duration, tvTotalDuration)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LocalEventFromMediaPlayerHolder.PlaybackPosition) {
//        if (!isUserSeeking) {
//            seekbar_audio.progress = event.position
//            getDurationTimer(event.position, tvCurrentDuration)
//        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LocalEventFromMediaPlayerHolder.PreparedDone) {
        play()
    }

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
        }
        println("State:--" + java.lang.String.format("State changed to:%s", event.currentState))
    }

    override fun onClose() {
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


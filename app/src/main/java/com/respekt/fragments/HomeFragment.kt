package com.respekt.fragments

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.respekt.R
import com.respekt.activites.MainActivity
import com.respekt.mediaplayer.LocalEventFromMainActivity
import com.respekt.mediaplayer.LocalEventFromMediaPlayerHolder
import com.respekt.mediaplayer.MediaPlayerHolder
import kotlinx.android.synthetic.main.fragment_home.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var mPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private fun playAudio() {
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
        if (mPlayer == null)
            mPlayer = MediaPlayer.create(activity, R.raw.chakli_voice)
        mPlayer?.start()
        mPlayer?.isLooping = true
    }

    private fun btnClick() {
        btnPlayPause.setOnClickListener {
            if ((activity as MainActivity).getMediaPlayerHolder()?.isPlaying!!) {
                pause()
            } else {
                play()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        btnClick()
        button1.setOnClickListener {
            navigateToPlaylist("BEAUTY MEDITATION", "1")
        }
        button2.setOnClickListener {
            navigateToPlaylist("SHORT BREAK", "2")
        }
        button3.setOnClickListener {
            navigateToPlaylist("MUSIC", "3")
        }
        button4.setOnClickListener {
            navigateToPlaylist("SOUND", "4")
        }

        btnShop.setOnClickListener {
            val transaction = activity?.supportFragmentManager?.beginTransaction()
//            transaction?.hide(activity?.supportFragmentManager?.findFragmentByTag("PlayerList")!!)
            transaction?.replace(R.id.frame_container, ShopFragment.newInstance("", ""))
            transaction?.addToBackStack(null)
            transaction?.commit()
        }


    }

    private fun navigateToPlaylist(categoryName: String, id: String) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.replace(
            R.id.frame_container,
            PlayListFragment.newInstance(categoryName, id),
            "PlayerList"
        )
        transaction?.addToBackStack(null)
        transaction?.commit()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onResume() {
        super.onResume()
        playAudio()
    }

    override fun onPause() {
        super.onPause()
        mPlayer?.pause()
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

}
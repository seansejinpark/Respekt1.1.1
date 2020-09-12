package com.respekt.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.respekt.R
import com.respekt.activites.MainActivity
import com.respekt.api.RetrofitClient
import com.respekt.models.DataItem
import com.respekt.models.PlayListResponse
import com.respekt.utils.MarginItemDecoration
import com.respekt.utils.PlayListAdapter
import com.respekt.utils.hide
import com.respekt.utils.show
import kotlinx.android.synthetic.main.fragment_play_list.*
import kotlinx.android.synthetic.main.fragment_play_list.btnShop
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PlayListFragment : Fragment(), (DataItem) -> Unit {
    private var param1: String? = null
    private var param2: String? = null
    var isFilterOne: Boolean = false
    var isFilterTwo: Boolean = false
    var isFilterThree: Boolean = false
    var playList: MutableList<DataItem>? = null
    var filterCategoryOne: String = ""
    var filterCategoryTwo: String = ""
    var filterCategoryThree: String = ""
    var categoryName: String = ""
    var playListId: String = ""
    var adapter: PlayListAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString("CategoryName")?.let {
            categoryName = it

        }
        arguments?.getString("Id")?.let {
            playListId = it

        }
        when (categoryName) {
            "BEAUTY MEDITATION" -> {
                filterCategoryOne = "ROUTINE"
                filterCategoryTwo = "WELLNESS"
                filterCategoryThree = "MINDFULNESS"
            }
            "SHORT BREAK" -> {
                filterCategoryOne = "VALUE"
                filterCategoryTwo = "AFFIRMATION"
                filterCategoryThree = "STRETCH"
            }
            "MUSIC" -> {
                filterCategoryOne = "CALM"
                filterCategoryTwo = "HOPE"
                filterCategoryThree = "ROMANCE"
            }
            "SOUND" -> {
                filterCategoryOne = "RELAX"
                filterCategoryTwo = "ASMR"
                filterCategoryThree = "DAILY"
            }
        }
    }

    fun bindList(list: MutableList<DataItem>) {
        adapter = activity?.let { PlayListAdapter(it, list, this) }
        val layoutManager = LinearLayoutManager(activity)
        rvPlayList?.layoutManager = layoutManager
        rvPlayList?.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen._10sdp).toInt()
            )
        )
        rvPlayList?.itemAnimator = DefaultItemAnimator()

        rvPlayList?.adapter = adapter
        adapter?.notifyDataSetChanged()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_play_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvPlayList.hide()
        progressPlaylist.show()
        (activity as MainActivity).shuffleVideo()

        filterOne.text = filterCategoryOne
        filterTwo.text = filterCategoryTwo
        filterThree.text = filterCategoryThree

        filterOne.setOnClickListener {
            if (playList != null && playList?.size != 0) {
                isFilterOne = !isFilterOne
                isFilterTwo = false
                isFilterThree = false
                filter()
            }
        }
        filterTwo.setOnClickListener {
            if (playList != null && playList?.size != 0) {
                isFilterTwo = !isFilterTwo
                isFilterOne = false
                isFilterThree = false
                filter()
            }
        }
        filterThree.setOnClickListener {
            if (playList != null && playList?.size != 0) {
                isFilterThree = !isFilterThree
                isFilterTwo = false
                isFilterOne = false
                filter()
            }
        }
        fetchPlayList(playListId)
        tvListTitle.text = categoryName

        btnShop.setOnClickListener {
            val transaction = activity?.supportFragmentManager?.beginTransaction()
//            transaction?.hide(activity?.supportFragmentManager?.findFragmentByTag("PlayerList")!!)
            transaction?.replace(R.id.frame_container, ShopFragment.newInstance("", ""))
            transaction?.addToBackStack(null)
            transaction?.commit()
        }

    }

    private fun fetchPlayList(id: String) {
        RetrofitClient.instance.getPlayList(id).enqueue(object : Callback<PlayListResponse> {
            override fun onFailure(call: Call<PlayListResponse>, t: Throwable) {
                progressPlaylist?.hide()
                rvPlayList?.hide()
            }

            override fun onResponse(
                call: Call<PlayListResponse>,
                response: Response<PlayListResponse>
            ) {
                progressPlaylist?.hide()
                rvPlayList?.show()
                playList = response.body()?.data as MutableList<DataItem>?
                response.body()?.data?.let {
                    playList?.let { it1 -> bindList(it1) }
                }
            }

        })
    }

    private fun filter() {
        if (isFilterOne) {
            filterOne.setBackgroundResource(R.drawable.rounded_bar)
        } else {
            filterOne.setBackgroundColor(
                ContextCompat.getColor(
                    activity!!,
                    android.R.color.transparent
                )
            )
        }
        if (isFilterTwo) {
            filterTwo.setBackgroundResource(R.drawable.rounded_bar)
        } else {
            filterTwo.setBackgroundColor(
                ContextCompat.getColor(
                    activity!!,
                    android.R.color.transparent
                )
            )
        }
        if (isFilterThree) {
            filterThree.setBackgroundResource(R.drawable.rounded_bar)
        } else {
            filterThree.setBackgroundColor(
                ContextCompat.getColor(
                    activity!!,
                    android.R.color.transparent
                )
            )
        }
        playList?.let {
            if (it.isNotEmpty()) {
                if (isFilterOne) {
                    adapter?.filter?.filter(filterCategoryOne)
                } else if (isFilterTwo) {
                    adapter?.filter?.filter(filterCategoryTwo)
                } else if (isFilterThree) {
                    adapter?.filter?.filter(filterCategoryThree)
                } else {
                    adapter?.filter?.filter("")
                }
            }
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlayListFragment().apply {
                arguments = Bundle().apply {
                    putString("CategoryName", param1)
                    putString("Id", param2)
                }
            }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()
    }

    override fun invoke(dataItem: DataItem) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.hide(activity?.supportFragmentManager?.findFragmentByTag("PlayerList")!!)
        transaction?.add(R.id.frame_container, PlayerFragment.newInstance(dataItem, false),"PlayerFragment")
        transaction?.addToBackStack(null)
        transaction?.commit()
    }

}
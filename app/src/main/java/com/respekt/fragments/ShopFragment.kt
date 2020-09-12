package com.respekt.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.net.http.SslError
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.respekt.R
import kotlinx.android.synthetic.main.fragment_shop.*


class ShopFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWebView()
        webViewButtonClicks()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        wvShop.settings.javaScriptEnabled = true
        wvShop.settings.loadWithOverviewMode = true
        wvShop.settings.useWideViewPort = true
        wvShop.settings.domStorageEnabled = true
        wvShop.webViewClient = object : WebViewClient() {}
        wvShop.loadUrl("https://respekt.co/collections/skincare")
    }


    fun webViewButtonClicks() {
        btnBack.setOnClickListener {
            if (wvShop.canGoBack()) {
                wvShop.goBack();
            }
        }
        btnForward.setOnClickListener {
            if (wvShop.canGoForward()) {
                wvShop.goForward();
            }
        }
        btnRefresh.setOnClickListener {
            wvShop.reload()
        }
        btnCancel.setOnClickListener {
            wvShop.stopLoading()
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ShopFragment().apply {
                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
                }
            }
    }


}


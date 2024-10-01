package com.eaclub.app

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.eaclub.app.databinding.ActivityMainBinding
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.GREEN))

        initWebView()
        setupReloadButton()

        // Subscribe to Firebase Topic
        FirebaseMessaging.getInstance().subscribeToTopic("News")
            .addOnCompleteListener { task ->
                val msg = if (task.isSuccessful) {
                    "Subscription successful"
                } else {
                    "Subscription failed"
                }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let { view?.loadUrl(it) }
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.webView.visibility = View.GONE
                binding.loadingBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.webView.visibility = View.VISIBLE
                binding.loadingBar.visibility = View.GONE
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.resources?.let { resources ->
                    if (resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
                        || resources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                        request.grant(resources)
                    }
                }
            }
        }

        if (isOnline()) {
            binding.webView.loadUrl("https://eaclub.app/")
        } else {
            showNoInternetViews()
        }
    }

    private fun setupReloadButton() {
        binding.reload.setOnClickListener {
            if (isOnline()) {
                initWebView()
                binding.networkTV.visibility = View.GONE
                binding.NoInternet.visibility = View.GONE
                binding.reload.visibility = View.GONE
            } else {
                showNoInternetViews()
            }
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnected ?: false
    }

    private fun showNoInternetViews() {
        binding.networkTV.visibility = View.VISIBLE
        binding.NoInternet.visibility = View.VISIBLE
        binding.webView.visibility = View.GONE
        binding.reload.visibility = View.VISIBLE
        binding.loadingBar.visibility = View.GONE
    }

    // Override the back button functionality for WebView navigation with double press to exit
    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed() // Close the app
                return
            }

            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()

            // Reset the flag after 2 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000)
        }
    }
}

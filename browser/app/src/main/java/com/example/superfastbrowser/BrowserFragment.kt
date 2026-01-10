package com.example.superfastbrowser

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.superfastbrowser.db.BrowserDao

class BrowserFragment : Fragment() {

    private var isIncognito: Boolean = false

    companion object {
        fun newInstance(isIncognito: Boolean): BrowserFragment {
            val fragment = BrowserFragment()
            val args = Bundle()
            args.putBoolean("isIncognito", isIncognito)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var webView: WebView
    private lateinit var urlBar: EditText
    private lateinit var fullscreenContainer: FrameLayout
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            isIncognito = it.getBoolean("isIncognito")
        }

        val view = inflater.inflate(R.layout.fragment_browser, container, false)

        webView = view.findViewById(R.id.webview)
        urlBar = view.findViewById(R.id.url_bar)
        fullscreenContainer = view.findViewById(R.id.fullscreen_container)

        webView.settings.javaScriptEnabled = true
        if (isIncognito) {
            webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
            CookieManager.getInstance().removeAllCookies(null)
            webView.clearCache(true)
            webView.clearHistory()
        }

        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
            val request = DownloadManager.Request(Uri.parse(url))
            request.setMimeType(mimetype)
            val cookies = CookieManager.getInstance().getCookie(url)
            request.addRequestHeader("cookie", cookies)
            request.addRequestHeader("User-Agent", userAgent)
            request.setDescription("Downloading file...")
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype))
            val downloadManager = requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            Toast.makeText(requireContext(), "Downloading File", Toast.LENGTH_LONG).show()
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    onHideCustomView()
                    return
                }

                customView = view
                customViewCallback = callback
                fullscreenContainer.addView(customView)

                val controlsView = layoutInflater.inflate(R.layout.custom_video_controls, fullscreenContainer, false)
                fullscreenContainer.addView(controlsView)

                val playbackSpeeds = listOf(1.0f, 1.25f, 1.5f, 2.0f)
                var currentSpeedIndex = 0

                val playbackSpeedButton = controlsView.findViewById<Button>(R.id.playback_speed)
                playbackSpeedButton.setOnClickListener {
                    currentSpeedIndex = (currentSpeedIndex + 1) % playbackSpeeds.size
                    val newSpeed = playbackSpeeds[currentSpeedIndex]
                    webView.evaluateJavascript("document.getElementsByTagName('video')[0].playbackRate = $newSpeed;", null)
                    playbackSpeedButton.text = "${newSpeed}x"
                }

                fullscreenContainer.visibility = View.VISIBLE
                webView.visibility = View.GONE
            }

            override fun onHideCustomView() {
                fullscreenContainer.removeView(customView)
                fullscreenContainer.visibility = View.GONE
                webView.visibility = View.VISIBLE
                customView = null
                customViewCallback?.onCustomViewHidden()
                customViewCallback = null
            }
        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val originalUrl = request?.url?.toString()
                if (originalUrl != null) {
                    // First, sanitize the URL to remove trackers
                    val sanitizedUrl = AdBlocker.sanitizeUrl(originalUrl)
                    if (sanitizedUrl != originalUrl) {
                        // If trackers were removed, load the clean URL
                        view?.post {
                            view.loadUrl(sanitizedUrl)
                            Toast.makeText(requireContext(), "Trackers removed from URL", Toast.LENGTH_SHORT).show()
                        }
                        // Block the original request
                        return WebResourceResponse("text/plain", "UTF-8", null)
                    }

                    // If no trackers were removed, proceed with ad-blocking
                    val domain = Uri.parse(originalUrl).host
                    if (domain != null && AdBlocker.isBlocked(domain)) {
                        return WebResourceResponse("text/plain", "UTF-8", null)
                    }
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (!isIncognito) {
                    val title = view?.title
                    if (url != null && title != null) {
                        val browserDao = BrowserDao(requireContext())
                        browserDao.addHistory(title, url, System.currentTimeMillis())
                    }
                }
            }
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val searchEngine = sharedPreferences.getString("search_engine", "google")
        val searchUrl = when (searchEngine) {
            "duckduckgo" -> "https://duckduckgo.com"
            "bing" -> "https://www.bing.com"
            else -> "https://www.google.com"
        }
        webView.loadUrl(searchUrl)

        urlBar.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                val url = urlBar.text.toString()
                if (url.isNotEmpty()) {
                    if (url.contains(".") && !url.contains(" ")) {
                        webView.loadUrl(url)
                    } else {
                        val searchUrl = when (searchEngine) {
                            "duckduckgo" -> "https://duckduckgo.com/?q=$url"
                            "bing" -> "https://www.bing.com/search?q=$url"
                            else -> "https://www.google.com/search?q=$url"
                        }
                        webView.loadUrl(searchUrl)
                    }
                }
                true
            } else {
                false
            }
        }
        return view
    }

    fun onBackPressed(): Boolean {
        if (customView != null) {
            webView.webChromeClient.onHideCustomView()
            return true
        } else if (webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return false
    }

    fun getCurrentUrl(): String? {
        return webView.url
    }

    fun getCurrentTitle(): String? {
        return webView.title
    }
}

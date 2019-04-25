package com.drewel.drewel.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.drewel.drewel.R
import kotlinx.android.synthetic.main.activity_webview.*

class PaymentWebViewActivity : BaseActivity() {
    var slug: String = ""
    var msg: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        slug = intent.getStringExtra("PAGE_SLUG")
        msg = intent.getStringExtra("msg")
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(slug)
        progressBar.visibility = View.VISIBLE
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                super.shouldOverrideUrlLoading(view, request)
                if (request.url.getQueryParameter("status") != null) {
                    val intent = Intent(this@PaymentWebViewActivity, HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (request.url.getQueryParameter("status").equals("failed")) {
                        intent.putExtra("FROM", 3)
                        intent.putExtra("msg", getString(R.string.Payment_Failed_MSG))
                        startActivity(intent)
                    } else if (request.url.getQueryParameter("status").equals("success")) {
                        intent.putExtra("FROM", 4)
                        intent.putExtra("msg", msg)
                        startActivity(intent)
                    }
                }
                return false
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                super.onReceivedError(view, request, error)
                progressBar.visibility = View.GONE
                val intent = Intent(this@PaymentWebViewActivity, HomeActivity::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    intent.putExtra("msg", error.description.toString())
                } else {
                    intent.putExtra("msg", getString(R.string.Payment_Failed_MSG))
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("FROM", 3)
                startActivity(intent)
            }

            override fun onPageFinished(view: WebView, url: String) {
                progressBar.visibility = View.GONE
            }
        }
    }

    override fun onBackPressed() {
//        finish()
    }


}

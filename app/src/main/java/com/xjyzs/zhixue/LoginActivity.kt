package com.xjyzs.zhixue

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.xjyzs.zhixue.ui.theme.ZhiXueTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import kotlin.concurrent.thread

var LoginFlag=false

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZhiXueTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LoginUi("https://www.zhixue.com/wap_login.html")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginUi(url: String) {
    val context = LocalContext.current
    val pref=context.getSharedPreferences("token", Context.MODE_PRIVATE)
    var showMenu by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("登录") },
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多选项"
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("密码") },
                            onClick = {
                                showMenu = false
                                val intent=Intent(context,PwdActivity::class.java)
                                context.startActivity(intent)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("高级登录") },
                            onClick = {
                                showMenu = false
                                val intent=Intent(context,AdvancedLoginActivity::class.java)
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            )
        }) { innerPadding ->
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                    }

                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    settings.javaScriptEnabled = true
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)
                    webViewClient = object : WebViewClient() {
                        @SuppressLint("CommitPrefEdits")
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val rurl = request?.url.toString()
                            if (rurl.startsWith("https://www.zhixue.com/container")) {
                                LoginFlag=true
                            }
                            return super.shouldOverrideUrlLoading(view, request)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            CookieManager.getInstance().flush()
                            super.onPageFinished(view, url)
                            val pwd=pref.getString("pwd","")!!
                            if (pwd.isNotEmpty()) {
                                val jsCode = """document.getElementById('txtPassword').value = '${pwd}';
setTimeout(function() {
    document.getElementById('signup_button').click();
}, 300);"""
                                view!!.evaluateJavascript(jsCode, null)
                            }
                            if (LoginFlag){
                                Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show()
                                CoroutineScope(Dispatchers.Main).launch {
                                    withContext(Dispatchers.IO) {
                                        requestWithCookie(
                                            "https://www.zhixue.com/addon/error/book/index",
                                            CookieManager.getInstance()
                                                .getCookie("https://www.zhixue.com/activitystudy/web-report")
                                        ) { resp ->
                                            val rg = Regex(""""result":"(?<tk>.*?)"""")
                                            val matchResult = rg.findAll(resp)
                                            with(pref.edit()) {
                                                for (i in matchResult) {
                                                    putString("token", i.groups["tk"]?.value!!)
                                                    apply()
                                                }
                                            }
                                            val intent = Intent(context, MainActivity::class.java)
                                            context.startActivity(intent)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    loadUrl(url)
                }
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

fun requestWithCookie(targetUrl: String, cookies: String,onResponse: (String) -> Unit) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(targetUrl)
        .header("Cookie", cookies)
        .build()
    thread {
        try {
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseData = response.body?.string()
                onResponse(responseData ?: "No response")
            }
        }catch (_: Exception){

        }
    }
}
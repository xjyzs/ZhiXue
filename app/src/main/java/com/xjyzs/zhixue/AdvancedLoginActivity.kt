package com.xjyzs.zhixue

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.xjyzs.zhixue.ui.theme.ZhiXueTheme

class AdvancedLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZhiXueTheme {
                Surface {
                    AdvancedLoginUi()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedLoginUi() {
    val context = LocalContext.current
    val pref = context.getSharedPreferences("token", Context.MODE_PRIVATE)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("高级登录") },
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
                    }
                }
            )
        }) { innerPadding ->
        Column(Modifier.fillMaxSize().wrapContentSize(Alignment.Center).padding(innerPadding).padding(30.dp)) {
            var tgt by remember { mutableStateOf(pref.getString("tgt", "")) }
            var extInfo by remember { mutableStateOf(pref.getString("extInfo", "")) }
            TextField(label = { Text("tgt") }, value = tgt!!, onValueChange = { tgt = it }, modifier = Modifier.fillMaxWidth())
            TextField(label = { Text("extInfo") }, value = extInfo!!, onValueChange = { extInfo = it }, modifier = Modifier.fillMaxWidth())
            Button({
                with(pref.edit()) {
                    putString("tgt", tgt)
                    putString("extInfo",extInfo)
                    apply()
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("确认")
            }
        }
    }
}
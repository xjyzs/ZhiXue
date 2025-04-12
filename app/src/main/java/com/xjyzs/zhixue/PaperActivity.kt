package com.xjyzs.zhixue

import android. os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xjyzs.zhixue.ui.theme.ZhiXueTheme

class PaperActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val examId=intent.getStringExtra("examId")!!
        val paperId=intent.getStringExtra("paperId")!!
        val paperName=intent.getStringExtra("paperName")!!
        enableEdgeToEdge()
        setContent {
            ZhiXueTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Paper(examId,paperId,paperName)
                }
            }
        }
    }
}

val paperurl="https://ali-bg.zhixue.com/zhixuebao/report/checksheet/"
data class PaperResponse(
    val result:PaperResult
)
data class PaperResult(
    val sheetImages:String,
    val sheetDatas:String
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Paper(examId:String,paperId:String,paperName:String) {
    var paper by remember { mutableStateOf<List<String>>(emptyList()) }
    var sheetDatas by remember { mutableStateOf("") }
    val context = LocalContext.current
    get(paperurl + "?token=${token}&examId=${examId}&paperId=${paperId}") { response ->
        val result = Gson().fromJson(response, PaperResponse::class.java).result
        val sheetImages = result.sheetImages
        paper = Gson().fromJson(sheetImages, object : TypeToken<List<String>>() {}.type)
        sheetDatas = result.sheetDatas.replace("\\", "")
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(paperName) },
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(Modifier.verticalScroll(rememberScrollState()).padding(innerPadding)) {
            var c = 0
            for (i in paper) {
                if ("a9cdb4f1-c586-4786-8148-6b35d1c2f024.jpg" in i) {
                    continue
                }
                LoadImageFromUrl(i)
                Spacer(Modifier.height(50.dp))
                c += 1
            }
            if (c == 0) {
                Text("暂无数据")
            }
            val regex =
                Regex(""""dispTitle":"(?<dispTitle>.*?)".*?"isCorrect":(?<isCorrect>.*?),"score":(?<score>.*?),.*?"standardScore":(?<standardScore>.*?),""")
            val matchResult = regex.findAll(sheetDatas)
            val resultLst: MutableList<List<String>> = mutableListOf()
            var len = 0
            for (i in matchResult) {
                resultLst.add(
                    listOf(
                        i.groups["isCorrect"]?.value!!,
                        i.groups["dispTitle"]?.value!!,
                        i.groups["score"]?.value!!,
                        i.groups["standardScore"]?.value!!
                    )
                )
                len += 1
            }
            val subLst0 = if (resultLst.size >= 1) {
                resultLst.subList(0, len / 2 + 1)
            } else {
                emptyList()
            }
            val subLst1 = if (resultLst.size >= 1) {
                resultLst.subList(len / 2 + 1, len)
            } else {
                emptyList()
            }
            Row {
                Column(Modifier.weight(1f)) {
                    for (i in subLst0) {
                        Row {
                            if ((i[0])[0] == 't') {
                                Text(i[1], color = Color.Green)
                            } else {
                                Text(i[1], color = Color.Red)
                            }
                            Text("${i[2]}/${i[3]}")
                        }
                    }
                    Text("")
                }
                Column(Modifier.weight(1f)) {
                    for (i in subLst1) {
                        Row {
                            if ((i[0])[0] == 't') {
                                Text(i[1], color = Color.Green)
                            } else {
                                Text(i[1], color = Color.Red)
                            }
                            Text("${i[2]}/${i[3]}")
                        }
                    }
                    Text("")
                }
            }
        }
    }
}
@Composable
fun LoadImageFromUrl(url:String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(url).diskCachePolicy(CachePolicy.DISABLED).build(),
        contentDescription = "",
        modifier = Modifier.fillMaxWidth(),
        contentScale = ContentScale.FillWidth
    )
}
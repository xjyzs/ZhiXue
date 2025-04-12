package com.xjyzs.zhixue

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.xjyzs.zhixue.ui.theme.ZhiXueTheme

class ExamActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val examId=intent.getStringExtra("examId")!!
        val examName=intent.getStringExtra("examName")!!
        enableEdgeToEdge()
        setContent {
            ZhiXueTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ViewExam(examId,examName)
                }
            }
        }
    }
}

val examurl="https://ali-bg.zhixue.com/zhixuebao/report/exam/getReportMain?examId="
data class ExamResponse(
    val result:ExamResult
)
data class ExamResult(
    val paperList: List<PaperList>
)
data class PaperList(
    val paperId:String,
    val paperName:String,
    val standardScore:String,
    val userScore:String
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewExam(examId:String,examName:String) {
    val context = LocalContext.current
    var papers by remember { mutableStateOf<List<PaperList>>(emptyList()) }
    get(examurl + examId + "&token=${token}") { response ->
        papers = Gson().fromJson(response, ExamResponse::class.java).result.paperList
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(examName) },
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
                    }
                }
            )
        }) { innerPadding ->
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            for (i in papers) {
                Button(
                    {
                        val intent = Intent(context, PaperActivity::class.java)
                        intent.putExtra("examId", examId)
                        intent.putExtra("paperId", i.paperId)
                        intent.putExtra("paperName", i.paperName)
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(Color.Transparent, LocalContentColor.current),
                    shape = RectangleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(Modifier.weight(1f).padding(vertical = 5.dp)) {
                        Text(
                            i.paperName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            i.userScore + "/" + i.standardScore,
                            fontSize = 30.sp
                        )
                    }
                }
            }
        }
    }
}
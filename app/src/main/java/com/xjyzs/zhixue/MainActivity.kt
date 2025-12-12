package com.xjyzs.zhixue

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread
import kotlin.math.ceil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZhiXueTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ExamList()
                }
            }
        }
    }
}
var token=""
val mainUrl = "https://ali-bg.zhixue.com/zxbReport/report/getPageAllExamList?pageSize=10&pageIndex="
var isLoading=true
var at=""
var userId=""
data class ExamsResponse(
    val result: Result
)
data class Result(
    val examInfoList: List<Exam>
)
data class Exam(
    val examCreateDateTime: Long,
    val examId: String,
    val examName: String
)

@SuppressLint("CommitPrefEdits")
@Composable
fun ExamList() {
    val context = LocalContext.current
    val pref = context.getSharedPreferences("token", Context.MODE_PRIVATE)
    var trigger by remember { mutableIntStateOf(1) }
    token = pref.getString("token", "")!!
    var exams by remember { mutableStateOf<List<Exam>>(emptyList()) }
    val scrollState = rememberScrollState()
    LaunchedEffect(trigger) {
        isLoading = true
        get(mainUrl + "1" + "&token=${token}") { response ->
            exams = Gson().fromJson(response, ExamsResponse::class.java).result.examInfoList
        }
        while (isLoading) {
            delay(100)
        }
        if (exams.isEmpty()) {
            val tgt=pref.getString("tgt","")
            val extInfo=pref.getString("extInfo","")
            if (tgt!!.isNotEmpty() && extInfo!!.isNotEmpty()) {
                var client=OkHttpClient()
                var bd = "tgt=${tgt}&method=sso.extend.tgt&appId=zhixue_parent&client=android&extInfo=${extInfo}".toRequestBody()
                var request=Request.Builder().url("https://open.changyan.com/sso/v1/api").post(bd).build()
                thread {
                    var resp = client.newCall(request).execute()
                    var regex=Regex(""""at":"(?<at>.*?)","loginName.*?"userId":"(?<userId>.*?)"""")
                    var matchResult = regex.findAll(resp.body?.string().toString())
                    for(i in matchResult){
                        at=i.groups["at"]?.value!!
                        userId=i.groups["userId"]?.value!!
                    }
                    if (at.isEmpty()){
                        val intent = Intent(context, LoginActivity::class.java)
                        context.startActivity(intent)
                    }else{
                        client=OkHttpClient()
                        val mediaType = "application/x-www-form-urlencoded".toMediaType()
                        bd = "at=${at}&userId=${userId}".toRequestBody(mediaType)
                        request=Request.Builder().url("https://www.zhixue.com/container/app/login/casLogin").post(bd).build()
                        resp=client.newCall(request).execute()
                        regex=Regex(""""token":"(?<token>.*?)"""")
                        matchResult = regex.findAll(resp.body?.string().toString())
                        var tokenChanged=false
                        for(i in matchResult){
                            token=i.groups["token"]?.value!!
                            tokenChanged=true
                        }
                        if (tokenChanged) {
                            with(pref.edit()) {
                                putString("token", token)
                                apply()
                            }
                            trigger++
                        }else{
                            val intent = Intent(context, LoginActivity::class.java)
                            context.startActivity(intent)
                        }
                    }
                }
            }else {
                val intent = Intent(context, LoginActivity::class.java)
                context.startActivity(intent)
            }
        }
    }
    LaunchedEffect(scrollState.value) {
        if (exams.size < 10) {
            delay(200)
        }
        if (!isLoading && scrollState.value == scrollState.maxValue) {
            get(mainUrl + ceil((exams.size.toFloat())/10f+1).toInt().toString() + "&token=${token}&endSchoolYear=1755187199000&startSchoolYear=1723651200000") { response ->
                exams += Gson().fromJson(response, ExamsResponse::class.java).result.examInfoList
            }
        }
    }
    Column(Modifier.verticalScroll(scrollState)) {
        Spacer(Modifier.height(36.dp))
        for (exam in exams) {
            Button({
                val intent = Intent(context, ExamActivity::class.java)
                intent.putExtra("examName", exam.examName)
                intent.putExtra("examId", exam.examId)
                context.startActivity(intent)
            }, colors = ButtonDefaults.buttonColors(Color.Transparent, LocalContentColor.current), shape = RectangleShape, contentPadding = PaddingValues(0.dp)) {
                Row(Modifier.height(90.dp).weight(1f)) {
                    Column {
                        Text(exam.examName, fontSize = 24.sp, fontWeight = FontWeight.Normal, modifier = Modifier.weight(1f))
                        Text(
                            Instant.ofEpochMilli(exam.examCreateDateTime)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime().format(
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                ), color = Color.Gray, fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
    }
    Spacer(Modifier.height(50.dp))
}


fun get(url: String, onResponse: (String) -> Unit) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .build()
    thread {
        isLoading=true
        try {
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseData = response.body?.string()
                onResponse(responseData ?: "No response")
            }
        } catch (_: Exception) { }
        isLoading=false
    }
}

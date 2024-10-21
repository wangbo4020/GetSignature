package com.example.getsignature

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    data class AppSigningValue(
        val packageName: String,
        val md5: String,
        val sha1: String,
        val sha256: String,
    )

    private val mSigningValues = mutableStateListOf<AppSigningValue>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainActivityView()
        }
        onClickGetSignature("android")
//        Log.d("WXUPS", "value = " + (12345678 % 100))
//        Log.d("WXUPS", "value = " + ((12345678 / 100) % 100))
//        Log.d("WXUPS", "value = " + ((12345678 / 10000) % 100))
//        Log.d("WXUPS", "value = " + ((12345678 / 1000000) % 100))
//        Log.d("WXUPS", "value = " + (12 * 1000000 + 34 * 10000 + 56 * 100 + 78))
    }

    @Preview
    @Composable
    fun MainActivityView() {
        var pnContent by remember { mutableStateOf(TextFieldValue("")) }
        val pnFocuser = remember { FocusRequester() }
        val verSnackbar = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        Scaffold(
            snackbarHost = { SnackbarHost(verSnackbar) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "GetSignature"
                        )
                    },
                    colors = TopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        scrolledContainerColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    actions = {
                        IconButton(onClick = {
                            val info = packageManager.getPackageInfo(packageName, 0)
                            coroutineScope.launch {
                                verSnackbar.showSnackbar("version: ${info.versionName}")
                            }
                        }) {
                            Icon(Icons.Outlined.Info, contentDescription = "About")
                        }
                    }
                )
            },
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp),
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    // EditText for package name
                    OutlinedTextField(
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(pnFocuser),
                        value = pnContent,
                        onValueChange = { pnContent = it },
                        label = { Text("Package Name") },
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground,
                            disabledContainerColor = MaterialTheme.colorScheme.secondary,
                            disabledContentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
                        onClick = {
                            val text = getClipboardText()?.toString() ?: ""
                            if (text.isEmpty()) return@Button
                            pnFocuser.requestFocus()
                            pnContent =
                                pnContent.copy(text = text, selection = TextRange(text.length))
                        }
                    ) {
                        Text("Paste")
                    }
                }

                // Button to get signature
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = { onClickGetSignature(pnContent.text) },
                ) {
                    Text("Get Signature")
                }

                SelectionContainer {
                    LazyColumn(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .verticalScroll(rememberScrollState()),
                    ) {
                        items(mSigningValues.size) {
                            SigningInfoItem(mSigningValues[it])
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun SigningInfoItem(value: AppSigningValue) {
        Column(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(all = 16.dp)
        ) {
            Text(
                text = value.packageName,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "MD5:",
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
            )
            Text(
                text = value.md5,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "SHA1:",
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
            )
            Text(
                text = value.sha1,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "SHA256:",
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
            )
            Text(
                text = value.sha256,
                color = Color.Gray,
            )

        }
    }

    private fun onClickGetSignature(pn: String?) {
        if (pn == null || pn.trim().isEmpty())
            return
        mSigningValues.add(
            0,
            getAppSignature(pn) ?: return
        )
    }

    private fun getAppSignature(pn: String): AppSigningValue? {
        val md5 = AppSigning.getMD5(this, pn)
        val sha1 = AppSigning.getSHA1(this, pn)
        val sha256 = AppSigning.getSHA256(this, pn)
        if (md5 == null || sha1 == null || sha256 == null) return null
        return AppSigningValue(pn, md5, sha1, sha256)
    }

    private fun getClipboardText(): CharSequence? {
        // 获取系统服务 ClipboardManager
        val clipboardManager: ClipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // 检查剪切板是否有内容
        val clip: ClipData? = clipboardManager.primaryClip
        if (clip != null && clip.itemCount > 0) {
            // 获取第一个（也是唯一的一个）ClipData.Item对象
            val item = clip.getItemAt(0)

            // 获取文本内容
            return item.coerceToText(this)

        }
        return null
    }
}
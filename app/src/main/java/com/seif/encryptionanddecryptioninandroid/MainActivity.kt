package com.seif.encryptionanddecryptioninandroid

import android.os.Bundle
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.seif.encryptionanddecryptioninandroid.ui.theme.EncryptionAndDecryptionInAndroidTheme
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cryptoManager = CryptoManager()
        setContent {
            EncryptionAndDecryptionInAndroidTheme {
                // A surface container using the 'background' color from the theme
                var messageToEncrypt by remember {
                    mutableStateOf("")
                }
                var messageToDecrypt by remember {
                    mutableStateOf("")
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    TextField(
                        value = messageToEncrypt,
                        onValueChange = { messageToEncrypt = it },
                        Modifier.fillMaxWidth(),
                        placeholder = { Text(text = "Encrypt String", color = Color.Gray)}
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth()) {
                        Button(onClick = {
                            // we will use a file to write our encrypted string (bytes)
                            val bytes = messageToEncrypt.encodeToByteArray()
                            // filesDir (files Directory): app internal storage
                            val file = File(filesDir, "secret.txt")
                            if (!file.exists()){
                                file.createNewFile()
                            }
                            // fos (fileOpenStream)
                            val fos = FileOutputStream(file)
                            messageToDecrypt = cryptoManager.encrypt(
                                bytes,
                                fos
                            ).decodeToString()
                        }) {
                            Text(text = "Encrypt")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(onClick = {
                            // read this file
                            val file = File(filesDir,"secret.txt")
                            messageToEncrypt = cryptoManager.decrypt(
                                inputStream = FileInputStream(file)
                            ).decodeToString()
                        }) {
                            Text(text = "Decrypt")
                        }
                    }
                    Text(text = messageToDecrypt) // result of our encryption

                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    EncryptionAndDecryptionInAndroidTheme {
        Greeting("Android")
    }
}
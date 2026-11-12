package com.example.apk_levelup.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_levelup.ui.theme.LuBackground
import com.example.apk_levelup.ui.theme.LuBorder
import com.example.apk_levelup.ui.theme.LuPrimary
import com.example.apk_levelup.ui.theme.LuPrimarySoft
import com.example.apk_levelup.ui.theme.LuSurface
import com.example.apk_levelup.ui.theme.LuTextDark
import com.example.apk_levelup.ui.theme.LuTextGray
import kotlinx.coroutines.delay

// Logo sementara: kotak biru muda dengan bintang. Nanti diganti ikon asli dari Figma.
@Composable
fun LogoBox(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(LuPrimarySoft),
        contentAlignment = Alignment.Center
    ) {
        Text("✦", fontSize = (size.value * 0.45f).sp, color = LuPrimary)
    }
}

@Composable
private fun Blob(color: Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .size(280.dp)
            .background(
                Brush.radialGradient(listOf(color, Color.Transparent)),
                CircleShape
            )
    )
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500)
        onFinished()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuBackground)
    ) {
        Blob(Color(0xFFBFD7FF), Modifier.align(Alignment.TopStart).offset(x = (-80).dp, y = (-60).dp))
        Blob(Color(0xFFCFF2DB), Modifier.align(Alignment.TopEnd).offset(x = 80.dp, y = (-40).dp))
        Blob(Color(0xFFFFE9B0), Modifier.align(Alignment.CenterStart).offset(x = (-100).dp, y = 160.dp))
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoBox(104.dp)
            Spacer(Modifier.height(16.dp))
            Text("Level Up", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = LuPrimary,
    unfocusedBorderColor = LuBorder,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedLeadingIconColor = LuTextGray,
    unfocusedLeadingIconColor = LuTextGray
)

@Composable
fun LoginScreen(onLogin: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuBackground)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        LogoBox(72.dp)
        Spacer(Modifier.height(12.dp))
        Text("Level Up", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
        Spacer(Modifier.height(20.dp))

        Column(Modifier.fillMaxWidth()) {
            Text("Selamat Datang", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = LuTextDark)
            Text("Silakan masuk untuk melanjutkan", fontSize = 13.sp, color = LuTextGray)
        }
        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LuSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Email", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LuTextDark)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("nama@email.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = fieldColors()
                )
                Spacer(Modifier.height(16.dp))
                Text("Kata sandi", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LuTextDark)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Masukkan kata sandi") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        TextButton(onClick = { showPassword = !showPassword }) {
                            Text(if (showPassword) "Sembunyi" else "Lihat", fontSize = 12.sp)
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = fieldColors()
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LuPrimary)
                ) {
                    Text("Masuk", fontWeight = FontWeight.Bold)
                }
            }
        }

        Text(
            "Lupa kata sandi?",
            modifier = Modifier.align(Alignment.End).padding(top = 12.dp),
            color = LuPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f), color = LuTextDark)
            Text("atau", modifier = Modifier.padding(horizontal = 12.dp), fontSize = 12.sp, color = LuTextDark)
            HorizontalDivider(Modifier.weight(1f), color = LuTextDark)
        }
        Spacer(Modifier.height(20.dp))

        OutlinedButton(
            onClick = { },
            modifier = Modifier.width(170.dp).height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
            border = BorderStroke(1.dp, LuBorder)
        ) {
            Text("Google", fontWeight = FontWeight.Bold, color = LuTextDark)
        }

        Spacer(Modifier.height(40.dp))
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("Belum punya akun? ", fontSize = 13.sp, color = LuTextGray)
            Text("Buat Akun", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LuPrimary)
        }
        Spacer(Modifier.height(24.dp))
    }
}

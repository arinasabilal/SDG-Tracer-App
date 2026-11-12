package com.example.apk.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.remember
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
import com.example.apk.ui.theme.LuBackground
import com.example.apk.ui.theme.LuBorder
import com.example.apk.ui.theme.LuPrimary
import com.example.apk.ui.theme.LuPrimarySoft
import com.example.apk.ui.theme.LuSurface
import com.example.apk.ui.theme.LuTextDark
import com.example.apk.ui.theme.LuTextGray
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
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

// BARU: mengubah error Firebase menjadi pesan bahasa Indonesia.
// Urutan penting: WeakPassword turunan dari InvalidCredentials, jadi harus dicek lebih dulu.
private fun authErrorMessage(e: Exception?): String = when (e) {
    is FirebaseAuthWeakPasswordException -> "Kata sandi terlalu lemah (minimal 6 karakter)."
    is FirebaseAuthUserCollisionException -> "Email ini sudah terdaftar. Silakan masuk."
    is FirebaseAuthInvalidUserException,
    is FirebaseAuthInvalidCredentialsException -> "Email atau kata sandi salah."
    is FirebaseNetworkException -> "Tidak ada koneksi internet."
    else -> "Gagal: ${e?.localizedMessage ?: "terjadi kesalahan"}"
}

// Tanda tangan fungsi TIDAK berubah (onLogin: () -> Unit), jadi file lain tidak perlu diubah.
// onLogin sekarang hanya dipanggil kalau login/daftar di Firebase BERHASIL.
@Composable
fun LoginScreen(onLogin: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var isRegister by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var messageIsError by rememberSaveable { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    // BARU: proses masuk / daftar ke Firebase Authentication.
    val submit: () -> Unit = {
        val e = email.trim()
        when {
            e.isEmpty() || password.isEmpty() -> {
                message = "Email dan kata sandi wajib diisi."
                messageIsError = true
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches() -> {
                message = "Format email tidak valid."
                messageIsError = true
            }
            else -> {
                isLoading = true
                message = null
                val auth = FirebaseAuth.getInstance()
                val task = if (isRegister) {
                    auth.createUserWithEmailAndPassword(e, password)
                } else {
                    auth.signInWithEmailAndPassword(e, password)
                }
                task.addOnCompleteListener { t ->
                    isLoading = false
                    if (t.isSuccessful) {
                        onLogin()
                    } else {
                        message = authErrorMessage(t.exception)
                        messageIsError = true
                    }
                }
            }
        }
    }

    // BARU: kirim email reset kata sandi.
    val resetPassword: () -> Unit = {
        val e = email.trim()
        if (e.isEmpty()) {
            message = "Isi email dulu, lalu tekan Lupa kata sandi."
            messageIsError = true
        } else {
            FirebaseAuth.getInstance().sendPasswordResetEmail(e).addOnCompleteListener { t ->
                if (t.isSuccessful) {
                    message = "Jika email terdaftar, tautan reset kata sandi sudah dikirim. Cek juga folder spam."
                    messageIsError = false
                } else {
                    message = authErrorMessage(t.exception)
                    messageIsError = true
                }
            }
        }
    }

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
            Text(
                if (isRegister) "Buat Akun Baru" else "Selamat Datang",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = LuTextDark
            )
            Text(
                if (isRegister) "Isi email dan kata sandi untuk mendaftar" else "Silakan masuk untuk melanjutkan",
                fontSize = 13.sp,
                color = LuTextGray
            )
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

                // BARU: pesan error / sukses
                message?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        it,
                        fontSize = 13.sp,
                        color = if (messageIsError) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                    )
                }

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = submit,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LuPrimary)
                ) {
                    Text(
                        if (isLoading) "Memproses..." else if (isRegister) "Daftar" else "Masuk",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (!isRegister) {
            Text(
                "Lupa kata sandi?",
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable(onClick = resetPassword)
                    .padding(top = 12.dp),
                color = LuPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f), color = LuTextDark)
            Text("atau", modifier = Modifier.padding(horizontal = 12.dp), fontSize = 12.sp, color = LuTextDark)
            HorizontalDivider(Modifier.weight(1f), color = LuTextDark)
        }
        Spacer(Modifier.height(20.dp))

        OutlinedButton(
            onClick = {
                // Login Google belum disiapkan (butuh library tambahan dan SHA-1).
                message = "Login dengan Google belum tersedia."
                messageIsError = true
            },
            modifier = Modifier.width(170.dp).height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
            border = BorderStroke(1.dp, LuBorder)
        ) {
            Text("Google", fontWeight = FontWeight.Bold, color = LuTextDark)
        }

        Spacer(Modifier.height(40.dp))
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (isRegister) "Sudah punya akun? " else "Belum punya akun? ",
                fontSize = 13.sp,
                color = LuTextGray
            )
            Text(
                if (isRegister) "Masuk" else "Buat Akun",
                modifier = Modifier.clickable {
                    isRegister = !isRegister
                    message = null
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = LuPrimary
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

// LOKASI DI GITHUB (TIMPA file lama): app/src/main/java/com/example/apk/ui/theme/AuthScreens.kt
// Berisi: Splash, Login, Daftar, Lupa Kata Sandi. Logika Firebase ada di AuthManager.kt.
// Aturan: tampilan boleh diubah bebas, tapi nama dan parameter fungsi publik jangan diubah.
package com.example.apk.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk.auth.AuthManager
import com.example.apk.ui.theme.LuBackground
import com.example.apk.ui.theme.LuBorder
import com.example.apk.ui.theme.LuPrimary
import com.example.apk.ui.theme.LuPrimarySoft
import com.example.apk.ui.theme.LuSurface
import com.example.apk.ui.theme.LuTextDark
import com.example.apk.ui.theme.LuTextGray
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val AuthErrorColor = Color(0xFFD32F2F)
private val AuthErrorBg = Color(0xFFFDECEA)
private val AuthSuccessColor = Color(0xFF2E7D32)
private val AuthSuccessBg = Color(0xFFE8F5E9)

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

// ---------------------------------------------------------------- SPLASH

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val finished by rememberUpdatedState(onFinished)
    val scaleAnim = remember { Animatable(0.6f) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
        launch { alphaAnim.animateTo(1f, tween(700)) }
        delay(1800)
        finished()
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
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    alpha = alphaAnim.value
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoBox(104.dp)
            Spacer(Modifier.height(16.dp))
            Text("Level Up", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
            Spacer(Modifier.height(6.dp))
            Text("Satu langkah kecil, setiap hari", fontSize = 13.sp, color = LuTextGray)
        }
    }
}

// ---------------------------------------------------------------- KOMPONEN BERSAMA

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = LuPrimary,
    unfocusedBorderColor = LuBorder,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedLeadingIconColor = LuTextGray,
    unfocusedLeadingIconColor = LuTextGray
)

// Kerangka layar akun: latar, logo, judul. Isi layar diberikan lewat content.
@Composable
private fun AuthScaffold(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuBackground)
    ) {
        Blob(Color(0xFFBFD7FF), Modifier.align(Alignment.TopStart).offset(x = (-110).dp, y = (-90).dp))
        Blob(Color(0xFFCFF2DB), Modifier.align(Alignment.TopEnd).offset(x = 110.dp, y = (-70).dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (onBack != null) {
                Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = LuTextDark
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            } else {
                Spacer(Modifier.height(48.dp))
            }
            LogoBox(64.dp)
            Spacer(Modifier.height(10.dp))
            Text("Level Up", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
            Spacer(Modifier.height(24.dp))

            Column(Modifier.fillMaxWidth()) {
                Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, fontSize = 14.sp, color = LuTextGray)
            }
            Spacer(Modifier.height(20.dp))

            content()

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AuthCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LuSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(20.dp), content = content)
    }
}

// Satu kolom isian: label di atas, kolom teks di bawah. Kalau isPassword, ada ikon mata.
@Composable
private fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leading: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isPassword: Boolean = false,
    onDone: () -> Unit = {}
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LuTextDark)
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(leading, contentDescription = null) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (visible) "Sembunyikan kata sandi" else "Tampilkan kata sandi"
                    )
                }
            }
        } else null,
        singleLine = true,
        visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onDone = {
                focusManager.clearFocus()
                onDone()
            }
        ),
        colors = fieldColors()
    )
}

@Composable
private fun PrimaryAuthButton(text: String, loading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !loading,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = LuPrimary,
            disabledContainerColor = LuPrimary
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            Text(text, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MessageBanner(text: String, isError: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isError) AuthErrorBg else AuthSuccessBg)
            .padding(12.dp)
    ) {
        Text(
            text,
            fontSize = 13.sp,
            color = if (isError) AuthErrorColor else AuthSuccessColor
        )
    }
}

// ---------------------------------------------------------------- LOGIN

// onGoRegister dan onGoForgot diberi nilai awal supaya file lama yang hanya memberi onLogin tetap bisa dikompilasi.
@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    onGoRegister: () -> Unit = {},
    onGoForgot: () -> Unit = {}
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var messageIsError by rememberSaveable { mutableStateOf(true) }
    var loading by remember { mutableStateOf(false) }

    val submit: () -> Unit = {
        if (!loading) {
            when {
                email.isBlank() || password.isEmpty() -> {
                    message = "Email dan kata sandi wajib diisi."
                    messageIsError = true
                }
                !AuthManager.isValidEmail(email) -> {
                    message = "Format email tidak valid."
                    messageIsError = true
                }
                else -> {
                    loading = true
                    message = null
                    AuthManager.signIn(email, password) { error ->
                        loading = false
                        if (error == null) {
                            onLogin()
                        } else {
                            message = error
                            messageIsError = true
                        }
                    }
                }
            }
        }
    }

    AuthScaffold(
        title = "Selamat Datang",
        subtitle = "Silakan masuk untuk melanjutkan"
    ) {
        AuthCard {
            AuthField(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                placeholder = "nama@email.com",
                leading = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
            Spacer(Modifier.height(16.dp))
            AuthField(
                label = "Kata sandi",
                value = password,
                onValueChange = { password = it },
                placeholder = "Masukkan kata sandi",
                leading = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                isPassword = true,
                onDone = submit
            )
            message?.let {
                Spacer(Modifier.height(12.dp))
                MessageBanner(it, messageIsError)
            }
            Spacer(Modifier.height(20.dp))
            PrimaryAuthButton("Masuk", loading, submit)
        }

        Text(
            "Lupa kata sandi?",
            modifier = Modifier
                .align(Alignment.End)
                .clickable(onClick = onGoForgot)
                .padding(vertical = 12.dp),
            color = LuPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f), color = LuBorder)
            Text("atau", modifier = Modifier.padding(horizontal = 12.dp), fontSize = 12.sp, color = LuTextGray)
            HorizontalDivider(Modifier.weight(1f), color = LuBorder)
        }
        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                // Login Google belum disiapkan (butuh library tambahan dan SHA-1).
                message = "Login dengan Google segera hadir."
                messageIsError = false
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
            border = BorderStroke(1.dp, LuBorder)
        ) {
            Text("G", fontWeight = FontWeight.Bold, color = LuPrimary)
            Spacer(Modifier.width(10.dp))
            Text("Lanjutkan dengan Google", fontWeight = FontWeight.SemiBold, color = LuTextDark)
        }

        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("Belum punya akun? ", fontSize = 13.sp, color = LuTextGray)
            Text(
                "Buat Akun",
                modifier = Modifier.clickable(onClick = onGoRegister),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = LuPrimary
            )
        }
    }
}

// ---------------------------------------------------------------- DAFTAR

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val submit: () -> Unit = {
        if (!loading) {
            when {
                name.isBlank() -> {
                    message = "Nama wajib diisi."
                }
                !AuthManager.isValidEmail(email) -> {
                    message = "Format email tidak valid."
                }
                password.length < 6 -> {
                    message = "Kata sandi minimal 6 karakter."
                }
                password != confirm -> {
                    message = "Konfirmasi kata sandi tidak sama."
                }
                else -> {
                    loading = true
                    message = null
                    AuthManager.register(name, email, password) { error ->
                        loading = false
                        if (error == null) {
                            onRegistered()
                        } else {
                            message = error
                        }
                    }
                }
            }
        }
    }

    AuthScaffold(
        title = "Buat Akun Baru",
        subtitle = "Isi data di bawah untuk mulai memakai Level Up",
        onBack = onBackToLogin
    ) {
        AuthCard {
            AuthField(
                label = "Nama",
                value = name,
                onValueChange = { name = it },
                placeholder = "Nama panggilanmu",
                leading = Icons.Default.Person,
                imeAction = ImeAction.Next
            )
            Spacer(Modifier.height(16.dp))
            AuthField(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                placeholder = "nama@email.com",
                leading = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
            Spacer(Modifier.height(16.dp))
            AuthField(
                label = "Kata sandi",
                value = password,
                onValueChange = { password = it },
                placeholder = "Minimal 6 karakter",
                leading = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
                isPassword = true
            )
            Spacer(Modifier.height(16.dp))
            AuthField(
                label = "Ulangi kata sandi",
                value = confirm,
                onValueChange = { confirm = it },
                placeholder = "Ketik ulang kata sandi",
                leading = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                isPassword = true,
                onDone = submit
            )
            message?.let {
                Spacer(Modifier.height(12.dp))
                MessageBanner(it, isError = true)
            }
            Spacer(Modifier.height(20.dp))
            PrimaryAuthButton("Daftar", loading, submit)
        }

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sudah punya akun? ", fontSize = 13.sp, color = LuTextGray)
            Text(
                "Masuk",
                modifier = Modifier.clickable(onClick = onBackToLogin),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = LuPrimary
            )
        }
    }
}

// ---------------------------------------------------------------- LUPA KATA SANDI

@Composable
fun ForgotPasswordScreen(onBack: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var messageIsError by rememberSaveable { mutableStateOf(true) }
    var loading by remember { mutableStateOf(false) }

    val submit: () -> Unit = {
        if (!loading) {
            when {
                email.isBlank() -> {
                    message = "Email wajib diisi."
                    messageIsError = true
                }
                !AuthManager.isValidEmail(email) -> {
                    message = "Format email tidak valid."
                    messageIsError = true
                }
                else -> {
                    loading = true
                    message = null
                    AuthManager.sendPasswordReset(email) { error ->
                        loading = false
                        if (error == null) {
                            message = "Jika email terdaftar, tautan untuk mengatur ulang kata sandi sudah dikirim. Cek juga folder spam."
                            messageIsError = false
                        } else {
                            message = error
                            messageIsError = true
                        }
                    }
                }
            }
        }
    }

    AuthScaffold(
        title = "Lupa Kata Sandi",
        subtitle = "Masukkan email akunmu. Kami kirim tautan untuk mengatur ulang kata sandi.",
        onBack = onBack
    ) {
        AuthCard {
            AuthField(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                placeholder = "nama@email.com",
                leading = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
                onDone = submit
            )
            message?.let {
                Spacer(Modifier.height(12.dp))
                MessageBanner(it, messageIsError)
            }
            Spacer(Modifier.height(20.dp))
            PrimaryAuthButton("Kirim tautan reset", loading, submit)
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Kembali ke Masuk",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable(onClick = onBack)
                .padding(8.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = LuPrimary
        )
    }
}

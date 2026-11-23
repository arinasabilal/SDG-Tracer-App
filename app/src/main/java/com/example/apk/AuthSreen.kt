package com.example.apk

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val BrandAccent = Color(0xFFF3CC4F)
val BrandAccentDark = Color(0xFFE5BD3E)
val DarkSurface = Color(0xFF16130E)
val InputBg = Color(0xFF231F18)
val InputBorder = Color(0xFF3A342B)
val TextPrimaryDark = Color(0xFF1A1712)
val TextMuted = Color(0xFF9E978B)
val ErrorCoral = Color(0xFFE57373)
val ValidGreen = Color(0xFF81C784)

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit = {}
) {
    var isLoginTab by remember { mutableStateOf(true) }

    // State Form Masuk (Login)
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPassVisible by remember { mutableStateOf(false) }

    // State Form Daftar (Sign Up)
    var fullName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regPassVisible by remember { mutableStateOf(false) }
    var regConfirmPassVisible by remember { mutableStateOf(false) }
    var isTermsAccepted by remember { mutableStateOf(false) }

    // Validasi Password Real-time
    val hasMinLength = regPassword.length >= 8
    val hasUppercase = regPassword.any { it.isUpperCase() }
    val hasLowercase = regPassword.any { it.isLowerCase() }
    val hasDigit = regPassword.any { it.isDigit() }
    val isPasswordMatch = regPassword.isNotEmpty() && regPassword == regConfirmPassword

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandAccent)
    ) {
        // ================= HEADER LEVELUPP =================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFF8E1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🚀", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "LevelUpp",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = BrandAccentDark.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, TextPrimaryDark.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌐", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ID",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimaryDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isLoginTab) "Selamat datang kembali!" else "Mulai perjalanan LevelUpp",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isLoginTab)
                    "Masuk untuk melanjutkan progres dan target harianmu."
                else
                    "Buat akun gratis. Data tersinkron aman di semua perangkatmu.",
                fontSize = 14.sp,
                color = TextPrimaryDark.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }

        // ================= KARTU FORM GELAP =================
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                // Tab Switcher (Masuk / Daftar)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(InputBg)
                        .padding(4.dp)
                ) {
                    TabButton(
                        text = "Masuk",
                        isSelected = isLoginTab,
                        onClick = { isLoginTab = true },
                        modifier = Modifier.weight(1f)
                    )
                    TabButton(
                        text = "Daftar",
                        isSelected = !isLoginTab,
                        onClick = { isLoginTab = false },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoginTab) {
                    // ================= KONTEN TAB MASUK =================
                    InputLabel("Email")
                    CustomTextField(
                        value = loginEmail,
                        onValueChange = { loginEmail = it },
                        placeholder = "nama@email.com",
                        leadingEmoji = "✉️",
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    InputLabel("Password")
                    CustomTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        placeholder = "Masukkan password",
                        leadingEmoji = "🔒",
                        isPassword = true,
                        passwordVisible = loginPassVisible,
                        onTogglePassword = { loginPassVisible = !loginPassVisible }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Button(
                            onClick = { onAuthSuccess() },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandAccent,
                                contentColor = TextPrimaryDark
                            )
                        ) {
                            Text(
                                text = "Masuk  →",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Segera",
                                fontSize = 11.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(InputBg)
                                    .border(1.dp, InputBorder, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👤", fontSize = 20.sp)
                            }
                        }
                    }

                } else {
                    // ================= KONTEN TAB DAFTAR =================
                    InputLabel("Nama lengkap")
                    CustomTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = "Nama kamu",
                        leadingEmoji = "👤"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    InputLabel("Email")
                    CustomTextField(
                        value = regEmail,
                        onValueChange = { regEmail = it },
                        placeholder = "nama@email.com",
                        leadingEmoji = "✉️",
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    InputLabel("Password")
                    CustomTextField(
                        value = regPassword,
                        onValueChange = { regPassword = it },
                        placeholder = "Minimal 8 karakter",
                        leadingEmoji = "🔒",
                        isPassword = true,
                        passwordVisible = regPassVisible,
                        onTogglePassword = { regPassVisible = !regPassVisible }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    InputLabel("Konfirmasi Password")
                    CustomTextField(
                        value = regConfirmPassword,
                        onValueChange = { regConfirmPassword = it },
                        placeholder = "Ulangi password",
                        leadingEmoji = "🔒",
                        isPassword = true,
                        passwordVisible = regConfirmPassVisible,
                        onTogglePassword = { regConfirmPassVisible = !regConfirmPassVisible },
                        isError = regConfirmPassword.isNotEmpty() && !isPasswordMatch
                    )

                    if (regConfirmPassword.isNotEmpty() && !isPasswordMatch) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⚠️ Password tidak sama",
                            color = ErrorCoral,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RequirementBadge("8+ karakter", hasMinLength)
                        RequirementBadge("Huruf besar", hasUppercase)
                        RequirementBadge("Huruf kecil", hasLowercase)
                        RequirementBadge("Angka", hasDigit)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = isTermsAccepted,
                            onCheckedChange = { isTermsAccepted = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = BrandAccent,
                                uncheckedColor = TextMuted,
                                checkmarkColor = TextPrimaryDark
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(color = Color.White)) {
                                        append("Saya menyetujui ")
                                    }
                                    withStyle(
                                        SpanStyle(
                                            color = Color.White,
                                            textDecoration = TextDecoration.Underline,
                                            fontWeight = FontWeight.Medium
                                        )
                                    ) {
                                        append("Syarat & Ketentuan")
                                    }
                                    withStyle(SpanStyle(color = Color.White)) {
                                        append(" dan ")
                                    }
                                    withStyle(
                                        SpanStyle(
                                            color = Color.White,
                                            textDecoration = TextDecoration.Underline,
                                            fontWeight = FontWeight.Medium
                                        )
                                    ) {
                                        append("Kebijakan Privasi.")
                                    }
                                },
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            if (!isTermsAccepted) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Setujui syarat & ketentuan untuk melanjutkan",
                                    color = ErrorCoral,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { onAuthSuccess() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandAccent,
                            contentColor = TextPrimaryDark
                        )
                    ) {
                        Text(
                            text = "Buat Akun  →",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // ================= PEMISAH & LOGIN SOSIAL =================
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(InputBorder)
                    )
                    Text(
                        text = "atau",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(InputBorder)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = { onAuthSuccess() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, InputBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = InputBg,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "G",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4285F4)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Lanjutkan dengan Google",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isLoginTab) "Belum punya akun? " else "Sudah punya akun? ",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (isLoginTab) "Daftar" else "Masuk",
                        color = BrandAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { isLoginTab = !isLoginTab }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🛡️", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Enkripsi 256-bit · Keamanan data akun LevelUpp",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// ================= KOMPONEN PEMBANTU =================

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF332E25) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else TextMuted,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun InputLabel(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingEmoji: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(text = placeholder, color = TextMuted, fontSize = 14.sp)
        },
        leadingIcon = {
            Text(text = leadingEmoji, fontSize = 16.sp)
        },
        trailingIcon = if (isPassword && onTogglePassword != null) {
            {
                TextButton(onClick = onTogglePassword) {
                    Text(
                        text = if (passwordVisible) "Sembunyi" else "Lihat",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = InputBg,
            unfocusedContainerColor = InputBg,
            focusedBorderColor = if (isError) ErrorCoral else BrandAccent,
            unfocusedBorderColor = if (isError) ErrorCoral else InputBorder,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = BrandAccent
        )
    )
}

@Composable
private fun RequirementBadge(text: String, isMet: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (isMet) ValidGreen else TextMuted)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = if (isMet) ValidGreen else TextMuted,
            fontSize = 11.sp
        )
    }
}
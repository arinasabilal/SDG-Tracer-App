// LOKASI DI GITHUB (file BARU): app/src/main/java/com/example/apk/auth/AuthManager.kt
// Semua logika Firebase Authentication ada di sini. Layar hanya memanggil fungsi di bawah ini.
// Aturan: jangan ubah nama fungsi dan parameternya kalau mengubah tampilan.
package com.example.apk.auth

import android.util.Patterns
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest

object AuthManager {

    private fun auth(): FirebaseAuth = FirebaseAuth.getInstance()

    // True kalau pengguna masih login (Firebase mengingat sesi walau app ditutup).
    val isLoggedIn: Boolean
        get() = auth().currentUser != null

    val email: String
        get() = auth().currentUser?.email.orEmpty()

    // Nama untuk sapaan: nama yang diisi saat daftar, kalau kosong pakai bagian depan email.
    val displayName: String
        get() {
            val user = auth().currentUser
            val name = user?.displayName
            if (!name.isNullOrBlank()) return name
            val mail = user?.email
            if (!mail.isNullOrBlank()) return mail.substringBefore("@")
            return "Pengguna"
        }

    fun isValidEmail(value: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches()

    // onDone(null) = berhasil, onDone("pesan") = gagal dengan pesan untuk ditampilkan.
    fun signIn(email: String, password: String, onDone: (String?) -> Unit) {
        auth().signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                onDone(if (task.isSuccessful) null else friendlyMessage(task.exception))
            }
    }

    fun register(name: String, email: String, password: String, onDone: (String?) -> Unit) {
        auth().createUserWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    onDone(friendlyMessage(task.exception))
                } else {
                    val user = task.result?.user
                    val cleanName = name.trim()
                    if (user != null && cleanName.isNotEmpty()) {
                        val request = UserProfileChangeRequest.Builder()
                            .setDisplayName(cleanName)
                            .build()
                        // Akun sudah jadi. Gagal menyimpan nama tidak membatalkan pendaftaran.
                        user.updateProfile(request).addOnCompleteListener { onDone(null) }
                    } else {
                        onDone(null)
                    }
                }
            }
    }

    fun sendPasswordReset(email: String, onDone: (String?) -> Unit) {
        auth().sendPasswordResetEmail(email.trim())
            .addOnCompleteListener { task ->
                val e = task.exception
                // Email yang tidak terdaftar sengaja dianggap berhasil, supaya orang lain
                // tidak bisa mengecek email siapa yang punya akun.
                onDone(
                    if (task.isSuccessful || e is FirebaseAuthInvalidUserException) null
                    else friendlyMessage(e)
                )
            }
    }

    fun signOut() {
        auth().signOut()
    }

    // Urutan penting: WeakPassword adalah turunan InvalidCredentials, jadi dicek lebih dulu.
    private fun friendlyMessage(e: Exception?): String = when (e) {
        is FirebaseAuthWeakPasswordException -> "Kata sandi terlalu lemah (minimal 6 karakter)."
        is FirebaseAuthUserCollisionException -> "Email ini sudah terdaftar. Silakan masuk."
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> "Email atau kata sandi salah."
        is FirebaseTooManyRequestsException -> "Terlalu banyak percobaan. Coba lagi beberapa menit lagi."
        is FirebaseNetworkException -> "Tidak ada koneksi internet. Coba lagi."
        else -> "Terjadi kesalahan: ${e?.localizedMessage ?: "tidak diketahui"}"
    }
}

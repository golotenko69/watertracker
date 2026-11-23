package com.watertracker.app.auth

import android.content.Context
import android.os.Build
import android.util.Base64
import android.util.Patterns
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import org.json.JSONObject

data class Account(val id: String, val email: String)

class AccountStore(context: Context) {
    private val prefs = context.getSharedPreferences("local_accounts", Context.MODE_PRIVATE)

    fun current(): Account? {
        val email = prefs.getString("active", null) ?: return null
        val record = prefs.getString("account:$email", null) ?: return null
        return Account(JSONObject(record).getString("id"), email)
    }

    @Synchronized
    fun authenticate(input: String, password: String, register: Boolean): Account {
        val email = input.trim().lowercase(Locale.ROOT)
        require(Patterns.EMAIL_ADDRESS.matcher(email).matches()) { "Введите адрес электронной почты" }
        val key = "account:$email"
        val saved = prefs.getString(key, null)
        val record = if (register) {
            require(saved == null) { "Аккаунт с этой почтой уже есть на устройстве" }
            require(password.length >= 8) { "Используйте не менее 8 символов в пароле" }
            val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
            val algorithm = if (Build.VERSION.SDK_INT >= 26) "PBKDF2WithHmacSHA256" else "PBKDF2WithHmacSHA1"
            JSONObject().apply {
                put("id", if (prefs.contains("registered")) UUID.randomUUID().toString() else "original")
                put("algorithm", algorithm)
                put("rounds", 600000)
                put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
                put("hash", Base64.encodeToString(derive(password, salt, algorithm, getInt("rounds")), Base64.NO_WRAP))
            }
        } else {
            requireNotNull(saved) { "Проверьте почту и пароль" }
            JSONObject(saved).also {
                val actual = derive(password, Base64.decode(it.getString("salt"), Base64.DEFAULT),
                    it.getString("algorithm"), it.getInt("rounds"))
                require(MessageDigest.isEqual(actual, Base64.decode(it.getString("hash"), Base64.DEFAULT))) {
                    "Проверьте почту и пароль"
                }
            }
        }
        if (register) {
            check(prefs.edit().putString(key, record.toString()).putBoolean("registered", true).commit()) {
                "Не удалось сохранить аккаунт"
            }
        }
        return Account(record.getString("id"), email)
    }

    val hasAccounts get() = prefs.getBoolean("registered", false)

    fun remember(account: Account) { prefs.edit().putString("active", account.email).apply() }

    fun logout() { prefs.edit().remove("active").apply() }

    private fun derive(password: String, salt: ByteArray, algorithm: String, rounds: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, rounds, 256)
        return try { SecretKeyFactory.getInstance(algorithm).generateSecret(spec).encoded }
        finally { spec.clearPassword() }
    }
}

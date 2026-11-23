package com.watertracker.app.auth

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.watertracker.app.WaterTrackerApp
import com.watertracker.app.databinding.ActivityAuthBinding
import com.watertracker.app.ui.main.MainActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as WaterTrackerApp
    data class State(val busy: Boolean = false, val error: String? = null, val done: Boolean = false)
    private val mutableState = MutableLiveData(State())
    val state: LiveData<State> = mutableState

    fun submit(email: String, password: String, registering: Boolean) {
        if (mutableState.value?.busy == true) return
        mutableState.value = State(busy = true)
        viewModelScope.launch {
            try {
                val account = withContext(Dispatchers.IO) { app.accounts.authenticate(email, password, registering) }
                app.openSession(account)
                mutableState.value = State(done = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                mutableState.value = State(error = e.message)
            } catch (e: Exception) {
                mutableState.value = State(error = "Не удалось открыть аккаунт. Попробуйте ещё раз.")
            }
        }
    }

    fun clearError() { mutableState.value = State() }
}

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private val model: AuthViewModel by viewModels()
    private var registering = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if ((application as WaterTrackerApp).session != null) {
            openDiary()
            return
        }
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registering = savedInstanceState?.getBoolean("registering") ?: false
        renderMode()
        binding.switchMode.setOnClickListener {
            registering = !registering
            binding.password.text?.clear()
            binding.confirm.text?.clear()
            model.clearError()
            renderMode()
        }
        binding.submit.setOnClickListener { submit() }
        for (field in listOf(binding.password, binding.confirm)) {
            field.setOnEditorActionListener { _, action, _ ->
                if (action == EditorInfo.IME_ACTION_DONE) { submit(); true } else false
            }
        }
        model.state.observe(this) { state ->
            binding.progress.visibility = if (state.busy) View.VISIBLE else View.GONE
            for (view in listOf(binding.submit, binding.switchMode, binding.email, binding.password, binding.confirm)) {
                view.isEnabled = !state.busy
            }
            showError(state.error)
            if (state.done) openDiary()
        }
    }

    private fun renderMode() {
        binding.title.text = if (registering) "Создать аккаунт" else "Вход в дневник"
        binding.submit.text = if (registering) "Зарегистрироваться" else "Войти"
        binding.switchMode.text = if (registering) "Уже есть аккаунт? Войти" else "Создать аккаунт"
        val app = application as WaterTrackerApp
        binding.localHint.text = if (registering && !app.accounts.hasAccounts &&
            getDatabasePath("water_tracker.db").exists()) {
            "Ваши прежние записи на этом устройстве перейдут в первый аккаунт"
        } else {
            "Аккаунт и записи хранятся на этом устройстве"
        }
        binding.confirmLayout.visibility = if (registering) View.VISIBLE else View.GONE
        binding.password.imeOptions = if (registering) EditorInfo.IME_ACTION_NEXT else EditorInfo.IME_ACTION_DONE
        binding.passwordLayout.helperText = if (registering) "Не менее 8 символов" else null
    }

    private fun submit() {
        val password = binding.password.text.toString()
        if (registering && password != binding.confirm.text.toString()) {
            showError("Пароли должны совпадать")
            return
        }
        model.submit(binding.email.text.toString(), password, registering)
    }

    private fun showError(message: String?) {
        binding.error.text = message
        binding.error.visibility = if (message == null) View.GONE else View.VISIBLE
    }

    private fun openDiary() {
        startActivity(Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("registering", registering)
    }
}

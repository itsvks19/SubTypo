package com.teixeira0x.subtypo

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import com.blankj.utilcode.util.ThrowableUtils
import com.teixeira0x.subtypo.core.preferences.PreferencesManager
import com.teixeira0x.subtypo.ui.activity.crash.CrashActivity
import dagger.hilt.android.HiltAndroidApp
import kotlin.system.exitProcess

@HiltAndroidApp
class App : Application() {

  companion object {
    const val APP_REPO_URL = "https://github.com/teixeira0x/SubTypo"
    const val APP_REPO_OPEN_ISSUE = "$APP_REPO_URL/issues/new"
  }

  private var uncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

  override fun onCreate() {
    uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler(this::uncaughtException)
    PreferencesManager.init(this)
    super.onCreate()
    updateUIMode()
  }

  private fun updateUIMode() {
    AppCompatDelegate.setDefaultNightMode(PreferencesManager.appearanceUIMode)
  }

  private fun uncaughtException(thread: Thread, throwable: Throwable) {
    // Start the crash activity
    startActivity(
      Intent(this, CrashActivity::class.java).apply {
        putExtra(
          CrashActivity.KEY_EXTRA_CRASH_ERROR,
          ThrowableUtils.getFullStackTrace(throwable),
        )
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    )

    uncaughtExceptionHandler?.uncaughtException(thread, throwable)
    exitProcess(1)
  }
}

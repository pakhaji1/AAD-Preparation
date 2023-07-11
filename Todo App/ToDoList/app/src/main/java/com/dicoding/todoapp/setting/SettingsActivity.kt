package com.dicoding.todoapp.setting

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.work.Data
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.dicoding.todoapp.R
import com.dicoding.todoapp.notification.NotificationWorker
import com.dicoding.todoapp.utils.NOTIFICATION_CHANNEL_ID
import java.util.concurrent.TimeUnit

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val prefNotification =
                findPreference<SwitchPreference>(getString(R.string.pref_key_notify))
            prefNotification?.setOnPreferenceChangeListener { preference, newValue ->
                val channelName = getString(R.string.notify_channel_name)
                //TODO 13 : Schedule and cancel daily reminder using WorkManager with data channelName

                val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT)
                    notificationManager.createNotificationChannel(channel)
                }

                val workManager: WorkManager = WorkManager.getInstance(requireContext())
                lateinit var periodicWorkRequest: PeriodicWorkRequest
                if (preference.key == getString(R.string.pref_key_notify)) {
                    if (newValue == true) {
                        val data = Data.Builder()
                            .putString(channelName, NOTIFICATION_CHANNEL_ID)
                            .build()

                        periodicWorkRequest = PeriodicWorkRequest.Builder(
                            NotificationWorker::class.java,
                            1, TimeUnit.DAYS
                        ).setInputData(data).build()

                        workManager.enqueue(periodicWorkRequest)
                        workManager.getWorkInfoByIdLiveData(periodicWorkRequest.id)
                            .observe(this) { workInfo ->
                                val status = workInfo.state.name
                                Log.d("SettingsFragment", "Get WorkManager Status : $status")
                                if (workInfo.state == WorkInfo.State.ENQUEUED) {
                                    Log.d("SettingsFragment", "Enqueued")
                                }
                            }
                    } else {
                        try {
                            workManager.getWorkInfoByIdLiveData(periodicWorkRequest.id)
                                .observe(this) { workInfo ->
                                    val status = workInfo.state.name
                                    Log.d(
                                        "SettingsFragment",
                                        "Get WorkManager Status : $status"
                                    )
                                    if (workInfo.state == WorkInfo.State.ENQUEUED) {
                                        try {
                                            workManager.cancelWorkById(periodicWorkRequest.id)
                                        } catch (e: Exception) {
                                            Log.e(
                                                "SettingsFragment",
                                                "Cancel Periodic Failed : ${e.message}"
                                            )
                                        }
                                        Toast.makeText(
                                            requireContext(),
                                            "Task reminder cancelled",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } catch (e: Exception) {
                            Log.e("SettingsFragment", "Cancelled Failed : ${e.message}")
                        }
                    }
                }
                true
            }

        }

        private fun updateTheme(mode: Int): Boolean {
            AppCompatDelegate.setDefaultNightMode(mode)
            requireActivity().recreate()
            return true
        }
    }
}
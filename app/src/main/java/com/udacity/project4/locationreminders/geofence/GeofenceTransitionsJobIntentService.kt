package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment.Companion.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.errorMessage
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

/**
 * A service that handles geofence transitions. This class extends JobIntentService and implements
 * CoroutineScope for background task handling using coroutines.
 */
class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()

    // Define the coroutine context, using IO dispatcher and coroutine job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573
        private const val TAG = "GeofenceIntentService"

        /**
         * Enqueue the work to be processed by the service.
         *
         * @param context The application context.
         * @param intent The intent containing the work to be done.
         */
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    /**
     * Handle the work given in the intent.
     *
     * @param intent The intent containing the geofencing event data.
     */
    override fun onHandleWork(intent: Intent) {
        // Check if the action is a geofence event
        if (intent.action == ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            // Check if the event has an error and log it if it does
            if (geofencingEvent != null && geofencingEvent.hasError()) {
                val errorMessage = errorMessage(this, geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)
                return
            }

            // Handle geofence transition event
            if (geofencingEvent != null && geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                geofencingEvent.triggeringGeofences?.let { sendNotification(it) }
                Log.d(TAG, "Geofences found")
            }
        }
    }


    /**
     * Send notifications for the geofence events.
     *
     * @param triggeringGeofences The list of geofences that triggered the event.
     */
    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        // Iterate through the list of triggering geofences
        for (triggeringGeofence in triggeringGeofences) {
            val requestId = triggeringGeofence.requestId

            // Get the local repository instance
            val remindersLocalRepository: ReminderDataSource by inject()

            // Launch a coroutine to interact with the repository
            CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                // Retrieve the reminder with the given request ID
                val result = remindersLocalRepository.getReminder(requestId)

                // If the reminder retrieval is successful, send a notification
                if (result is Result.Success<ReminderDTO>) {
                    val reminderDTO = result.data
                    sendNotification(
                        this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude,
                            reminderDTO.id
                        )
                    )
                }
            }
        }
    }
}

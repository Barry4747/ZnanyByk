package com.example.myapplication.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.data.model.trainings.Appointment
import com.example.myapplication.data.model.trainings.WeeklySchedule
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()

    private val _weeklySchedule = MutableLiveData<WeeklySchedule>()
    val weeklySchedule: LiveData<WeeklySchedule> = _weeklySchedule

    private val _appointments = MutableLiveData<List<Appointment>>()
    val appointments: LiveData<List<Appointment>> = _appointments

    fun getWeeklySchedule(trainerId: String) {
        db.collection("trainerSchedules")
            .document(trainerId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val schedule = document.toObject(WeeklySchedule::class.java)
                    _weeklySchedule.value = schedule ?: WeeklySchedule()
                } else {
                    _weeklySchedule.value = WeeklySchedule(
                        monday = listOf(),
                        tuesday = listOf(),
                        wednesday = listOf(),
                        thursday = listOf(),
                        friday = listOf(),
                        saturday = listOf(),
                        sunday = listOf()
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.e("ScheduleRepository", "Błąd pobierania terminarza", e)
                _weeklySchedule.value = WeeklySchedule()
            }
    }

    fun setWeeklySchedule(trainerId: String?, schedule: WeeklySchedule) {
        if (trainerId == null) {
            Log.e("ScheduleRepository", "trainerId jest null")
            return
        }

        db.collection("trainerSchedules")
            .document(trainerId)
            .set(schedule)
            .addOnSuccessListener {
                Log.d("ScheduleRepository", "Terminarz zapisany/aktualizowany")
                _weeklySchedule.value = schedule
            }
            .addOnFailureListener { e ->
                Log.e("ScheduleRepository", "Błąd zapisu terminarza", e)
            }
    }

    fun getAppointments(trainerId: String) {
        db.collection("appointments")
            .whereEqualTo("trainerId", trainerId)
            .get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(Appointment::class.java)
                _appointments.value = list
            }
            .addOnFailureListener { e ->
                Log.e("ScheduleRepository", "Błąd pobierania umówionych treningów", e)
                _appointments.value = emptyList()
            }
    }

    fun addAppointment(appointment: Appointment, onSuccess: (() -> Unit)? = null) {
        db.collection("appointments")
            .add(appointment)
            .addOnSuccessListener {
                Log.d("ScheduleRepository", "Dodano nowy trening")
                appointment.trainerId?.let { trainerId ->
                    getAppointments(trainerId)
                }
                onSuccess?.invoke()
            }
            .addOnFailureListener { e ->
                Log.e("ScheduleRepository", "Błąd dodawania treningu", e)
            }
    }

    fun getAppointmentsForMonthYear(currentUserId: String, month: Int, year: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startOfMonth = calendar.time
        calendar.add(Calendar.MONTH, 1)
        val startOfNextMonth = calendar.time


        val query = db.collection("appointments")
            .where(
                Filter.and(
                    Filter.or(
                        Filter.equalTo("trainerId", currentUserId),
                        Filter.equalTo("clientId", currentUserId)
                    ),
                    Filter.greaterThanOrEqualTo("date", startOfMonth),
                    Filter.lessThan("date", startOfNextMonth)
                )
            )

        query.get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(Appointment::class.java)
                _appointments.value = list
                Log.d("ScheduleRepository", "Pobrano ${list.size} treningów dla $month/$year")
            }
            .addOnFailureListener { e ->
                Log.e("ScheduleRepository", "Błąd pobierania treningów dla miesiąca $month/$year", e)

                if (e.message?.contains("index") == true) {
                    Log.e("ScheduleRepository", "WYMAGANY COMPOSITE INDEX - sprawdź logi Firebase Console")
                }
                _appointments.value = emptyList()
            }
    }
}
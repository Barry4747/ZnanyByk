package com.example.yourapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.data.model.trainings.Appointment
import com.example.myapplication.data.model.trainings.WeeklySchedule
import com.google.firebase.firestore.FirebaseFirestore
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
                    _weeklySchedule.value = schedule!!
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
            }
    }

    fun setWeeklySchedule(trainerId: String, schedule: WeeklySchedule) {
        db.collection("trainerSchedules")
            .document(trainerId)
            .set(schedule)
            .addOnSuccessListener {
                Log.d("ScheduleRepository", "Terminarz zapisany/aktualizowany")
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
            }
    }

    fun addAppointment(appointment: Appointment) {
        db.collection("appointments")
            .add(appointment)
            .addOnSuccessListener {
                Log.d("ScheduleRepository", "Dodano nową rezerwację")
            }
            .addOnFailureListener { e ->
                Log.e("ScheduleRepository", "Błąd dodawania rezerwacji", e)
            }
    }

    fun getAppointmentsForDay(trainerId: String, dayOfWeek: String) {
        db.collection("appointments")
            .whereEqualTo("trainerId", trainerId)
            .whereEqualTo("dayOfWeek", dayOfWeek)
            .get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(Appointment::class.java)
                _appointments.value = list
            }
            .addOnFailureListener { e ->
                Log.e("ScheduleRepository", "Błąd pobierania rezerwacji dla dnia", e)
            }
    }
}

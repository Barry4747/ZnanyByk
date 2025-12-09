package com.example.myapplication.data.model.trainings

data class WeeklySchedule(
    var trainerId: String? = null,
    var monday: List<TrainingSlot>? = null,
    var tuesday: List<TrainingSlot>? = null,
    var wednesday: List<TrainingSlot>? = null,
    var thursday: List<TrainingSlot>? = null,
    var friday: List<TrainingSlot>? = null,
    var saturday: List<TrainingSlot>? = null,
    var sunday: List<TrainingSlot>? = null
)
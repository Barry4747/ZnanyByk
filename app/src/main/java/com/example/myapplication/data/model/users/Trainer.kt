package com.example.myapplication.data.model.users

import androidx.annotation.StringRes
import com.example.myapplication.R

data class Trainer(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String? = null,
    val description: String? = null,
    val categories: List<String>? = null,
    val location: String? = null,
    val ratings: List<Int>? = null,
    var avgRating: String? = null,
    val pricePerHour: Int? = null,
    val experience: Int? = null,
    val images: List<String>? = null,
    val profileImage: String? = null
)


enum class TrainerCategory(@param:StringRes val labelRes: Int) {
    CROSSFIT(R.string.category_crossfit),
    POWERLIFTING(R.string.category_powerlifting),
    YOGA(R.string.category_yoga),
    CARDIO(R.string.category_cardio),
    PILATES(R.string.category_pilates),
    BODYBUILDING(R.string.category_bodybuilding),
    ENDURANCE(R.string.category_endurance),
    SPINNING(R.string.category_spinning),
    MARTIAL_ARTS(R.string.category_martial_arts),
    AEROBICS(R.string.category_aerobics),
    CALISTHENICS(R.string.category_calistenics),
    REHABILITATION(R.string.category_rehabilitation),
    MEDITATION(R.string.category_meditation),
    ZUMBA(R.string.category_zumba),
    AQUA_FITNESS(R.string.category_aqua_fitness),
    STRETCHING(R.string.category_stretching),
    MOBILITY_TRAINING(R.string.category_mobility),
    GYMNASTICS(R.string.category_gymnastics),
    TRACK_AND_FIELD(R.string.category_track_and_field),
    RUNNING_COACH(R.string.category_running),
    SWIMMING_COACH(R.string.category_swimming),
    TRIATHLON(R.string.category_triathlon),
    SPORT_SPECIFIC(R.string.category_sport_specific),
    OUTDOOR_FITNESS(R.string.category_outdoor_fitness)
}

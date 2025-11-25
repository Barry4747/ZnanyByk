package com.example.myapplication.data.model

data class CountryCode(
    val name: String,
    val code: String,
    val dialCode: String,
    val flag: String
)

object CountryCodes {
    val countries = listOf(
        CountryCode("Poland", "PL", "+48", "🇵🇱"),
        CountryCode("United States", "US", "+1", "🇺🇸"),
        CountryCode("United Kingdom", "GB", "+44", "🇬🇧"),
        CountryCode("Germany", "DE", "+49", "🇩🇪"),
        CountryCode("France", "FR", "+33", "🇫🇷"),
        CountryCode("Spain", "ES", "+34", "🇪🇸"),
        CountryCode("Italy", "IT", "+39", "🇮🇹"),
        CountryCode("Netherlands", "NL", "+31", "🇳🇱"),
        CountryCode("Belgium", "BE", "+32", "🇧🇪"),
        CountryCode("Austria", "AT", "+43", "🇦🇹"),
        CountryCode("Switzerland", "CH", "+41", "🇨🇭"),
        CountryCode("Czech Republic", "CZ", "+420", "🇨🇿"),
        CountryCode("Slovakia", "SK", "+421", "🇸🇰"),
        CountryCode("Ukraine", "UA", "+380", "🇺🇦"),
        CountryCode("Russia", "RU", "+7", "🇷🇺"),
        CountryCode("Canada", "CA", "+1", "🇨🇦"),
        CountryCode("Australia", "AU", "+61", "🇦🇺"),
        CountryCode("India", "IN", "+91", "🇮🇳"),
        CountryCode("China", "CN", "+86", "🇨🇳"),
        CountryCode("Japan", "JP", "+81", "🇯🇵"),
        CountryCode("South Korea", "KR", "+82", "🇰🇷"),
        CountryCode("Brazil", "BR", "+55", "🇧🇷"),
        CountryCode("Mexico", "MX", "+52", "🇲🇽"),
        CountryCode("Argentina", "AR", "+54", "🇦🇷")
    ).sortedBy { it.name }

    val default = countries.first { it.code == "PL" }
}


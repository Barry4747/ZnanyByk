import os

API_KEY = os.getenv("GOOGLE_API_KEY")
API_URL = "https://places.googleapis.com/v1/places:searchNearby"
FIELD_MASK = "places.displayName,places.formattedAddress,places.shortFormattedAddress,places.location"

LATITUDE = 51.1078852
LONGITUDE = 17.0385376
LAT_KM_SPAN = 11
LNG_KM_SPAN = 7
SUBDIVISION_RADIUS = 2000

TYPE = "gym"
MAX_RESULT_COUNT = 20
MAX_GYMS = 500

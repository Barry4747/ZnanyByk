import os

from dotenv import load_dotenv

load_dotenv()

from firebase_manager import init_firestore, save_gym
from google_places import fetch_gyms, parse_place_to_gym, generate_grid
from config import MAX_GYMS, LATITUDE, LONGITUDE, SUBDIVISION_RADIUS, LAT_KM_SPAN, LNG_KM_SPAN, MAX_RESULT_COUNT

API_KEY = os.getenv("GOOGLE_API_KEY")


def main():
    firebase_service = os.getenv("FIREBASE_SERVICE_ACCOUNT")
    db = init_firestore(firebase_service)

    total_fetched = 0
    unique_places = set()
    grid_centers = generate_grid(LATITUDE, LONGITUDE, LAT_KM_SPAN, LNG_KM_SPAN, SUBDIVISION_RADIUS)

    for center_lat, center_lng in grid_centers:
        page_token = None
        while True:
            data = fetch_gyms(center_lat, center_lng, SUBDIVISION_RADIUS, max_result_count=MAX_RESULT_COUNT,
                              page_token=page_token)
            places = data.get("places", [])
            if not places:
                break

            for place in places:
                key = (
                    place.get("displayName", {}).get("text", ""),
                    place.get("location", {}).get("latitude"),
                    place.get("location", {}).get("longitude"),
                )
                if key in unique_places:
                    continue
                unique_places.add(key)

                gym = parse_place_to_gym(place)
                save_gym(db, gym)
                total_fetched += 1
                if total_fetched >= MAX_GYMS:
                    print("DEBUG: Reached MAX_GYMS limit, stopping.")
                    print(f"Done. Total gyms fetched: {total_fetched}")
                    return

            page_token = data.get("nextPageToken")
            if not page_token:
                break

    print(f"Done. Total gyms fetched: {total_fetched}")


if __name__ == "__main__":
    main()

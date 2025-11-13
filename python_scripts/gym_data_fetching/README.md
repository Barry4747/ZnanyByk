# Firebase Gym Data Fetch Script

This folder contains a Python script that fetches gym data from Google Places and uploads it to Firebase Firestore.  

> **Note:** This script is **optional** for the Android app. The app works perfectly without it. You only need it if you want to populate or update the Firebase database with gym locations.

---

## What it does

- Pulls gym locations from the Google Places API (new).
- Converts the API data into a format that Firestore understands.
- Splits the area into a grid to cover more ground.
- Uploads gyms to the `gyms` collection in Firebase.
- Avoids duplicates and stops after a set maximum (`MAX_GYMS`).


## Getting Started

### 1. Install dependencies

```bash
pip install -r requirements.txt

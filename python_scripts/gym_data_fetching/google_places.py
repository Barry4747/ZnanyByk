import math

import requests

from config import API_KEY, API_URL, FIELD_MASK


def fetch_gyms(center_lat, center_lng, radius, max_result_count=20, page_token=None):
    headers = {
        "Content-Type": "application/json",
        "X-Goog-Api-Key": API_KEY,
        "X-Goog-FieldMask": FIELD_MASK
    }

    body = {
        "locationRestriction": {
            "circle": {
                "center": {"latitude": center_lat, "longitude": center_lng},
                "radius": radius
            }
        },
        "includedTypes": ["gym"],
        "maxResultCount": max_result_count
    }

    if page_token:
        body["pageToken"] = page_token

    response = requests.post(API_URL, json=body, headers=headers)
    response.raise_for_status()
    return response.json()


def parse_place_to_gym(place):
    location = place.get("location", {})
    return {
        "gymName": place.get("displayName", {}).get("text", ""),
        "gymLocation": {
            "latitude": location.get("latitude"),
            "longitude": location.get("longitude"),
            "formattedAddress": place.get("formattedAddress"),
            "shortFormattedAddress": place.get("shortFormattedAddress")
        }
    }


def km_to_deg_lat(km):
    return km / 111


def km_to_deg_lng(km, lat):
    return km / (111 * math.cos(math.radians(lat)))


def generate_grid(center_lat, center_lng, lat_km_span, lng_km_span, radius_m):
    lat_span_deg = km_to_deg_lat(lat_km_span)
    lng_span_deg = km_to_deg_lng(lng_km_span, center_lat)

    radius_deg_lat = radius_m / 111000
    radius_deg_lng = radius_m / (111000 * math.cos(math.radians(center_lat)))

    lat_steps = math.ceil(lat_span_deg / radius_deg_lat)
    lng_steps = math.ceil(lng_span_deg / radius_deg_lng)

    grid = []
    for i in range(lat_steps + 1):
        for j in range(lng_steps + 1):
            lat = center_lat - lat_span_deg / 2 + i * (lat_span_deg / lat_steps)
            lng = center_lng - lng_span_deg / 2 + j * (lng_span_deg / lng_steps)
            grid.append((lat, lng))
    return grid

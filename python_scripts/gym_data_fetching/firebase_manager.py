import firebase_admin
from firebase_admin import credentials, firestore


def init_firestore(service_account_path: str):
    cred = credentials.Certificate(service_account_path)
    firebase_admin.initialize_app(cred)
    return firestore.client()


def save_gym(db, gym: dict):
    db.collection("gyms").add(gym)
    print(f"Saved: {gym['gymName']}")

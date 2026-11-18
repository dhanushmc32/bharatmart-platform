"""
BharatMart Phase 14 — Decoupled AI Shopping & Order Assistant (Ollama + REST API)
Communicates strictly via Spring Boot REST APIs with JWT; never touches the DB directly.
Requires local/free Ollama (https://ollama.com/) running a model like llama3.2 or qwen2.5.
"""
import os
import requests

API_BASE = os.getenv("BHARATMART_API_URL", "http://localhost:8080/api/v1")
OLLAMA_URL = os.getenv("OLLAMA_URL", "http://localhost:11434/api/generate")
MODEL = os.getenv("OLLAMA_MODEL", "llama3.2")


def search_products(query: str) -> list:
    res = requests.get(f"{API_BASE}/products", params={"q": query}, timeout=10)
    res.raise_for_status()
    return res.json().get("data", [])


def ask_assistant(user_question: str, product_query: str = "") -> str:
    products = search_products(product_query)
    context = "\n".join(
        f"- {p['name']} ({p['brand']}): {', '.join(s['variantName'] + ' @ INR ' + str(s['sellingPrice']) for s in p['skus'])}"
        for p in products
    )
    prompt = (
        f"You are BharatMart's helpful shopping assistant (Cash on Delivery only).\n"
        f"Available catalog:\n{context}\n\n"
        f"Customer question: {user_question}"
    )
    resp = requests.post(OLLAMA_URL, json={"model": MODEL, "prompt": prompt, "stream": False}, timeout=60)
    resp.raise_for_status()
    return resp.json().get("response", "")


if __name__ == "__main__":
    print("BharatMart AI Assistant ready (Ollama + REST API).")

import time
import requests
import numpy as np
from sklearn.ensemble import IsolationForest

# Integração: publica métricas de latência na API do backend.
API_URL = "http://localhost:8080/api/metrics"
API_KEY = "davi-super-secreta"
TARGET_URL = "https://www.youtube.com"

def get_real_latency(url):
    """Mede a latência real (ms) de uma requisição HTTP.

    Em falha de rede/timeout, retorna um sentinel (5000ms) para acionar o guardrail.
    """
    inicio = time.perf_counter()
    try:
        requests.get(url, timeout=5)
        fim = time.perf_counter()
        return int(round((fim - inicio) * 1000))
    except Exception as e:
        print(f"❌ Erro ao medir latência real para {url}: {e}")
        return 5000

# Treinamento: aprende o baseline real para reduzir falsos positivos (data drift).
print("🤖 [AIOps] Inicializando e treinando o modelo de Machine Learning...")

amostras_baseline = []
for _ in range(10):
    lat = get_real_latency(TARGET_URL)
    if lat != 5000:
        amostras_baseline.append(lat)
    time.sleep(1)

if len(amostras_baseline) == 0:
    # Fallback defensivo quando não há conectividade no bootstrap.
    historico_normal = np.random.uniform(low=800, high=1600, size=(1000, 1))
else:
    historico_normal = np.array(amostras_baseline, dtype=float).reshape(-1, 1)
    if historico_normal.shape[0] < 50:
        # IsolationForest é mais estável com mais pontos; replica amostras quando o baseline é curto.
        rep = int(np.ceil(50 / historico_normal.shape[0]))
        historico_normal = np.tile(historico_normal, (rep, 1))

modelo_ia = IsolationForest(contamination=0.05, random_state=42)
modelo_ia.fit(historico_normal)
print("✅ [AIOps] Modelo treinado com sucesso! Iniciando monitoramento em tempo real.\n")


def check_site():
    """Coleta métricas, aplica guardrails e classifica risco (regras + ML)."""
    latency = get_real_latency(TARGET_URL)

    # Guardrail: falha absoluta de conectividade/timeout não depende de modelo estatístico.
    if latency >= 5000:
        previsao = -1
        risk_level = "CRÍTICO"
        ai_message = "Falha Absoluta: Timeout ou conexão recusada (Guardrail)."
    else:
        previsao = modelo_ia.predict([[latency]])[0]
        if previsao == -1:
            risk_level = "CRÍTICO"
            ai_message = "IA Detectou: Anomalia grave no tempo de resposta (Degradação)."
        else:
            risk_level = "OK"
            ai_message = "IA Detectou: Comportamento padrão da rede."

    # Contrato com o backend: campos devem casar com a entidade SiteMetric do Spring Boot.
    payload = {
        "targetUrl": TARGET_URL,
        "latencyMs": latency,
        "httpStatus": 200 if latency < 1000 else 503,
        "riskLevel": risk_level,
        "aiPredictionMessage": ai_message
    }

    headers = {
        "Content-Type": "application/json",
        "X-API-KEY": API_KEY
    }

    try:
        requests.post(API_URL, json=payload, headers=headers)
        print(f"📊 Latência: {latency}ms | Risco: {risk_level}")
        print(f"   ↳ 🧠 Mensagem: {ai_message}\n")
    except Exception as e:
        print(f"Erro ao enviar dados: {e}")

if __name__ == "__main__":
    while True:
        check_site()
        time.sleep(5)
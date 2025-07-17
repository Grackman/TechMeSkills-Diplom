from flask import Flask, request

import logging

from prometheus_client import Counter, generate_latest

# Настройка логирования
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

app = Flask(__name__)

# Метрика для Prometheus
request_counter = Counter('http_requests_total', 'Total HTTP requests', ['endpoint'])


@app.route('/')
def home():
    logger.info(f"Request to / from {request.remote_addr}")
    request_counter.labels(endpoint='/').inc()
    return """
    <html>
        <head>
            <title>TMS Project</title>
            <style>
                body { font-family: Arial, sans-serif; text-align: center; background-color: #f0f4f8; }
                h1 { color: #2c3e50; margin-top: 50px; }
                p { color: #7f8c8d; font-size: 18px; }
            </style>
        </head>
        <body>
            <h1>Welcome to TMS Project!</h1>
            <p>This is a simple Flask app for your DevOps pipeline.</p>
        </body>
    </html>
    """


@app.route('/metrics')
def metrics():
    logger.info(f"Request to /metrics from {request.remote_addr}")
    request_counter.labels(endpoint='/metrics').inc()
    return generate_latest(), 200, {'Content-Type': 'text/plain; version=0.0.4'}


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
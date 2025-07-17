```markdown
# Task Management System (TMS) Project

![TMS Architecture](docs/screenshots/architecture.png)

## Overview

The Task Management System (TMS) is a comprehensive DevOps project demonstrating a full CI/CD pipeline, containerization, and real-time monitoring with alerting. The core application is a Flask-based web service written in Python, automated through a Jenkins CI/CD pipeline, containerized using Docker, and monitored with Prometheus and Grafana. The entire infrastructure is provisioned and managed using Ansible for automation.

This project showcases the end-to-end DevOps lifecycle: from code commits in a version control system to automated build, test, deployment, monitoring, and alerting via Telegram.

---

## Architecture

The project is distributed across multiple servers:

1. **Gitea (http://192.168.0.20:3000)**:
   - Version control system hosting the `admin/project-tms` repository (branch: `main`).
   - Triggers Jenkins builds via webhook on code push.

2. **Jenkins (http://192.168.0.22:8080)**:
   - CI/CD server running the `Project-TMS-Pipeline`.
   - Automates code checkout, linting, testing, building, pushing Docker images, and deployment.

3. **Docker Registry (192.168.0.22:5000)**:
   - Stores the Docker image `project-tms:latest`.
   - Secured with htpasswd authentication (user: `root`).

4. **Application Server (vm-app, http://192.168.0.24:5000)**:
   - Runs the Flask application in a Docker container.
   - Exposes endpoints:
     - `/`: HTML welcome page ("Welcome to TMS Project!").
     - `/metrics`: Prometheus metrics (`http_requests_total`).
   - Managed via `docker-compose.yml` in `/opt/project-tms`.

5. **Monitoring Server (vm-monitor, 192.168.0.25)**:
   - **Prometheus (http://192.168.0.25:9090)**: Scrapes metrics from the app and Node Exporter.
   - **Grafana (http://192.168.0.25:3000)**: Visualizes dashboards for request metrics.
   - **Alertmanager (http://192.168.0.25:9093)**: Sends alerts to Telegram for low disk space.

**Architecture Diagram**:
<<<<<<< HEAD

![Architecture Diagram](docs/screenshots/architecture.png)
=======
```
[Gitea:192.168.0.20] → (webhook) → [Jenkins:192.168.0.22] → (push) → [Docker Registry:192.168.0.22:5000]
                                                            ↓
                                                        [vm-app:192.168.0.24:5000]
                                                            ↓ (metrics)
                                                        [Prometheus:192.168.0.25:9090] ↔ [Grafana:192.168.0.25:3000]
                                                            ↓ (alerts)
                                                        [Alertmanager:192.168.0.25:9093] → [Telegram]
```
>>>>>>> ff486e6a491976a708ae5bac35b8bd91ddad149a

---

## Components

### Source Code
Located in `src/`:
- `app.py`: Flask application with `/` (welcome page) and `/metrics` (Prometheus metrics).
- `test_app.py`: Unit tests verifying the endpoints.
- `Dockerfile`: Builds the Docker image based on `python:3.9-slim`.
- `requirements.txt`: Lists dependencies (`Flask==2.2.5`, `prometheus_client==0.14.1`).
- `Jenkinsfile`: Defines the CI/CD pipeline.

### CI/CD Pipeline
The Jenkins pipeline (`Project-TMS-Pipeline`) automates:
1. **Checkout**: Clones code from Gitea (`main` branch).
2. **Lint**: Runs `flake8` for code style checks.
3. **Test**: Executes unit tests (`unittest`) in a Docker container.
4. **Build Docker Image**: Builds `192.168.0.22:5000/project-tms:latest`.
5. **Push to Registry**: Pushes the image to the Docker Registry.
6. **Deploy to vm-app**: Deploys the app via `docker-compose` on `vm-app`.

### Monitoring
- **Prometheus**: Scrapes metrics from:
  - `http://192.168.0.24:5000/metrics` (app metrics like `http_requests_total`).
  - `192.168.0.24:9100` (Node Exporter for system metrics like disk space).
- **Grafana**: Displays dashboards for request counts and rates (e.g., `rate(http_requests_total{endpoint="\/"}[5m])`).
- **Alertmanager**: Sends Telegram notifications for alerts.

### Alerting
The system monitors disk space on `vm-app` (192.168.0.24) using Node Exporter and triggers alerts via Alertmanager:
- **Alert Rule**: `LowDiskSpace` fires when free disk space on `/` is <20% for 1 minute.
- **Notification**: Sent to a Telegram group (chat_id: `-4870244448`) with details like:
  ```
  Low disk space on 192.168.0.24:9100: 192.168.0.24:9100 has less than 20% free disk space (current: X%).
  ```
- **Configuration**:
  - Prometheus rule in `/opt/monitoring/prometheus/alerts.yml`.
  - Alertmanager configuration in `/opt/monitoring/alertmanager/alertmanager.yml`.

### Notifications
- **Telegram**: Sends build status (SUCCESS/FAILURE) and disk space alerts to a Telegram group.
- Configured via Jenkins (`telegram-bot-token`) and Alertmanager.

---

## Setup

### Prerequisites
- Ansible, Docker, and docker-compose installed on all servers.
- Servers:
  - Gitea: 192.168.0.20
  - Jenkins/Registry: 192.168.0.22
  - vm-app: 192.168.0.24
  - vm-monitor: 192.168.0.25

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/tms-project.git
   cd tms-project
   ```
2. Update `ansible/inventory.ini` with your server IPs and credentials.
3. Run Ansible to provision the infrastructure:
   ```bash
   ansible-playbook -i ansible/inventory.ini ansible/site.yml
   ```

### Usage
1. **Push Code**:
   ```bash
   cd src
   git add .
   git commit -m "Update app"
   git push
   ```
2. **Monitor Pipeline**:
   - Jenkins: `http://192.168.0.22:8080/job/Project-TMS-Pipeline`.
   - Telegram: Receive build status notifications.
3. **Access Application**:
   - `http://192.168.0.24:5000/`: Welcome page.
   - `http://192.168.0.24:5000/metrics`: Metrics.
4. **Monitor Metrics**:
   - Prometheus: `http://192.168.0.25:9090` (check Targets).
   - Grafana: `http://192.168.0.25:3000` (login: admin/<password>).
   - Alerts: Telegram notifications for low disk space (<20%).
5. **Check Alerts**:
   - Simulate low disk space:
     ```bash
     ssh deploy@192.168.0.24
     dd if=/dev/zero of=/bigfile bs=1G count=10
     ```
   - Receive Telegram alert within 1 minute.

---

## Screenshots

| Component       | Screenshot |
|----------------|------------|
| Gitea          | ![Gitea](docs/screenshots/gitea.png) |
| Jenkins        | ![Jenkins](docs/screenshots/jenkins.png) |
| Application    | ![App](docs/screenshots/app.png) |
| Prometheus     | ![Prometheus](docs/screenshots/prometheus.png) |
| Grafana        | ![Grafana](docs/screenshots/grafana.png) |
| Alertmanager   | ![Alertmanager](docs/screenshots/alertmanager.png) |

---

## QR Code

Scan to view the project on GitHub:

![QR Code](docs/screenshots/qr-code.png)

---

## License
<<<<<<< HEAD

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

```
=======
This project is for educational purposes and is not licensed for commercial use.
>>>>>>> ff486e6a491976a708ae5bac35b8bd91ddad149a

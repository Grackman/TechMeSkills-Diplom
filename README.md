# Task Management System (TMS) Project

## Overview
The Task Management System (TMS) is a demonstration of a modern DevOps pipeline, showcasing continuous integration, continuous deployment (CI/CD), containerization, and monitoring. The project features a simple Flask-based web application written in Python, automated through a Jenkins CI/CD pipeline, containerized with Docker, and monitored using Prometheus and Grafana. The entire infrastructure is provisioned and configured using Ansible for automation.

This project is designed to illustrate the full DevOps lifecycle: from code push in a version control system (Gitea) to automated build, test, deployment, and real-time monitoring of the application.

---

## Architecture

The TMS project consists of the following components, each running on dedicated servers:

1. **Gitea (http://192.168.0.20:3000)**:
   - Version control system hosting the repository `admin/project-tms` (branch: `main`).
   - Stores source code (`app.py`, `test_app.py`, `Dockerfile`, `Jenkinsfile`, `requirements.txt`).
   - Triggers Jenkins builds via webhook on code push.

2. **Jenkins (http://192.168.0.22:8080)**:
   - CI/CD server managing the pipeline (`Project-TMS-Pipeline`).
   - Automates linting, testing, building, pushing Docker images, and deploying to production.
   - Configured with credentials for Gitea, Docker Registry, and SSH access to the app server.

3. **Docker Registry (192.168.0.22:5000)**:
   - Stores the Docker image `project-tms:latest`.
   - Secured with htpasswd authentication (user: `root`, password: configured via Ansible).

4. **Application Server (vm-app, http://192.168.0.24:5000)**:
   - Runs the Flask application in a Docker container.
   - Exposes endpoints:
     - `/`: HTML welcome page ("Welcome to TMS Project!").
     - `/metrics`: Prometheus metrics (`http_requests_total`).
   - Managed via `docker-compose.yml` in `/opt/project-tms`.

5. **Monitoring Server (vm-monitor, 192.168.0.25)**:
   - **Prometheus (http://192.168.0.25:9090)**: Scrapes metrics from `http://192.168.0.24:5000/metrics` every 15 seconds.
   - **Grafana (http://192.168.0.25:3000)**: Visualizes metrics in dashboards (e.g., request counts, rates).
   - Configured via `docker-compose.yml` in `/opt/monitoring`.

**Architecture Diagram**:
```
[Gitea:192.168.0.20] → (webhook) → [Jenkins:192.168.0.22] → (push) → [Docker Registry:192.168.0.22:5000]
                                                            ↓
                                                        [vm-app:192.168.0.24:5000]
                                                            ↓ (metrics)
                                                        [Prometheus:192.168.0.25:9090] ↔ [Grafana:192.168.0.25:3000]
                                                            ↓ (alerts)
                                                        [Alertmanager:192.168.0.25:9093] → [Telegram]
```

---

## Project Components

### Source Code
- **app.py**: Flask application with two endpoints:
  - `/`: Returns an HTML page with a welcome message.
  - `/metrics`: Exposes Prometheus metrics (e.g., `http_requests_total{endpoint="/"}`).
- **test_app.py**: Unit tests for the Flask app, verifying endpoints `/` and `/metrics`.
- **Dockerfile**: Builds the Docker image based on `python:3.9-slim`, installs dependencies, and runs `app.py`.
- **requirements.txt**: Lists dependencies (`Flask==2.2.5`, `prometheus_client==0.14.1`).
- **Jenkinsfile**: Defines the CI/CD pipeline with stages: Checkout, Lint, Test, Build, Push, Deploy.

### CI/CD Pipeline
The Jenkins pipeline automates the following stages:
1. **Checkout**: Clones the repository from Gitea (`main` branch).
2. **Lint**: Runs `flake8` to check code style in a Docker container (`python:3.9-slim`).
3. **Test**: Executes unit tests (`unittest`) in a Docker container.
4. **Build Docker Image**: Builds the Docker image `192.168.0.22:5000/project-tms:latest`.
5. **Push to Registry**: Pushes the image to the Docker Registry using credentials (`docker-registry-credentials`).
6. **Deploy to vm-app**: SSH to vm-app (user: `deploy`, credentials: `vm-app-ssh`), pulls the image, and starts the container via `docker-compose`.

### Monitoring
- **Prometheus**: Configured in `/opt/monitoring/prometheus/prometheus.yml` to scrape metrics from `http://192.168.0.24:5000/metrics`.
- **Grafana**: Displays dashboards with metrics like `http_requests_total{endpoint="\/"}` and `rate(http_requests_total[5m])`.

### Notifications
- **Telegram**: Sends build and deployment status to a Telegram group using a bot (configured via Jenkins Telegram Notifications plugin).
- Notifications include build number, status (SUCCESS/FAILURE), and a link to the Jenkins build.

---

## Setup and Configuration

The entire infrastructure is provisioned using **Ansible**. The roles are defined as follows:

1. **gitea** (192.168.0.20):
   - Installs Gitea in a Docker container.
   - Creates user `admin` and repository `admin/project-tms`.
   - Configures webhook to trigger Jenkins (`http://192.168.0.22:8080/gitea-webhook/post`).

2. **jenkins** (192.168.0.22):
   - Installs Jenkins, Java, Docker, and docker-compose.
   - Configures plugins (e.g., Gitea, Telegram Notifications).
   - Sets up credentials: `gitea-credentials-id`, `docker-registry-credentials`, `vm-app-ssh`, `telegram-bot-token`.
   - Creates pipeline `Project-TMS-Pipeline`.

3. **docker-registry** (192.168.0.22:5000):
   - Runs Docker Registry in `/opt/docker-registry` with htpasswd authentication.
   - Configures `insecure-registries` in `/etc/docker/daemon.json`.

4. **vm-app** (192.168.0.24):
   - Installs Docker and docker-compose.
   - Deploys `/opt/project-tms/docker-compose.yml` to run the TMS app.
   - Configures SSH access for user `deploy` and Docker auth for pulling images.

5. **monitoring** (192.168.0.25):
   - Deploys Prometheus and Grafana in `/opt/monitoring` via docker-compose.
   - Configures Prometheus to scrape metrics from `192.168.0.24:5000/metrics`.

**Run Ansible**:
```bash
ansible-playbook -i inventory.ini site.yml
```

---

## How to Use

1. **Push Code**:
   - Clone the repository:
     ```bash
     git clone http://192.168.0.20:3000/admin/project-tms.git
     ```
   - Make changes (e.g., edit `app.py` or `README.md`).
   - Commit and push:
     ```bash
     git add .
     git commit -m "Update app"
     git push
     ```

2. **CI/CD Pipeline**:
   - Gitea webhook triggers Jenkins (`http://192.168.0.22:8080`).
   - Pipeline runs: Checkout → Lint → Test → Build → Push → Deploy.
   - Check build status: `http://192.168.0.22:8080/job/Project-TMS-Pipeline`.

3. **Access Application**:
   - Open `http://192.168.0.24:5000/` for the welcome page.
   - Open `http://192.168.0.24:5000/metrics` for Prometheus metrics.

4. **Monitor Metrics**:
   - Prometheus: `http://192.168.0.25:9090` → Status → Targets (check 'tms-app').
   - Grafana: `http://192.168.0.25:3000` → Dashboards → TMS Metrics (login: admin/<your_password>).
   - Generate traffic: `curl http://192.168.0.24:5000/` to see metrics update.

5. **Check Notifications**:
   - Telegram: Build status sent to the configured group (e.g., "Build #N completed with status: SUCCESS").
   - Email (optional): Configured via Jenkins Email Notification.

---

## Verification

To verify the project:
- **Gitea**: Check webhook delivery in Settings → Webhooks.
- **Jenkins**: View Build History for `Project-TMS-Pipeline`.
- **Application**: Access `http://192.168.0.24:5000`.
- **Monitoring**: Check Prometheus (`http://192.168.0.25:9090/targets`) and Grafana dashboards.
- **Notifications**: Confirm Telegram messages after builds.

---

## Future Improvements
- Add HTTPS for Gitea, Jenkins, Docker Registry, and Grafana using Let’s Encrypt.
- Implement alerts in Prometheus for high request rates or errors.
- Add more metrics to `app.py` (e.g., request latency with `Histogram`).
- Configure backup for Jenkins and monitoring data.

---

## License
This project is for educational purposes and is not licensed for commercial use.

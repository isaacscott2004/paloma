# 🧠 PALOMA: Proactive Mental Health Support App

> *Helping those who struggle connect with those who care — without needing to ask.*

## 📱 About the App

PALOMA is a mental health support app that empowers users to manage their emotional well-being and builds a bridge between them and their trusted contacts. Through a combination of **daily mood check-ins**, **medication tracking**, **a predictive algorithm**, and **automated notifications**, the app identifies early signs of emotional distress and gently alerts the user's support network when help is most needed.

This app was born from personal experience and designed with compassion at its core.

---

## 🔑 Core Features

### ✅ Daily Check-Ins
Users log scores for:
- Mood
- Energy
- Motivation
- Suicidal Thoughts (optional)

Scores are tracked over time to detect downward trends and trigger support mechanisms.

### 💊 Medication Tracker
Gently reminds users to take their medication and logs adherence. Missed doses can factor into alerting.

### 🔔 Trusted Contact Notifications
Users assign trusted contacts who are automatically notified when concerning patterns are detected. Each contact can receive a personalized note written by the user, guiding their response.

### 🧠 Predictive Algorithm
Using historical check-in and med log data, the app anticipates depressive episodes and enables early intervention. This allows users to prepare proactively, increasing the chances of positive coping outcomes.

---

## 🧩 Architecture Overview

You can open the web application by running the `:composeApp:wasmJsBrowserDevelopmentRun` Gradle task.

The backend is responsible for user management, data storage, and the logic that powers notification triggering and analytics.

---

## 🧾 Database Schema

Key tables include:
- `users` — user accounts
- `auth`, `auth_credentials` — secure login and token management
- `daily_checkins` — mood and mental health tracking
- `medications`, `med_logs` — medication tracking
- `trusted_contacts` — designated support network
- `alerts` — records of triggered notifications
- `score_history` — longitudinal data for AI models

See the [`MENTAL HEALTH - Database Schema`](./MENTAL_HEALTH-Database_Schema.pdf) PDF for full entity relationships.

---

## 📊 API Design

See the [`PALOMA API Diagram`](./PALOMA%20API_DIAGRAM.png) for full sequence interactions

The API supports:
- Authentication (register/login/logout)
- Daily check-ins and med tracking
- Notification triggering and alerting
- Trusted contact management

---

## 📜 License

All rights reserved.
This project is proprietary and not open for reuse, reproduction, or modification without written permission. 

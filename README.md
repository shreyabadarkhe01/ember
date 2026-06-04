# 🔥 Ember — Adaptive Habit Tracker

> *"We don't reset your streak. We keep your ember alive."*

Ember is a full-stack habit tracking app that adapts your daily goals to your energy level. Instead of rigid all-or-nothing habits, Ember scales each habit across three versions — Minimal, Lite, and Full — based on how you're feeling that day.

**Live Demo:** [ember-neon-theta.vercel.app](https://ember-neon-theta.vercel.app)

---

## How It Works

1. **Check in daily** — rate your energy (1–5) manually or via biometric data (sleep, heart rate, steps, HRV)
2. **Ember scales your habits** — low energy day? Minimal version. High energy? Full version.
3. **AI nudge** — GPT-4.1 Nano generates a personalised motivational nudge after each check-in
4. **Weekly Autopsy** — pattern detection + AI insight on your energy and habit trends

---

## Tech Stack

| Layer | Technology                                      |
|-------|-------------------------------------------------|
| Backend | Spring Boot 3, Java 21, JWT Auth, JPA/Hibernate |
| Frontend | React 18, Vite, Axios                           |
| Mobile | Flutter 3, Health Connect API (Android)         |
| Database | PostgreSQL (Supabase)                           |
| AI | OpenAI GPT-4.1 Nano                             |
| Deploy | Render (backend), Vercel (frontend), Docker     |

---

## Features

- **Adaptive habit scaling** — 3 versions per habit (Minimal/Lite/Full) driven by energy score
- **Dual check-in modes** — manual energy selector or biometric form (sleep, HR, HRV, steps, calories)
- **AI nudges** — personalised post check-in motivation via GPT-4.1 Nano
- **Streak tracking** — consecutive day streaks, resets on skip, minimal version counts
- **Weekly Autopsy** — energy chart, habit performance, pattern detection, AI insight
- **Health Connect integration** — auto-detect energy from wearables (Galaxy Watch, Pixel Watch, Fitbit etc.)
- **Archive/unarchive habits** — hide habits without losing streak history
- **Flutter mobile app** — full habit flow with bottom nav, autopsy chart, nudge card



## Project Structure
    ember/
    ├── backend/          # Spring Boot API
    │   ├── auth/         # JWT authentication
    │   ├── habit/        # Habit CRUD + adaptive scaling
    │   ├── checkin/      # Daily check-ins
    │   ├── biometric/    # Health Connect biometric processing
    │   ├── ai/           # OpenAI nudge + autopsy insight
    │   ├── autopsy/      # Weekly report generation
    │   └── habitlog/     # Daily habit completion tracking
    ├── frontend/         # React/Vite web app
    └── ember_mobile/     # Flutter mobile app

## Local Setup

### Backend
```bash
cd backend
# Create src/main/resources/application-local.properties with:
# SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ember
# SPRING_DATASOURCE_USERNAME=your_username
# SPRING_DATASOURCE_PASSWORD=your_password
# JWT_SECRET=your-secret-key-min-32-chars
# JWT_EXPIRATION=86400000
# openai.api.key=your_openai_key

mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
# Create .env with: VITE_API_URL=http://localhost:8081
npm run dev
```

### Mobile
```bash
cd ember_mobile
flutter pub get
flutter run
```

### Docker (backend only)
```bash
cd backend
docker build -t ember-backend .
docker run -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=... \
  -e SPRING_DATASOURCE_USERNAME=... \
  -e SPRING_DATASOURCE_PASSWORD=... \
  -e JWT_SECRET=... \
  -e JWT_EXPIRATION=86400000 \
  -e openai.api.key=... \
  ember-backend
```

---

## API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login, returns JWT |
| GET | `/api/users/{id}/habits` | Get all habits |
| POST | `/api/users/{id}/habits/{hId}/complete` | Complete habit |
| POST | `/api/users/{id}/habits/{hId}/skip` | Skip habit |
| GET | `/api/users/{id}/checkins/today` | Today's check-in |
| POST | `/api/users/{id}/checkins` | Create check-in |
| POST | `/api/users/{id}/biometric-checkin` | Biometric check-in |
| POST | `/api/users/{id}/ai/nudge` | Generate AI nudge |
| GET | `/api/users/{id}/autopsy` | Weekly autopsy report |

---

## Architecture Decisions

**Package-by-feature** — each feature owns its controller, service, entity and repository. Scales better than package-by-layer as the codebase grows.

**DTO boundaries** — entities never leave the service layer. MapStruct handles mapping at compile time with zero reflection overhead.

**Adaptive scaling** — energy score drives habit version selection client-side, keeping the backend stateless per request. Version text stored on the habit entity, selection logic in the frontend.

**Health Connect** — Flutter reads biometrics via Android's unified Health Connect API, supporting any compatible wearable without manufacturer-specific SDKs.

---

## Roadmap

- [ ] Play Store deployment (Health Connect live permissions)
- [ ] Password reset via email
- [ ] Account deletion
- [ ] Push notifications for habit reminders
- [ ] Onboarding flow
- [ ] Pattern detection improvements (more HabitLog data needed)

---

*Built by Shreya Badarkhe — [github.com/shreyabadarkhe01](https://github.com/shreyabadarkhe01)*
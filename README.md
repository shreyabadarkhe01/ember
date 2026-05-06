# 🔥 Ember — Adaptive Habit Tracker

> *Your habits should adapt to your energy, not the other way around.*

Ember is a full-stack habit tracking app that scales your daily habit targets based on your energy level. Instead of a rigid "did you or didn't you" system, Ember gives you three versions of every habit — minimal, lite, and full — and picks the right one for how you're feeling today. Eventually, energy scores will be detected **automatically** from Samsung Health biometric data via Health Connect.

---

## 📸 Screenshots

> Coming soon — UI screenshots will be added after deployment.

---

## ✨ Features

### Current
- 🔐 **JWT Authentication** — secure register and login
- ⚡ **Adaptive Habits** — 3 versions per habit (Minimal / Lite / Full)
- 📊 **Daily Check-in** — manual energy score (1–5) that scales your habits for the day
- ✅ **Habit Tracking** — mark habits Done or Skipped, with streak counting
- ↩️ **Undo actions** — reset a habit back to Active
- 📋 **Swagger UI** — full API documentation with JWT auth support
- 🌐 **React Frontend** — responsive web app with dark ember-themed design

### Coming Soon
- 📱 **Samsung Health Integration** — auto energy score from sleep, HRV, resting heart rate via Health Connect
- 📊 **Weekly Autopsy** — 7-day pattern analysis (best/worst days, consistency score, habit completion rate)
- 🤖 **Claude AI Insights** — personalised nudges after check-in, weekly AI-generated pattern analysis
- 🚀 **Deployment** — Railway (backend) + Vercel (frontend)
- 📲 **Flutter Mobile App** — Android app with Health Connect integration

---

## 🛠️ Tech Stack

### Backend
| Technology | Purpose |
|------------|---------|
| Java 21 | Language |
| Spring Boot 3.2.5 | Framework |
| Spring Security + JWT | Authentication |
| PostgreSQL | Database |
| Spring Data JPA + Hibernate | ORM |
| MapStruct | DTO mapping |
| Lombok | Boilerplate reduction |
| springdoc-openapi 2.3.0 | Swagger UI |
| Maven | Build tool |

### Frontend
| Technology | Purpose |
|------------|---------|
| React 18 | UI framework |
| Vite | Build tool |
| React Router v6 | Routing |
| Axios | HTTP client |
| CSS Variables | Theming |

### Planned
| Technology | Purpose |
|------------|---------|
| Flutter | Android mobile app |
| Health Connect API | Biometric data (Samsung Health) |
| Anthropic Claude API | AI nudges and weekly insights |
| Railway | Backend hosting |
| Vercel | Frontend hosting |

---

## 🏗️ Architecture

```
┌─────────────────────┐     ┌─────────────────────┐
│   React Frontend    │     │  Flutter Mobile App  │
│   localhost:5173    │     │  (coming soon)       │
└────────┬────────────┘     └──────────┬───────────┘
         │ HTTP + JWT                  │ HTTP + JWT
         ▼                             ▼
┌─────────────────────────────────────────────────┐
│           Spring Boot Backend                   │
│              localhost:8081                     │
│                                                 │
│  ┌──────────┐ ┌──────────┐ ┌─────────────────┐ │
│  │   Auth   │ │  Habits  │ │    Check-ins     │ │
│  └──────────┘ └──────────┘ └─────────────────┘ │
│  ┌──────────┐ ┌──────────┐ ┌─────────────────┐ │
│  │ Autopsy  │ │ Claude AI│ │ Energy Calculator│ │
│  │ (soon)   │ │  (soon)  │ │    (soon)        │ │
│  └──────────┘ └──────────┘ └─────────────────┘ │
└─────────────────────┬───────────────────────────┘
                      │
              ┌───────▼────────┐
              │   PostgreSQL   │
              └────────────────┘

Future biometric data flow:
Galaxy Watch → Samsung Health → Health Connect → Flutter App → Backend
```

---

## 🚀 Getting Started

### Prerequisites
- Java 21
- Maven
- PostgreSQL
- Node.js 20+
- IntelliJ IDEA (recommended)
- VS Code (for frontend)

### Backend Setup

1. **Clone the repository**
```bash
git clone https://github.com/shreyabadarkhe01/ember.git
cd ember
```

2. **Create PostgreSQL database**
```sql
CREATE DATABASE ember;
```

3. **Configure `application.properties`**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ember
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

4. **Run the backend**
```bash
mvn spring-boot:run
```

Backend runs on `http://localhost:8081`

5. **Open Swagger UI**
```
http://localhost:8081/swagger-ui/index.html
```

### Frontend Setup

1. **Navigate to frontend directory**
```bash
cd ember-frontend
```

2. **Install dependencies**
```bash
npm install
```

3. **Create `.env.local`**
```env
VITE_API_URL=http://localhost:8081
```

4. **Run the frontend**
```bash
npm run dev
```

Frontend runs on `http://localhost:5173`

---

## 📡 API Endpoints

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and get JWT token |

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | Get all users |
| GET | `/api/users/{id}` | Get user by ID |

### Habits
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users/{id}/habits` | Create a habit |
| GET | `/api/users/{id}/habits` | Get all habits |
| PATCH | `/api/users/{id}/habits/{habitId}` | Edit habit |
| PATCH | `/api/users/{id}/habits/{habitId}/complete` | Mark habit done ✅ |
| PATCH | `/api/users/{id}/habits/{habitId}/skip` | Skip habit ⏭️ |
| PATCH | `/api/users/{id}/habits/{habitId}/reset` | Undo done/skip ↩️ |
| PATCH | `/api/users/{id}/habits/{habitId}/scale` | Scale habit by energy score |

### Check-ins
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users/{id}/checkins` | Create daily check-in |
| GET | `/api/users/{id}/checkins` | Get all check-ins |
| GET | `/api/users/{id}/checkins/today` | Get today's check-in |

### Coming Soon
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users/{id}/biometric-checkin` | Auto check-in from Health Connect |
| GET | `/api/users/{id}/autopsy` | Weekly pattern report |
| POST | `/api/users/{id}/ai/nudge` | Get Claude AI nudge |
| GET | `/api/users/{id}/ai/autopsy-insight` | Get Claude AI weekly insight |

---

## 📊 Data Model

### Habit
```json
{
  "id": 1,
  "name": "Morning Run",
  "minimalVersion": "Walk 10 mins",
  "liteVersion": "Run 20 mins",
  "fullVersion": "Run 30 mins + stretching",
  "status": "ACTIVE",
  "streakCount": 5,
  "userId": 1,
  "createdAt": "2026-05-01T08:00:00"
}
```

### Check-in
```json
{
  "id": 1,
  "energyScore": 4,
  "note": "Slept well last night",
  "userId": 1,
  "createdAt": "2026-05-05T08:30:00"
}
```

### Energy Score Scale
| Score | Label | Habits shown |
|-------|-------|-------------|
| 1 | 😴 Exhausted | Minimal version |
| 2 | 😔 Low | Minimal version |
| 3 | 😐 Moderate | Lite version |
| 4 | 😊 Good | Full version |
| 5 | 🔥 Energised | Full version |

---

## 🗺️ Roadmap

### Phase 1 — Core Backend ✅
- [x] User authentication (JWT)
- [x] Habit CRUD with 3 energy versions
- [x] Daily check-in with energy score
- [x] Habit status tracking (Done / Skipped / Reset)
- [x] Streak counting
- [x] Swagger UI with JWT support

### Phase 2 — Frontend ✅
- [x] Register / Login pages
- [x] Dashboard with check-in form
- [x] Habit list with energy-scaled versions
- [x] Add / Edit habits
- [x] Mark Done / Skip / Undo
- [x] Ember dark theme

### Phase 3 — Biometric Energy Score 🔜
- [ ] Health Connect integration (Android)
- [ ] Energy Calculator from sleep + HRV + resting heart rate + steps
- [ ] Auto check-in from biometric data
- [ ] Manual override option

### Phase 4 — Autopsy + AI 🔜
- [ ] Weekly autopsy report (7-day pattern analysis)
- [ ] Best/worst day detection
- [ ] Habit completion rate
- [ ] Claude AI nudges after check-in
- [ ] Claude AI weekly insight generation
- [ ] Habit adjustment suggestions from AI

### Phase 5 — Flutter Mobile App 🔜
- [ ] Flutter Android app
- [ ] Health Connect SDK integration
- [ ] Auto morning check-in on app open
- [ ] Push notifications for habit reminders
- [ ] Dashboard, Habits, Autopsy screens

### Phase 6 — Deployment 🔜
- [ ] Backend on Railway
- [ ] Frontend on Vercel
- [ ] Flutter app on Google Play Store
- [ ] CI/CD pipeline

---

## 🔮 Future Vision

The end goal for Ember is **zero-friction habit tracking**:

```
You wake up
    ↓
Ember reads last night's sleep, HRV, and resting heart rate
from Samsung Health via Health Connect
    ↓
Calculates your energy score automatically
    ↓
Scales all your habits to the right version for today
    ↓
Claude AI sends you a personalised morning nudge
    ↓
You go about your day — marking habits done as you go
    ↓
Sunday evening — Claude analyses your week:
"Your energy drops every Wednesday. Your Tuesday sleep
averages 5.5 hours — try sleeping 30 mins earlier on Tuesdays."
```

No manual energy entry. No guilt for missing a day. Just adaptive, intelligent habit tracking that works with your body.

---

## 🤝 Contributing

This project is currently in active development. Contributions, suggestions, and feedback are welcome!

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add your feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License.

---

## 👩‍💻 Author

**Shreya Badarkhe**
- GitHub: [@shreyabadarkhe01](https://github.com/shreyabadarkhe01)
- Project: [github.com/shreyabadarkhe01/ember](https://github.com/shreyabadarkhe01/ember)

---

> Built with ☕ and 🔥 in Pune, Maharashtra.

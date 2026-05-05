import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { habitApi, checkinApi } from '../services/api';
import CheckInForm from '../components/CheckInForm';
import HabitList from '../components/HabitList';
import HabitForm from '../components/HabitForm';

export default function Dashboard() {
  const { user, logout } = useAuth();
  const [habits, setHabits] = useState([]);
  const [latestCheckin, setLatestCheckin] = useState(null);
  const [showCheckin, setShowCheckin] = useState(false);
  const [showHabitForm, setShowHabitForm] = useState(false);
  const [loading, setLoading] = useState(true);

  const fetchData = useCallback(async () => {
    try {
      const [habitsRes, checkinRes] = await Promise.all([
        habitApi.getAll(user.id),
        checkinApi.getLatest(user.id).catch(() => ({ data: null })),
      ]);
      setHabits(habitsRes.data);
      setLatestCheckin(checkinRes.data);
    } catch (err) {
      console.error('Failed to load dashboard:', err);
    } finally {
      setLoading(false);
    }
  }, [user.id]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const isCheckedInToday = () => {
    if (!latestCheckin) return false;
    const today = new Date().toDateString();
    const checkinDate = new Date(latestCheckin.date).toDateString();
    return today === checkinDate;
  };

  const getGreeting = () => {
    const h = new Date().getHours();
    if (h < 12) return 'Good morning';
    if (h < 17) return 'Good afternoon';
    return 'Good evening';
  };

  const energyScore = latestCheckin?.energyScore;
  const energyEmojis = { 1: '😴', 2: '😔', 3: '😐', 4: '😊', 5: '🔥' };

  if (loading) return <div className="loading-screen"><div className="loader">🔥</div></div>;

  return (
    <div className="dashboard">

      {/* Header */}
      <header className="dash-header">
        <div className="dash-brand">🔥 ember</div>
        <div className="dash-user">
          <span>{user.name}</span>
          <button className="btn-logout" onClick={logout}>Sign out</button>
        </div>
      </header>

      <main className="dash-main">

        {/* Greeting + energy */}
        <div className="dash-greeting">
          <h1>{getGreeting()}, {user.name.split(' ')[0]} 👋</h1>
          {isCheckedInToday() ? (
            <div className="energy-pill">
              Energy today: {energyEmojis[energyScore]} {energyScore}/5
            </div>
          ) : (
            <div className="checkin-nudge">
              Start your day with a check-in to scale your habits ↓
            </div>
          )}
        </div>

        {/* Check-in section */}
        {!isCheckedInToday() && (
          <div>
            {showCheckin ? (
              <CheckInForm onSuccess={() => { setShowCheckin(false); fetchData(); }} />
            ) : (
              <button className="btn-checkin" onClick={() => setShowCheckin(true)}>
                ✨ Check in for today
              </button>
            )}
          </div>
        )}

        {/* Habits section */}
        <div className="habits-section">
          <div className="section-header">
            <h2>Today's habits</h2>
            <button className="btn-add" onClick={() => setShowHabitForm(true)}>+ Add habit</button>
          </div>

          {showHabitForm && (
            <HabitForm
              onSuccess={() => { setShowHabitForm(false); fetchData(); }}
              onCancel={() => setShowHabitForm(false)}
            />
          )}

          <HabitList
            habits={habits}
            energyScore={energyScore}
            onRefresh={fetchData}
          />
        </div>

        {/* Autopsy link */}
        {habits.length > 0 && (
          <div className="autopsy-banner">
            <div className="autopsy-banner-text">
              📊 Weekly pattern report
            </div>
            <a href="/autopsy">View autopsy →</a>
          </div>
        )}

      </main>
    </div>
  );
}

import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { habitApi, checkinApi } from '../services/api';
import CheckInForm from '../components/CheckInForm';
import HabitList from '../components/HabitList';
import HabitForm from '../components/HabitForm';
import NudgeCard from "../components/Nudgecard";
import Navbar from '../components/Navbar';


export default function Dashboard() {
  const { user, logout } = useAuth();
  const [habits, setHabits] = useState([]);
  const [latestCheckin, setLatestCheckin] = useState(null);
  const [showCheckin, setShowCheckin] = useState(false);
  const [showHabitForm, setShowHabitForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [nudge, setNudge] = useState(null);

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
    // Backend returns date as array [year, month, day, ...]
    const c = latestCheckin.checkInDate || latestCheckin.date;
    if (!c) return false;
    if (Array.isArray(c)) {
      const today = new Date();
      return c[0] === today.getFullYear() &&
             c[1] === today.getMonth() + 1 &&
             c[2] === today.getDate();
    }
    return new Date(c).toDateString() === new Date().toDateString();
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
    <>
      <Navbar />
      <div className="dashboard">
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
               <CheckInForm onSuccess={(nudgeMessage) => { setShowCheckin(false); fetchData(); setNudge(nudgeMessage); }} />
             ) : (
               <button className="btn-checkin" onClick={() => setShowCheckin(true)}>
                 ✨ Check in for today
               </button>
             )}
           </div>
         )}

            {/* Nudge card */}
            {nudge && <NudgeCard nudge={nudge} onDismiss={() => setNudge(null)} />}

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
    </>
  );
}

import { useState } from 'react';
import { checkinApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

// Energy level labels — matches your backend enum/scaling logic
const ENERGY_LEVELS = [
  { score: 1, label: 'Exhausted',   emoji: '😴', color: '#ef4444', desc: 'Rest mode — minimal habits only' },
  { score: 2, label: 'Low',         emoji: '😔', color: '#f97316', desc: 'Light habits — be gentle today' },
  { score: 3, label: 'Moderate',    emoji: '😐', color: '#eab308', desc: 'Standard habits — steady pace' },
  { score: 4, label: 'Good',        emoji: '😊', color: '#84cc16', desc: 'Full habits — you\'ve got this' },
  { score: 5, label: 'Energised',   emoji: '🔥', color: '#22c55e', desc: 'Challenge mode — push further' },
];

export default function CheckInForm({ onSuccess }) {
  const { user } = useAuth();
  const [energy, setEnergy] = useState(3);
  const [note, setNote] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const selected = ENERGY_LEVELS[energy - 1];

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await checkinApi.create(user.id, { energyScore: energy, note });
      onSuccess?.();
    } catch (err) {
      setError(err.response?.data?.message || 'Check-in failed. Try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="checkin-card">
      <div className="checkin-header">
        <h2>How are you feeling today?</h2>
        {/* 
          🔮 FUTURE: This section will be replaced by Samsung Health auto-detection.
          The energyScore will be calculated from sleep + HRV + resting heart rate
          and pre-filled automatically. The user can still override.
        */}
        <p className="samsung-hint">
          📱 <span>Samsung Health integration coming soon</span> — your energy will be detected automatically
        </p>
      </div>

      <form onSubmit={handleSubmit}>
        {/* Energy Slider */}
        <div className="energy-display" style={{ borderColor: selected.color }}>
          <span className="energy-emoji">{selected.emoji}</span>
          <div>
            <div className="energy-label" style={{ color: selected.color }}>{selected.label}</div>
            <div className="energy-desc">{selected.desc}</div>
          </div>
          <div className="energy-score" style={{ color: selected.color }}>{energy}/5</div>
        </div>

        <div className="slider-wrap">
          <input
            type="range"
            min={1}
            max={5}
            step={1}
            value={energy}
            onChange={(e) => setEnergy(Number(e.target.value))}
            className="energy-slider"
            style={{ accentColor: selected.color }}
          />
          <div className="slider-ticks">
            {ENERGY_LEVELS.map(l => (
              <span key={l.score} className={energy === l.score ? 'tick active' : 'tick'}>{l.score}</span>
            ))}
          </div>
        </div>

        {/* Optional note */}
        <div className="field">
          <label>Note <span className="optional">(optional)</span></label>
          <textarea
            placeholder="Anything affecting your energy today? (poor sleep, stress, illness…)"
            value={note}
            onChange={(e) => setNote(e.target.value)}
            rows={2}
          />
        </div>

        {error && <div className="error-banner">{error}</div>}

        <button type="submit" className="btn-primary" disabled={loading}>
          {loading ? 'Saving…' : `Check in — ${selected.label}`}
        </button>
      </form>
    </div>
  );
}

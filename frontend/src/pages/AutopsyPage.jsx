import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { autopsyApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

const ENERGY_COLORS = {
  1: '#ef4444', 2: '#f97316', 3: '#eab308', 4: '#84cc16', 5: '#22c55e'
};
const ENERGY_EMOJIS = {
  1: '😴', 2: '😔', 3: '😐', 4: '😊', 5: '🔥'
};

export default function AutopsyPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [autopsy, setAutopsy] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchAutopsy = async () => {
      try {
        const res = await autopsyApi.get(user.id);
        setAutopsy(res.data);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load autopsy report.');
      } finally {
        setLoading(false);
      }
    };
    fetchAutopsy();
  }, [user.id]);

  if (loading) return (
    <div className="loading-screen">
      <div className="loader">🔥</div>
      <p style={{ color: 'var(--text2)', marginTop: 12 }}>Analysing your week...</p>
    </div>
  );

  return (
    <div className="dashboard">

      {/* Header */}
      <header className="dash-header">
        <div className="dash-brand">🔥 ember</div>
        <div className="dash-user">
          <button className="btn-logout" onClick={() => navigate('/dashboard')}>
            ← Back
          </button>
        </div>
      </header>

      <main className="dash-main">

        <div className="autopsy-page">

          {/* Title */}
          <div className="autopsy-title-section">
            <h1>Weekly Autopsy 📊</h1>
            <p className="autopsy-subtitle">
              {autopsy?.weekStart && autopsy?.weekEnd
                ? `${formatDate(autopsy.weekStart)} — ${formatDate(autopsy.weekEnd)}`
                : 'Last 7 days'}
            </p>
            {autopsy?.weekSummary && (
              <div className="week-summary-badge">{autopsy.weekSummary}</div>
            )}
          </div>

          {error && <div className="error-banner">{error}</div>}

          {autopsy && (
            <>
              {/* Stats row */}
              <div className="autopsy-stats">
                <div className="stat-card">
                  <div className="stat-value" style={{ color: getEnergyColor(autopsy.avgEnergyScore) }}>
                    {autopsy.avgEnergyScore?.toFixed(1)}
                    <span className="stat-unit">/5</span>
                  </div>
                  <div className="stat-label">Avg Energy</div>
                </div>
                <div className="stat-card">
                  <div className="stat-value" style={{ color: getConsistencyColor(autopsy.consistencyScore) }}>
                    {autopsy.consistencyScore}
                    <span className="stat-unit">%</span>
                  </div>
                  <div className="stat-label">Consistency</div>
                </div>
                <div className="stat-card">
                  <div className="stat-value" style={{ color: '#22c55e' }}>
                    {autopsy.habitCompletionRate}
                    <span className="stat-unit">%</span>
                  </div>
                  <div className="stat-label">Habits Done</div>
                </div>
                <div className="stat-card">
                  <div className="stat-value">{autopsy.totalCheckIns}
                    <span className="stat-unit">/7</span>
                  </div>
                  <div className="stat-label">Days Tracked</div>
                </div>
              </div>

              {/* Energy chart */}
              <div className="autopsy-card">
                <h3>Energy This Week</h3>
                <div className="energy-chart">
                  {autopsy.energyByDay?.map((day) => (
                    <div key={day.date} className="chart-bar-wrap">
                      <div className="chart-bar-container">
                        {day.checkedIn ? (
                          <div
                            className="chart-bar"
                            style={{
                              height: `${(day.energyScore / 5) * 100}%`,
                              background: ENERGY_COLORS[day.energyScore],
                            }}
                          >
                            <span className="bar-emoji">
                              {ENERGY_EMOJIS[day.energyScore]}
                            </span>
                          </div>
                        ) : (
                          <div className="chart-bar missed">
                            <span className="bar-emoji">—</span>
                          </div>
                        )}
                      </div>
                      <div className="chart-day">
                        {day.dayName?.slice(0, 3)}
                      </div>
                      <div className="chart-score" style={{
                        color: day.checkedIn ? ENERGY_COLORS[day.energyScore] : 'var(--text3)'
                      }}>
                        {day.checkedIn ? day.energyScore : '·'}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Best / Worst */}
              <div className="autopsy-row">
                <div className="autopsy-card half">
                  <div className="bw-label">Best day</div>
                  <div className="bw-day" style={{ color: '#22c55e' }}>
                    🏆 {autopsy.bestDay}
                  </div>
                  <div className="bw-sub">{autopsy.highEnergyDays} high energy days</div>
                </div>
                <div className="autopsy-card half">
                  <div className="bw-label">Toughest day</div>
                  <div className="bw-day" style={{ color: '#ef4444' }}>
                    😓 {autopsy.worstDay}
                  </div>
                  <div className="bw-sub">{autopsy.lowEnergyDays} low energy days</div>
                </div>
              </div>

              {/* Patterns */}
              {autopsy.patterns?.length > 0 && (
                <div className="autopsy-card">
                  <h3>Patterns Detected</h3>
                  <div className="patterns-list">
                    {autopsy.patterns.map((pattern, i) => (
                      <div key={i} className="pattern-item">
                        {pattern}
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Correlations */}
              <div className="autopsy-card">
                <h3>Biometric Insights</h3>
                <div className="correlation-item">
                  <span className="corr-icon">💤</span>
                  <span>{autopsy.sleepCorrelation || 'No sleep data tracked this week'}</span>
                </div>
                <div className="correlation-item">
                  <span className="corr-icon">💓</span>
                  <span>{autopsy.hrvCorrelation || 'HRV not tracked — connect Samsung Health for deeper insights'}</span>
                </div>
              </div>

              {/* AI Insight */}
              {autopsy.aiInsight && (
                <div className="autopsy-card ai-card">
                  <h3>🤖 AI Insight</h3>
                  <p className="ai-insight-text">{autopsy.aiInsight}</p>
                </div>
              )}

              {/* Habit performance */}
              <div className="autopsy-card">
                <h3>Habit Performance</h3>
                <div className="habit-perf-row">
                  <div className="perf-item">
                    <div className="perf-val" style={{ color: '#22c55e' }}>
                      {autopsy.totalHabitsDone}
                    </div>
                    <div className="perf-label">Completed</div>
                  </div>
                  <div className="perf-item">
                    <div className="perf-val" style={{ color: '#f97316' }}>
                      {autopsy.totalHabitsSkipped}
                    </div>
                    <div className="perf-label">Skipped</div>
                  </div>
                  <div className="perf-item">
                    <div className="perf-val">{autopsy.totalHabitsAssigned}</div>
                    <div className="perf-label">Total</div>
                  </div>
                </div>
                <div className="completion-bar-wrap">
                  <div className="completion-bar">
                    <div
                      className="completion-fill"
                      style={{ width: `${autopsy.habitCompletionRate}%` }}
                    />
                  </div>
                  <span className="completion-pct">{autopsy.habitCompletionRate}%</span>
                </div>
              </div>

            </>
          )}

          {!autopsy && !error && (
            <div className="empty-state">
              <div className="empty-icon">📊</div>
              <p>No data yet — check in daily for 7 days to see your autopsy report!</p>
            </div>
          )}

        </div>
      </main>
    </div>
  );
}

// ── Helpers ───────────────────────────────────────────
function formatDate(dateArr) {
  if (!dateArr) return '';
  // Backend returns date as array [year, month, day]
  if (Array.isArray(dateArr)) {
    return `${dateArr[2]}/${dateArr[1]}/${dateArr[0]}`;
  }
  return new Date(dateArr).toLocaleDateString();
}

function getEnergyColor(score) {
  if (!score) return 'var(--text)';
  if (score >= 4) return '#22c55e';
  if (score >= 3) return '#eab308';
  return '#ef4444';
}

function getConsistencyColor(score) {
  if (!score) return 'var(--text)';
  if (score >= 80) return '#22c55e';
  if (score >= 50) return '#eab308';
  return '#ef4444';
}

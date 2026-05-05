import { useState } from 'react';
import { habitApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import HabitForm from './HabitForm';

const STATUS_STYLES = {
  ACTIVE:   { color: '#94a3b8', label: 'Pending',  emoji: '⏳' }, // ← was PENDING, backend sends ACTIVE
  DONE:     { color: '#22c55e', label: 'Done',     emoji: '✅' },
  SKIPPED:  { color: '#f97316', label: 'Skipped',  emoji: '⏭️' },
  PAUSED:   { color: '#a78bfa', label: 'Paused',   emoji: '⏸️' },
  ARCHIVED: { color: '#64748b', label: 'Archived', emoji: '📦' },
};

export default function HabitList({ habits, energyScore, onRefresh }) {
  const { user } = useAuth();
  const [updating, setUpdating] = useState(null);
  const [editingId, setEditingId] = useState(null);

  // ✅ Fix — use fullVersion/liteVersion/minimalVersion instead of targets
  const getScaledVersion = (habit) => {
    if (!energyScore) return habit.liteVersion;
    if (energyScore <= 2) return habit.minimalVersion;
    if (energyScore >= 4) return habit.fullVersion;
    return habit.liteVersion;
  };

  // ✅ Fix — call correct endpoints for complete/skip
  const markDone = async (habitId) => {
    setUpdating(habitId);
    try {
      await habitApi.complete(user.id, habitId);
      onRefresh?.();
    } catch (err) {
      console.error('Failed to complete habit:', err);
    } finally {
      setUpdating(null);
    }
  };

  const markSkip = async (habitId) => {
    setUpdating(habitId);
    try {
      await habitApi.skip(user.id, habitId);
      onRefresh?.();
    } catch (err) {
      console.error('Failed to skip habit:', err);
    } finally {
      setUpdating(null);
    }
  };

  const markReset = async (habitId) => {
    setUpdating(habitId);
    try {
      await habitApi.reset(user.id, habitId);
      onRefresh?.();
    } catch (err) {
      console.error('Failed to reset habit:', err);
    } finally {
      setUpdating(null);
    }
  };

  if (!habits?.length) {
    return (
      <div className="empty-state">
        <div className="empty-icon">🌱</div>
        <p>No habits yet. Add your first habit to get started!</p>
      </div>
    );
  }

  return (
    <div className="habit-list">
      {habits.map((habit) => {
        const scaledVersion = getScaledVersion(habit);
        const statusStyle = STATUS_STYLES[habit.status] || STATUS_STYLES.ACTIVE;

        return (
          <div key={habit.id} className={`habit-card ${habit.status?.toLowerCase()}`}>
            <div className="habit-top">
              <div className="habit-name">{habit.name}</div>
              <div className="habit-status" style={{ color: statusStyle.color }}>
                {statusStyle.emoji} {statusStyle.label}
              </div>
            </div>

            {/* ✅ Fix — show version text instead of numeric targets */}
            <div className="habit-target">
              <span className="target-val">
                {scaledVersion || 'No version set'}
              </span>
            </div>

            {/* ✅ Fix — show all 3 versions */}
            <div className="habit-targets-row">
              <span>😴 {habit.minimalVersion}</span>
              <span>⚡ {habit.liteVersion}</span>
              <span>🔥 {habit.fullVersion}</span>
            </div>

            {/* Show streak if > 0 */}
            {habit.streakCount > 0 && (
              <div className="habit-streak">🔥 {habit.streakCount} day streak</div>
            )}

            {/* Action buttons based on status */}
            {habit.status === 'ACTIVE' && (
              <div className="habit-actions">
                <button
                  className="btn-done"
                  onClick={() => markDone(habit.id)}
                  disabled={updating === habit.id}
                >
                  ✓ Done
                </button>
                <button
                  className="btn-skip"
                  onClick={() => markSkip(habit.id)}
                  disabled={updating === habit.id}
                >
                  Skip
                </button>
                <button
                  className="btn-secondary"
                  onClick={() => setEditingId(habit.id)}
                >
                  ✏️ Edit
                </button>
              </div>
            )}

            {habit.status === 'DONE' && (
              <div className="habit-actions">
                <button
                  className="btn-secondary"
                  onClick={() => markReset(habit.id)}
                  disabled={updating === habit.id}
                >
                  ↩️ Undo
                </button>
                <button
                  className="btn-secondary"
                  onClick={() => setEditingId(habit.id)}
                >
                  ✏️ Edit
                </button>
              </div>
            )}

            {habit.status === 'SKIPPED' && (
              <div className="habit-actions">
                <button
                  className="btn-secondary"
                  onClick={() => markReset(habit.id)}
                  disabled={updating === habit.id}
                >
                  ↩️ Undo skip
                </button>
                <button
                  className="btn-secondary"
                  onClick={() => setEditingId(habit.id)}
                >
                  ✏️ Edit
                </button>
              </div>
            )}
          </div>
        );
      })}

      {/* Edit Modal */}
      {editingId && (
        <div className="modal-overlay" onClick={() => setEditingId(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Edit Habit</h2>
              <button className="btn-close" onClick={() => setEditingId(null)}>✕</button>
            </div>
            <HabitForm
              habit={habits.find((h) => h.id === editingId)}
              onSuccess={() => {
                setEditingId(null);
                onRefresh?.();
              }}
              onCancel={() => setEditingId(null)}
            />
          </div>
        </div>
      )}
    </div>
  );
}
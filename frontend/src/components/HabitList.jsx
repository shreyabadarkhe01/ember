import { useState } from 'react';
import { habitApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import HabitForm from './HabitForm';

const STATUS_STYLES = {
  ACTIVE:   { color: '#94a3b8', label: 'Pending',  emoji: '⏳' },
  DONE:     { color: '#22c55e', label: 'Done',     emoji: '✅' },
  SKIPPED:  { color: '#f97316', label: 'Skipped',  emoji: '⏭️' },
  PAUSED:   { color: '#a78bfa', label: 'Paused',   emoji: '⏸️' },
  ARCHIVED: { color: '#64748b', label: 'Archived', emoji: '📦' },
};

export default function HabitList({ habits, energyScore, onRefresh }) {
  const { user } = useAuth();
  const [updating, setUpdating]           = useState(null);
  const [editingId, setEditingId]         = useState(null);
  const [confirmArchiveId, setConfirmArchiveId] = useState(null); // ← which habit is pending archive confirm

  const getScaledVersion = (habit) => {
    if (!energyScore) return habit.liteVersion;
    if (energyScore <= 2) return habit.minimalVersion;
    if (energyScore >= 4) return habit.fullVersion;
    return habit.liteVersion;
  };

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

  const archiveHabit = async (habitId) => {
    setUpdating(habitId);
    try {
      await habitApi.archive(user.id, habitId);
      setConfirmArchiveId(null);
      onRefresh?.();
    } catch (err) {
      console.error('Failed to archive habit:', err);
    } finally {
      setUpdating(null);
    }
  };

  // Active habits first, then done/skipped, archived always last
  const sorted = [...(habits ?? [])].sort((a, b) => {
    const order = { ACTIVE: 0, DONE: 1, SKIPPED: 2, PAUSED: 3, ARCHIVED: 4 };
    return (order[a.status] ?? 9) - (order[b.status] ?? 9);
  });

  if (!sorted.length) {
    return (
      <div className="empty-state">
        <div className="empty-icon">🌱</div>
        <p>No habits yet. Add your first habit to get started!</p>
      </div>
    );
  }

  return (
    <div className="habit-list">
      {sorted.map((habit) => {
        const scaledVersion = getScaledVersion(habit);
        const statusStyle   = STATUS_STYLES[habit.status] || STATUS_STYLES.ACTIVE;
        const isArchived    = habit.status === 'ARCHIVED';
        const isPendingArchive = confirmArchiveId === habit.id;

        return (
          <div
            key={habit.id}
            className={`habit-card ${habit.status?.toLowerCase()}`}
            style={isArchived ? { opacity: 0.5 } : undefined}
          >
            <div className="habit-top">
              <div className="habit-name">{habit.name}</div>
              <div className="habit-status" style={{ color: statusStyle.color }}>
                {statusStyle.emoji} {statusStyle.label}
              </div>
            </div>

            {!isArchived && (
              <>
                <div className="habit-target">
                  <span className="target-val">
                    {scaledVersion || 'No version set'}
                  </span>
                </div>

                <div className="habit-targets-row">
                  <span>😴 {habit.minimalVersion}</span>
                  <span>⚡ {habit.liteVersion}</span>
                  <span>🔥 {habit.fullVersion}</span>
                </div>
              </>
            )}

            {habit.streakCount > 0 && !isArchived && (
              <div className="habit-streak">🔥 {habit.streakCount} day streak</div>
            )}

            {/* ── Inline archive confirmation ── */}
            {isPendingArchive && (
              <div className="archive-confirm">
                <span>Archive this habit? It won't appear in your daily list.</span>
                <div className="archive-confirm-actions">
                  <button
                    className="btn-archive-confirm"
                    onClick={() => archiveHabit(habit.id)}
                    disabled={updating === habit.id}
                  >
                    {updating === habit.id ? 'Archiving…' : 'Yes, archive'}
                  </button>
                  <button
                    className="btn-secondary"
                    onClick={() => setConfirmArchiveId(null)}
                  >
                    Cancel
                  </button>
                </div>
              </div>
            )}

            {/* ── Action buttons ── */}
            {!isPendingArchive && (
              <>
                {habit.status === 'ACTIVE' && (
                  <div className="habit-actions">
                    <button className="btn-done"      onClick={() => markDone(habit.id)}  disabled={updating === habit.id}>✓ Done</button>
                    <button className="btn-skip"      onClick={() => markSkip(habit.id)}  disabled={updating === habit.id}>Skip</button>
                    <button className="btn-secondary" onClick={() => setEditingId(habit.id)}>✏️ Edit</button>
                    <button className="btn-archive"   onClick={() => setConfirmArchiveId(habit.id)}>📦 Archive</button>
                  </div>
                )}

                {habit.status === 'DONE' && (
                  <div className="habit-actions">
                    <button className="btn-secondary" onClick={() => markReset(habit.id)}    disabled={updating === habit.id}>↩️ Undo</button>
                    <button className="btn-secondary" onClick={() => setEditingId(habit.id)}>✏️ Edit</button>
                    <button className="btn-archive"   onClick={() => setConfirmArchiveId(habit.id)}>📦 Archive</button>
                  </div>
                )}

                {habit.status === 'SKIPPED' && (
                  <div className="habit-actions">
                    <button className="btn-secondary" onClick={() => markReset(habit.id)}    disabled={updating === habit.id}>↩️ Undo skip</button>
                    <button className="btn-secondary" onClick={() => setEditingId(habit.id)}>✏️ Edit</button>
                    <button className="btn-archive"   onClick={() => setConfirmArchiveId(habit.id)}>📦 Archive</button>
                  </div>
                )}

                {/* Archived habits — no actions except a visual indicator */}
                {habit.status === 'ARCHIVED' && (
                  <div className="habit-actions">
                    <button
                      className="btn-secondary"
                      onClick={() => {
                        setUpdating(habit.id);
                        habitApi.unarchive(user.id, habit.id)
                          .then(() => onRefresh?.())
                          .catch(err => console.error('Failed to unarchive:', err))
                          .finally(() => setUpdating(null));
                      }}
                      disabled={updating === habit.id}
                    >
                      {updating === habit.id ? 'Restoring…' : '📤 Unarchive'}
                    </button>
                  </div>
                )}
              </>
            )}
          </div>
        );
      })}

      {/* ── Edit Modal ── */}
      {editingId && (
        <div className="modal-overlay" onClick={() => setEditingId(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Edit Habit</h2>
              <button className="btn-close" onClick={() => setEditingId(null)}>✕</button>
            </div>
            <HabitForm
              habit={habits.find((h) => h.id === editingId)}
              onSuccess={() => { setEditingId(null); onRefresh?.(); }}
              onCancel={() => setEditingId(null)}
            />
          </div>
        </div>
      )}

      <style>{`
        .archive-confirm {
          margin-top: 0.6rem;
          padding: 0.75rem;
          background: rgba(100, 116, 139, 0.1);
          border: 1px solid rgba(100, 116, 139, 0.25);
          border-radius: 8px;
          font-size: 13px;
          color: rgba(253, 220, 170, 0.7);
        }
        .archive-confirm-actions {
          display: flex;
          gap: 8px;
          margin-top: 0.5rem;
        }
        .btn-archive {
          background: transparent;
          border: 1px solid rgba(100, 116, 139, 0.35);
          color: #94a3b8;
          border-radius: 7px;
          padding: 5px 10px;
          font-size: 13px;
          cursor: pointer;
          transition: background 0.15s, border-color 0.15s;
        }
        .btn-archive:hover {
          background: rgba(100, 116, 139, 0.12);
          border-color: rgba(100, 116, 139, 0.55);
        }
        .btn-archive-confirm {
          background: rgba(100, 116, 139, 0.2);
          border: 1px solid rgba(100, 116, 139, 0.4);
          color: #cbd5e1;
          border-radius: 7px;
          padding: 5px 12px;
          font-size: 13px;
          cursor: pointer;
          transition: background 0.15s;
        }
        .btn-archive-confirm:hover {
          background: rgba(100, 116, 139, 0.35);
        }
        .archived-note {
          font-size: 12px;
          color: #64748b;
          font-style: italic;
        }
      `}</style>
    </div>
  );
}
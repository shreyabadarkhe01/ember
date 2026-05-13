import { useState } from 'react';
import { habitApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

// habit prop = existing habit object → edit mode
// habit prop = undefined/null         → create mode
export default function HabitForm({ habit, onSuccess, onCancel }) {
  const { user } = useAuth();
  const isEditing = !!habit;

  const [form, setForm] = useState({
    name:           habit?.name           ?? '',
    minimalVersion: habit?.minimalVersion ?? '',
    liteVersion:    habit?.liteVersion    ?? '',
    fullVersion:    habit?.fullVersion    ?? '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState('');

  const set = (key, val) => setForm(f => ({ ...f, [key]: val }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      if (isEditing) {
        // PATCH /api/users/{id}/habits/{habitId}
        await habitApi.update(user.id, habit.id, {
          name:           form.name,
          minimalVersion: form.minimalVersion,
          liteVersion:    form.liteVersion,
          fullVersion:    form.fullVersion,
        });
      } else {
        // POST /api/users/{id}/habits
        await habitApi.create(user.id, {
          name:           form.name,
          minimalVersion: form.minimalVersion,
          liteVersion:    form.liteVersion,
          fullVersion:    form.fullVersion,
        });
      }
      onSuccess?.();
    } catch (err) {
      setError(err.response?.data?.message || `Failed to ${isEditing ? 'update' : 'create'} habit.`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="habit-form-card">
      <h3>{isEditing ? 'Edit Habit' : 'New Habit'}</h3>
      <p className="form-hint">
        {isEditing
          ? 'Update the versions — Ember will use the new values from tomorrow'
          : 'Set 3 versions — Ember picks the right one based on your energy each day'}
      </p>

      <form onSubmit={handleSubmit}>
        <div className="field">
          <label>Habit name</label>
          <input
            type="text"
            placeholder="e.g. Morning run, Read, Meditate"
            value={form.name}
            onChange={(e) => set('name', e.target.value)}
            required
          />
        </div>

        <div className="targets-row">
          <div className="field target-field light">
            <label>😴 Minimal <span className="target-hint">(low energy day)</span></label>
            <input
              type="text"
              placeholder="e.g. Walk 10 mins"
              value={form.minimalVersion}
              onChange={(e) => set('minimalVersion', e.target.value)}
              required
            />
          </div>
          <div className="field target-field standard">
            <label>⚡ Lite <span className="target-hint">(normal day)</span></label>
            <input
              type="text"
              placeholder="e.g. Walk 20 mins"
              value={form.liteVersion}
              onChange={(e) => set('liteVersion', e.target.value)}
              required
            />
          </div>
          <div className="field target-field challenge">
            <label>🔥 Full <span className="target-hint">(high energy day)</span></label>
            <input
              type="text"
              placeholder="e.g. Walk 30 mins"
              value={form.fullVersion}
              onChange={(e) => set('fullVersion', e.target.value)}
              required
            />
          </div>
        </div>

        {error && <div className="error-banner">{error}</div>}

        <div className="form-actions">
          <button type="button" className="btn-secondary" onClick={onCancel}>Cancel</button>
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Saving…' : isEditing ? 'Save changes' : 'Add habit'}
          </button>
        </div>
      </form>
    </div>
  );
}
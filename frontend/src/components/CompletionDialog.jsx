import { useState } from 'react';
import { habitApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

const COMPLETION_OPTIONS = [
  { label: 'Partial',     emoji: '🌗', value: 0.5,  desc: 'Got through some of it' },
  { label: '~75%',        emoji: '⚡', value: 0.75, desc: 'Most of it done' },
  { label: 'Full',        emoji: '✅', value: 1.0,  desc: 'Completed as planned' },
  { label: 'Went beyond', emoji: '🔥', value: 1.25, desc: 'Exceeded the version' },
];

const FEELING_OPTIONS = [
  { label: 'Drained',    emoji: '😤', value: 'DRAINED',   color: '#ef4444' },
  { label: 'Just right', emoji: '⚡', value: 'NEUTRAL',   color: '#eab308' },
  { label: 'Energised',  emoji: '🔥', value: 'ENERGISED', color: '#22c55e' },
];

/**
 * CompletionDialog
 * -----------------
 * Props:
 *   habitId    {number}  — habit that was just marked done
 *   habitName  {string}  — shown in dialog title
 *   onClose    {fn}      — called after submit or skip
 */
export default function CompletionDialog({ habitId, habitName, onClose }) {
  const { user } = useAuth();
  const [completion, setCompletion] = useState(null);
  const [feeling, setFeeling]       = useState(null);
  const [saving, setSaving]         = useState(false);

  async function handleSubmit() {
    if (!completion || !feeling) return;
    setSaving(true);
    try {
      await habitApi.logCompletion(user.id, habitId, {
        completionRatio: completion.value,
        feelingTag: feeling.value,
      });
    } catch (err) {
      console.warn('Completion log failed (non-critical):', err);
    } finally {
      setSaving(false);
      onClose();
    }
  }

  return (
    <>
      <div className="cd-overlay" onClick={onClose} />
      <div className="cd-dialog" role="dialog" aria-modal="true">

        <div className="cd-header">
          <span className="cd-title">✓ {habitName}</span>
          <button className="cd-close" onClick={onClose} aria-label="Skip">✕</button>
        </div>

        <p className="cd-sub">Quick check — takes 5 seconds</p>

        {/* Completion amount */}
        <div className="cd-section">
          <div className="cd-label">How much did you complete?</div>
          <div className="cd-options">
            {COMPLETION_OPTIONS.map(opt => (
              <button
                key={opt.value}
                className={`cd-option ${completion?.value === opt.value ? 'cd-option--selected' : ''}`}
                onClick={() => setCompletion(opt)}
              >
                <span className="cd-opt-emoji">{opt.emoji}</span>
                <span className="cd-opt-label">{opt.label}</span>
                <span className="cd-opt-desc">{opt.desc}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Feeling */}
        <div className="cd-section">
          <div className="cd-label">How did it feel?</div>
          <div className="cd-feelings">
            {FEELING_OPTIONS.map(opt => (
              <button
                key={opt.value}
                className={`cd-feeling ${feeling?.value === opt.value ? 'cd-feeling--selected' : ''}`}
                style={feeling?.value === opt.value ? { borderColor: opt.color, background: `${opt.color}15` } : {}}
                onClick={() => setFeeling(opt)}
              >
                <span className="cd-feel-emoji">{opt.emoji}</span>
                <span className="cd-feel-label" style={feeling?.value === opt.value ? { color: opt.color } : {}}>
                  {opt.label}
                </span>
              </button>
            ))}
          </div>
        </div>

        {/* Actions */}
        <div className="cd-actions">
          <button className="cd-skip" onClick={onClose}>Skip</button>
          <button
            className="cd-submit"
            onClick={handleSubmit}
            disabled={!completion || !feeling || saving}
          >
            {saving ? 'Saving…' : 'Log it →'}
          </button>
        </div>

      </div>

      <style>{`
        .cd-overlay {
          position: fixed;
          inset: 0;
          background: rgba(0,0,0,0.55);
          z-index: 200;
          animation: cdFadeIn 0.2s ease;
        }
        .cd-dialog {
          position: fixed;
          bottom: 0;
          left: 0; right: 0;
          background: #1a1510;
          border-top: 1px solid rgba(255,140,30,0.2);
          border-radius: 20px 20px 0 0;
          padding: 1.5rem 1.25rem 2rem;
          z-index: 201;
          animation: cdSlideUp 0.28s cubic-bezier(0.22,1,0.36,1);
          max-width: 560px;
          margin: 0 auto;
        }
        @keyframes cdFadeIn  { from { opacity:0 } to { opacity:1 } }
        @keyframes cdSlideUp { from { transform: translateY(100%) } to { transform: translateY(0) } }

        .cd-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 0.25rem;
        }
        .cd-title {
          font-size: 15px;
          font-weight: 600;
          color: #22c55e;
        }
        .cd-close {
          background: none;
          border: none;
          color: rgba(253,220,170,0.35);
          font-size: 16px;
          cursor: pointer;
          padding: 4px 6px;
          border-radius: 6px;
          transition: color 0.15s;
        }
        .cd-close:hover { color: rgba(253,220,170,0.7); }
        .cd-sub {
          font-size: 12px;
          color: rgba(253,220,170,0.35);
          margin-bottom: 1.25rem;
        }
        .cd-section { margin-bottom: 1.25rem; }
        .cd-label {
          font-size: 13px;
          font-weight: 500;
          color: rgba(253,220,170,0.7);
          margin-bottom: 0.6rem;
        }

        /* Completion options — vertical stack */
        .cd-options {
          display: flex;
          flex-direction: column;
          gap: 6px;
        }
        .cd-option {
          display: flex;
          align-items: center;
          gap: 10px;
          background: rgba(255,255,255,0.03);
          border: 1px solid rgba(255,140,30,0.12);
          border-radius: 10px;
          padding: 10px 12px;
          cursor: pointer;
          text-align: left;
          transition: border-color 0.15s, background 0.15s;
          width: 100%;
        }
        .cd-option:hover {
          border-color: rgba(255,140,30,0.3);
          background: rgba(255,140,30,0.06);
        }
        .cd-option--selected {
          border-color: rgba(255,140,30,0.55) !important;
          background: rgba(255,140,30,0.1) !important;
        }
        .cd-opt-emoji { font-size: 18px; min-width: 24px; }
        .cd-opt-label {
          font-size: 13px;
          font-weight: 600;
          color: #fdf0e0;
          min-width: 90px;
        }
        .cd-opt-desc {
          font-size: 12px;
          color: rgba(253,220,170,0.4);
        }

        /* Feeling options — horizontal row */
        .cd-feelings {
          display: flex;
          gap: 8px;
        }
        .cd-feeling {
          flex: 1;
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 5px;
          background: rgba(255,255,255,0.03);
          border: 1px solid rgba(255,140,30,0.12);
          border-radius: 10px;
          padding: 12px 8px;
          cursor: pointer;
          transition: border-color 0.15s, background 0.15s;
        }
        .cd-feeling:hover {
          border-color: rgba(255,140,30,0.3);
          background: rgba(255,140,30,0.06);
        }
        .cd-feel-emoji { font-size: 22px; }
        .cd-feel-label {
          font-size: 12px;
          font-weight: 500;
          color: rgba(253,220,170,0.6);
          transition: color 0.15s;
        }

        /* Actions */
        .cd-actions {
          display: flex;
          gap: 10px;
          margin-top: 0.5rem;
        }
        .cd-skip {
          background: none;
          border: 1px solid rgba(255,140,30,0.15);
          color: rgba(253,220,170,0.4);
          border-radius: 8px;
          padding: 10px 18px;
          font-size: 13px;
          cursor: pointer;
          transition: all 0.15s;
        }
        .cd-skip:hover {
          border-color: rgba(255,140,30,0.35);
          color: rgba(253,220,170,0.7);
        }
        .cd-submit {
          flex: 1;
          background: #ff8c1e;
          border: none;
          border-radius: 8px;
          color: #0f0d0a;
          font-size: 14px;
          font-weight: 600;
          padding: 10px;
          cursor: pointer;
          transition: background 0.15s, opacity 0.15s;
        }
        .cd-submit:hover:not(:disabled) { background: #ffa040; }
        .cd-submit:disabled { opacity: 0.4; cursor: not-allowed; }
      `}</style>
    </>
  );
}
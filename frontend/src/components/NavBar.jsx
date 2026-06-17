import { useState, useRef, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout, deleteAccount } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const menuRef = useRef(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(e) {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setMenuOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  function handleLogout() {
    setMenuOpen(false);
    logout();
    navigate('/login');
  }

  async function handleDeleteAccount() {
    setMenuOpen(false);
    const confirmed = window.confirm(
      'This will permanently delete your account, all habits, check-ins, and history. This cannot be undone.'
    );
    if (!confirmed) return;

    setDeleting(true);
    try {
      await deleteAccount();
      navigate('/login');
    } catch (err) {
      alert(err.message || 'Failed to delete account. Try again.');
    } finally {
      setDeleting(false);
    }
  }

  return (
    <>
      <nav className="ember-nav">
        <Link to="/about" className="ember-nav__brand">
          <span className="ember-nav__flame" aria-hidden="true">🔥</span>
          <span className="ember-nav__name">Ember</span>
        </Link>

        <div className="ember-nav__right">
          {user && (
            <>
              <Link to="/dashboard" className="ember-nav__link">Dashboard</Link>
              <Link to="/autopsy" className="ember-nav__link">Autopsy</Link>
              <span className="ember-nav__divider" aria-hidden="true" />

              {/* User dropdown */}
              <div className="ember-nav__menu-wrap" ref={menuRef}>
                <button
                  className="ember-nav__user-btn"
                  onClick={() => setMenuOpen(!menuOpen)}
                  aria-expanded={menuOpen}
                >
                  Hi, {user.name?.split(' ')[0]}
                  <span className="ember-nav__chevron">{menuOpen ? '▴' : '▾'}</span>
                </button>

                {menuOpen && (
                  <div className="ember-nav__dropdown">
                    <button className="ember-nav__dd-item" onClick={handleLogout}>
                      Sign out
                    </button>
                    <div className="ember-nav__dd-divider" />
                    <button
                      className="ember-nav__dd-item ember-nav__dd-danger"
                      onClick={handleDeleteAccount}
                      disabled={deleting}
                    >
                      {deleting ? 'Deleting…' : 'Delete account'}
                    </button>
                  </div>
                )}
              </div>
            </>
          )}
        </div>
      </nav>

      <style>{`
        .ember-nav {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 0 1.5rem;
          height: 56px;
          background: rgba(15, 13, 10, 0.85);
          backdrop-filter: blur(12px);
          border-bottom: 1px solid rgba(255, 140, 30, 0.1);
          position: sticky;
          top: 0;
          z-index: 100;
        }
        .ember-nav__brand {
          display: flex;
          align-items: center;
          gap: 7px;
          text-decoration: none;
          transition: opacity 0.15s;
        }
        .ember-nav__brand:hover { opacity: 0.8; }
        .ember-nav__flame {
          font-size: 18px;
          animation: navFlicker 2.8s ease-in-out infinite;
          display: inline-block;
        }
        @keyframes navFlicker {
          0%, 100% { transform: scaleY(1) rotate(-1deg); }
          40%       { transform: scaleY(1.1) rotate(1deg); }
          70%       { transform: scaleY(0.95); }
        }
        .ember-nav__name {
          font-family: 'Sora', 'Segoe UI', sans-serif;
          font-size: 17px;
          font-weight: 600;
          color: #ff8c1e;
          letter-spacing: 0.02em;
        }
        .ember-nav__right {
          display: flex;
          align-items: center;
          gap: 4px;
        }
        .ember-nav__link {
          text-decoration: none;
          color: rgba(253, 220, 170, 0.55);
          font-size: 13px;
          font-weight: 500;
          padding: 5px 10px;
          border-radius: 6px;
          transition: color 0.15s, background 0.15s;
        }
        .ember-nav__link:hover {
          color: rgba(253, 220, 170, 0.9);
          background: rgba(255, 140, 30, 0.08);
        }
        .ember-nav__divider {
          width: 1px;
          height: 16px;
          background: rgba(255, 140, 30, 0.15);
          margin: 0 6px;
        }

        /* User dropdown trigger */
        .ember-nav__menu-wrap {
          position: relative;
        }
        .ember-nav__user-btn {
          display: flex;
          align-items: center;
          gap: 5px;
          background: none;
          border: 1px solid rgba(255, 140, 30, 0.2);
          color: rgba(253, 220, 170, 0.6);
          font-size: 13px;
          padding: 4px 10px;
          border-radius: 6px;
          cursor: pointer;
          margin-left: 6px;
          transition: border-color 0.15s, color 0.15s, background 0.15s;
        }
        .ember-nav__user-btn:hover {
          border-color: rgba(255, 140, 30, 0.45);
          color: #ff8c1e;
          background: rgba(255, 140, 30, 0.07);
        }
        .ember-nav__chevron {
          font-size: 9px;
          opacity: 0.6;
        }

        /* Dropdown menu */
        .ember-nav__dropdown {
          position: absolute;
          top: calc(100% + 8px);
          right: 0;
          min-width: 160px;
          background: #1a1714;
          border: 1px solid rgba(255, 140, 30, 0.15);
          border-radius: 8px;
          padding: 4px;
          box-shadow: 0 8px 24px rgba(0,0,0,0.4);
          z-index: 200;
        }
        .ember-nav__dd-item {
          display: block;
          width: 100%;
          text-align: left;
          background: none;
          border: none;
          color: rgba(253, 220, 170, 0.7);
          font-size: 13px;
          padding: 8px 12px;
          border-radius: 6px;
          cursor: pointer;
          transition: background 0.12s, color 0.12s;
        }
        .ember-nav__dd-item:hover {
          background: rgba(255, 140, 30, 0.08);
          color: rgba(253, 220, 170, 0.95);
        }
        .ember-nav__dd-divider {
          height: 1px;
          background: rgba(255, 140, 30, 0.1);
          margin: 4px 0;
        }
        .ember-nav__dd-danger {
          color: rgba(255, 80, 80, 0.7);
        }
        .ember-nav__dd-danger:hover {
          background: rgba(255, 60, 60, 0.08);
          color: #ff5555;
        }
        .ember-nav__dd-danger:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }
      `}</style>
    </>
  );
}
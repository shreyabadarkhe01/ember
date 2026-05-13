import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
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
              <Link to="/autopsy"   className="ember-nav__link">Autopsy</Link>
              <span className="ember-nav__divider" aria-hidden="true" />
              <span className="ember-nav__user">Hi, {user.name?.split(' ')[0]}</span>
              <button className="ember-nav__logout" onClick={handleLogout}>
                Sign out
              </button>
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
        .ember-nav__user {
          font-size: 13px;
          color: rgba(253, 220, 170, 0.4);
        }
        .ember-nav__logout {
          background: none;
          border: 1px solid rgba(255, 140, 30, 0.2);
          color: rgba(253, 220, 170, 0.5);
          font-size: 12px;
          padding: 4px 10px;
          border-radius: 6px;
          cursor: pointer;
          margin-left: 6px;
          transition: border-color 0.15s, color 0.15s, background 0.15s;
        }
        .ember-nav__logout:hover {
          border-color: rgba(255, 140, 30, 0.45);
          color: #ff8c1e;
          background: rgba(255, 140, 30, 0.07);
        }
      `}</style>
    </>
  );
}
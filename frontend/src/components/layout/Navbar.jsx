import React, { useState, useRef, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useNotification } from '../../context/NotificationContext';
import { notificationApi } from '../../services/api';
import { 
  Zap, 
  Briefcase, 
  User, 
  FileText, 
  Bookmark, 
  Bell, 
  LogOut, 
  LogIn, 
  UserPlus, 
  LayoutDashboard,
  CheckCheck
} from 'lucide-react';

export const Navbar = () => {
  const { user, isAuthenticated, isCandidate, isRecruiter, logout } = useAuth();
  const { notifications, unreadCount, markAllAsRead } = useNotification();
  const [showNotifications, setShowNotifications] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const notifRef = useRef(null);
  const userMenuRef = useRef(null);
  const navigate = useNavigate();
  const location = useLocation();

  // Close dropdowns on outside click
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (notifRef.current && !notifRef.current.contains(e.target)) {
        setShowNotifications(false);
      }
      if (userMenuRef.current && !userMenuRef.current.contains(e.target)) {
        setShowUserMenu(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const isActive = (path) => location.pathname === path;

  return (
    <header style={{
      position: 'sticky',
      top: 0,
      zIndex: 50,
      background: 'rgba(9, 13, 22, 0.85)',
      backdropFilter: 'blur(16px)',
      borderBottom: '1px solid var(--border-subtle)',
    }}>
      <div className="container" style={{
        height: '70px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
      }}>
        {/* Logo */}
        <Link to="/" style={{
          display: 'flex',
          alignItems: 'center',
          gap: '10px',
          textDecoration: 'none',
        }}>
          <div style={{
            width: '38px',
            height: '38px',
            borderRadius: '10px',
            background: 'var(--primary-gradient)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: 'var(--shadow-glow)',
          }}>
            <Zap size={22} color="#ffffff" />
          </div>
          <span style={{
            fontFamily: 'var(--font-display)',
            fontSize: '1.35rem',
            fontWeight: 800,
            letterSpacing: '-0.02em',
            color: '#ffffff',
          }}>
            Career<span className="text-gradient">Forge</span>
          </span>
        </Link>

        {/* Center Navigation Links */}
        <nav style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Link
            to="/jobs"
            style={{
              padding: '8px 14px',
              borderRadius: '8px',
              textDecoration: 'none',
              fontSize: '0.875rem',
              fontWeight: 500,
              color: isActive('/jobs') ? '#3b82f6' : 'var(--text-secondary)',
              background: isActive('/jobs') ? 'rgba(59, 130, 246, 0.1)' : 'transparent',
              transition: 'all 0.2s',
            }}
          >
            Explore Jobs
          </Link>

          {isAuthenticated && isCandidate && (
            <>
              <Link
                to="/applications"
                style={{
                  padding: '8px 14px',
                  borderRadius: '8px',
                  textDecoration: 'none',
                  fontSize: '0.875rem',
                  fontWeight: 500,
                  color: isActive('/applications') ? '#3b82f6' : 'var(--text-secondary)',
                  background: isActive('/applications') ? 'rgba(59, 130, 246, 0.1)' : 'transparent',
                  transition: 'all 0.2s',
                }}
              >
                Applications
              </Link>
              <Link
                to="/profile"
                style={{
                  padding: '8px 14px',
                  borderRadius: '8px',
                  textDecoration: 'none',
                  fontSize: '0.875rem',
                  fontWeight: 500,
                  color: isActive('/profile') ? '#3b82f6' : 'var(--text-secondary)',
                  background: isActive('/profile') ? 'rgba(59, 130, 246, 0.1)' : 'transparent',
                  transition: 'all 0.2s',
                }}
              >
                Profile & Resume AI
              </Link>
            </>
          )}

          {isAuthenticated && isRecruiter && (
            <>
              <Link
                to="/recruiter"
                style={{
                  padding: '8px 14px',
                  borderRadius: '8px',
                  textDecoration: 'none',
                  fontSize: '0.875rem',
                  fontWeight: 500,
                  color: isActive('/recruiter') ? '#3b82f6' : 'var(--text-secondary)',
                  background: isActive('/recruiter') ? 'rgba(59, 130, 246, 0.1)' : 'transparent',
                  transition: 'all 0.2s',
                }}
              >
                Recruiter Hub
              </Link>
            </>
          )}
        </nav>

        {/* Right Controls */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
          {isAuthenticated ? (
            <>
              {/* Notification Bell Dropdown */}
              <div ref={notifRef} style={{ position: 'relative' }}>
                <button
                  onClick={() => setShowNotifications(!showNotifications)}
                  style={{
                    position: 'relative',
                    background: 'rgba(255, 255, 255, 0.05)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: '10px',
                    width: '40px',
                    height: '40px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    cursor: 'pointer',
                    color: unreadCount > 0 ? '#3b82f6' : 'var(--text-secondary)',
                  }}
                  title="Notifications"
                >
                  <Bell size={18} />
                  {unreadCount > 0 && (
                    <span style={{
                      position: 'absolute',
                      top: '-4px',
                      right: '-4px',
                      background: 'var(--accent-rose)',
                      color: 'white',
                      fontSize: '0.65rem',
                      fontWeight: 700,
                      borderRadius: '9999px',
                      minWidth: '18px',
                      height: '18px',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      padding: '0 4px',
                      boxShadow: '0 0 10px rgba(244, 63, 94, 0.6)',
                    }}>
                      {unreadCount}
                    </span>
                  )}
                </button>

                {/* Notifications Panel */}
                {showNotifications && (
                  <div
                    className="glass-panel"
                    style={{
                      position: 'absolute',
                      right: 0,
                      top: '50px',
                      width: '340px',
                      maxHeight: '400px',
                      overflowY: 'auto',
                      padding: '16px',
                      zIndex: 100,
                      background: '#0f172a',
                    }}
                  >
                    <div style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      marginBottom: '12px',
                    }}>
                      <div style={{ fontWeight: 600, fontSize: '0.9rem', color: '#f8fafc' }}>
                        Live Notifications
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <button
                          type="button"
                          onClick={async () => {
                            try {
                              await notificationApi.testPush('STOMP Live Notification', 'Real-time WebSocket event received successfully!');
                            } catch (e) {
                              console.warn('Test push error', e);
                            }
                          }}
                          style={{
                            background: 'rgba(59, 130, 246, 0.1)',
                            border: '1px solid rgba(59, 130, 246, 0.3)',
                            borderRadius: '6px',
                            color: '#93c5fd',
                            fontSize: '0.7rem',
                            padding: '3px 8px',
                            cursor: 'pointer',
                          }}
                        >
                          ⚡ Test Push
                        </button>
                        {unreadCount > 0 && (
                          <button
                            onClick={markAllAsRead}
                            style={{
                              background: 'none',
                              border: 'none',
                              color: '#3b82f6',
                              fontSize: '0.75rem',
                              cursor: 'pointer',
                              display: 'flex',
                              alignItems: 'center',
                              gap: '4px',
                            }}
                          >
                            <CheckCheck size={14} /> Mark read
                          </button>
                        )}
                      </div>
                    </div>

                    {notifications.length === 0 ? (
                      <div style={{ textAlign: 'center', padding: '24px 0', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                        No new notifications
                      </div>
                    ) : (
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                        {notifications.map((n) => (
                          <div
                            key={n.id}
                            style={{
                              padding: '10px',
                              borderRadius: '8px',
                              background: n.read ? 'transparent' : 'rgba(59, 130, 246, 0.08)',
                              border: '1px solid var(--border-subtle)',
                              fontSize: '0.8rem',
                            }}
                          >
                            <div style={{ fontWeight: 600, color: '#f8fafc' }}>{n.title}</div>
                            <div style={{ color: 'var(--text-secondary)', marginTop: '2px' }}>{n.message}</div>
                            <div style={{ color: 'var(--text-muted)', fontSize: '0.7rem', marginTop: '4px' }}>
                              {n.timestamp}
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                )}
              </div>

              {/* User Profile Pill & Dropdown */}
              <div ref={userMenuRef} style={{ position: 'relative' }}>
                <button
                  onClick={() => setShowUserMenu(!showUserMenu)}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '10px',
                    background: 'rgba(255, 255, 255, 0.04)',
                    border: '1px solid var(--border-medium)',
                    borderRadius: '10px',
                    padding: '6px 14px',
                    cursor: 'pointer',
                    color: 'var(--text-main)',
                  }}
                >
                  <div style={{
                    width: '28px',
                    height: '28px',
                    borderRadius: '50%',
                    background: 'var(--primary-gradient)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontWeight: 700,
                    fontSize: '0.8rem',
                  }}>
                    {user?.name?.[0]?.toUpperCase() || 'U'}
                  </div>
                  <div style={{ textAlign: 'left', lineHeight: 1.2 }}>
                    <div style={{ fontSize: '0.825rem', fontWeight: 600 }}>{user?.name}</div>
                    <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>{user?.role}</div>
                  </div>
                </button>

                {showUserMenu && (
                  <div
                    className="glass-panel"
                    style={{
                      position: 'absolute',
                      right: 0,
                      top: '48px',
                      width: '200px',
                      padding: '8px',
                      zIndex: 100,
                      background: '#0f172a',
                    }}
                  >
                    <div style={{ padding: '8px 12px', borderBottom: '1px solid var(--border-subtle)', marginBottom: '4px' }}>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Signed in as</div>
                      <div style={{ fontSize: '0.8125rem', fontWeight: 600, color: '#f8fafc', wordBreak: 'break-all' }}>
                        {user?.email}
                      </div>
                    </div>

                    {isCandidate && (
                      <Link
                        to="/profile"
                        onClick={() => setShowUserMenu(false)}
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: '8px',
                          padding: '8px 12px',
                          color: 'var(--text-secondary)',
                          textDecoration: 'none',
                          fontSize: '0.85rem',
                          borderRadius: '6px',
                        }}
                      >
                        <User size={15} /> My Profile
                      </Link>
                    )}

                    {isRecruiter && (
                      <Link
                        to="/recruiter"
                        onClick={() => setShowUserMenu(false)}
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: '8px',
                          padding: '8px 12px',
                          color: 'var(--text-secondary)',
                          textDecoration: 'none',
                          fontSize: '0.85rem',
                          borderRadius: '6px',
                        }}
                      >
                        <LayoutDashboard size={15} /> Dashboard
                      </Link>
                    )}

                    <button
                      onClick={handleLogout}
                      style={{
                        width: '100%',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                        padding: '8px 12px',
                        background: 'none',
                        border: 'none',
                        color: '#fda4af',
                        fontSize: '0.85rem',
                        cursor: 'pointer',
                        textAlign: 'left',
                        borderRadius: '6px',
                        marginTop: '4px',
                      }}
                    >
                      <LogOut size={15} /> Sign Out
                    </button>
                  </div>
                )}
              </div>
            </>
          ) : (
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Link to="/login" className="btn btn-secondary btn-sm">
                <LogIn size={15} /> Sign In
              </Link>
              <Link to="/register" className="btn btn-primary btn-sm">
                <UserPlus size={15} /> Get Started
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

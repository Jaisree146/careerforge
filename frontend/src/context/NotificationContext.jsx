import React, { createContext, useContext, useState, useEffect } from 'react';
import { useAuth } from './AuthContext';
import { initWebSocket, disconnectWebSocket } from '../services/websocket';

const NotificationContext = createContext(null);

export const NotificationProvider = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [toasts, setToasts] = useState([]);

  useEffect(() => {
    if (!isAuthenticated) {
      disconnectWebSocket();
      return;
    }

    const client = initWebSocket({
      onNotification: (payload) => {
        const item = {
          id: Date.now() + Math.random(),
          title: payload.title || 'Platform Notification',
          message: payload.message || payload.content || 'You have an update on your profile or application.',
          type: payload.type || 'info',
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
          read: false,
        };

        setNotifications((prev) => [item, ...prev]);

        // Add toast popup
        setToasts((prev) => [...prev, item]);
        setTimeout(() => {
          setToasts((prev) => prev.filter((t) => t.id !== item.id));
        }, 6000);
      },
      onAnnouncement: (payload) => {
        const item = {
          id: Date.now() + Math.random(),
          title: 'System Announcement',
          message: payload.message || payload.content || 'Broadcast alert from CareerForge',
          type: 'announcement',
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
          read: false,
        };
        setNotifications((prev) => [item, ...prev]);
        setToasts((prev) => [...prev, item]);
        setTimeout(() => {
          setToasts((prev) => prev.filter((t) => t.id !== item.id));
        }, 6000);
      },
    });

    return () => {
      disconnectWebSocket();
    };
  }, [isAuthenticated]);

  const markAllAsRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
  };

  const removeToast = (id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  };

  const unreadCount = notifications.filter((n) => !n.read).length;

  return (
    <NotificationContext.Provider
      value={{
        notifications,
        unreadCount,
        markAllAsRead,
      }}
    >
      {children}

      {/* Floating Real-time Toast Notifications Container */}
      <div style={{
        position: 'fixed',
        bottom: '24px',
        right: '24px',
        zIndex: 9999,
        display: 'flex',
        flexDirection: 'column',
        gap: '10px',
        maxWidth: '380px',
        width: '100%',
        pointerEvents: 'none',
      }}>
        {toasts.map((toast) => (
          <div
            key={toast.id}
            className="glass-panel animate-fade-in"
            style={{
              pointerEvents: 'auto',
              padding: '14px 16px',
              backgroundColor: '#131c31',
              borderLeft: toast.type === 'announcement' ? '4px solid #8b5cf6' : '4px solid #3b82f6',
              boxShadow: '0 10px 25px -5px rgba(0,0,0,0.6)',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'flex-start',
              gap: '12px',
            }}
          >
            <div>
              <div style={{ fontWeight: 600, fontSize: '0.875rem', color: '#f8fafc' }}>{toast.title}</div>
              <div style={{ fontSize: '0.8125rem', color: '#94a3b8', marginTop: '2px' }}>{toast.message}</div>
              <div style={{ fontSize: '0.7rem', color: '#64748b', marginTop: '6px' }}>{toast.timestamp}</div>
            </div>
            <button
              onClick={() => removeToast(toast.id)}
              style={{
                background: 'none',
                border: 'none',
                color: '#64748b',
                cursor: 'pointer',
                fontSize: '1rem',
                lineHeight: 1,
              }}
            >
              ×
            </button>
          </div>
        ))}
      </div>
    </NotificationContext.Provider>
  );
};

export const useNotification = () => useContext(NotificationContext);

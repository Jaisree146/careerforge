import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { getToken } from './api';

let stompClient = null;
let activeSubscriptions = [];

export const initWebSocket = ({ onNotification, onAnnouncement }) => {
  const token = getToken();

  // If already active, disconnect before reconnecting
  if (stompClient && stompClient.active) {
    stompClient.deactivate();
  }

  stompClient = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
    debug: (str) => {
      // console.log('[STOMP]', str);
    },
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  });

  stompClient.onConnect = (frame) => {
    // 1. Subscribe to User Personal Queue (Application status updates, matches, direct alerts)
    if (token) {
      const userSub = stompClient.subscribe('/user/queue/notifications', (message) => {
        try {
          const payload = JSON.parse(message.body);
          if (onNotification) onNotification(payload);
        } catch (e) {
          console.error('Failed to parse STOMP user notification', e);
        }
      });
      activeSubscriptions.push(userSub);
    }

    // 2. Subscribe to System-Wide Broadcast Announcements
    const announcementSub = stompClient.subscribe('/topic/announcements', (message) => {
      try {
        const payload = JSON.parse(message.body);
        if (onAnnouncement) onAnnouncement(payload);
      } catch (e) {
        console.error('Failed to parse STOMP announcement', e);
      }
    });
    activeSubscriptions.push(announcementSub);
  };

  stompClient.onStompError = (frame) => {
    console.error('STOMP protocol error: ' + frame.headers['message']);
  };

  stompClient.activate();
  return stompClient;
};

export const disconnectWebSocket = () => {
  activeSubscriptions.forEach((sub) => sub.unsubscribe());
  activeSubscriptions = [];
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
  }
};

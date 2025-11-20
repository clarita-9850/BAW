import React, { useState, useEffect } from 'react';
import { apiClient as api } from '../config/api';
import './NotificationCenter.css';

const NotificationCenter = ({ userId }) => {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isOpen, setIsOpen] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchNotifications();
    fetchUnreadCount();
    
    // Poll for new notifications every 5 seconds
    const interval = setInterval(() => {
      fetchNotifications();
      fetchUnreadCount();
    }, 5000);

    return () => clearInterval(interval);
  }, [userId]);

  const fetchNotifications = async () => {
    try {
      const response = await api.get(`/notifications/user/${userId}`);
      setNotifications(response.data);
    } catch (error) {
      console.error('Error fetching notifications:', error);
    }
  };

  const fetchUnreadCount = async () => {
    try {
      const response = await api.get(`/notifications/unread-count/${userId}`);
      // Ensure we convert to number in case backend returns string or other format
      const count = typeof response.data === 'number' ? response.data : parseInt(response.data, 10) || 0;
      setUnreadCount(count);
      console.log(`Unread count for ${userId}:`, count);
    } catch (error) {
      console.error('Error fetching unread count:', error);
      setUnreadCount(0);
    }
  };

  const markAsRead = async (notificationId) => {
    try {
      await api.put(`/notifications/${notificationId}/read`);
      fetchNotifications();
      fetchUnreadCount();
    } catch (error) {
      console.error('Error marking notification as read:', error);
    }
  };

  const markAllAsRead = async () => {
    try {
      await api.put(`/notifications/user/${userId}/read-all`);
      fetchNotifications();
      fetchUnreadCount();
    } catch (error) {
      console.error('Error marking all as read:', error);
    }
  };

  const getNotificationIcon = (type) => {
    switch (type) {
      case 'ALERT': return '⚠️';
      case 'WARNING': return '⚠️';
      case 'SUCCESS': return '✅';
      case 'INFO': return 'ℹ️';
      default: return '📢';
    }
  };

  const getNotificationColor = (type) => {
    switch (type) {
      case 'ALERT': return '#dc3545';
      case 'WARNING': return '#ffc107';
      case 'SUCCESS': return '#28a745';
      case 'INFO': return '#17a2b8';
      default: return '#6c757d';
    }
  };

  return (
    <div className="notification-center">
      <div className="notification-bell-container">
        <button 
          className="notification-bell"
          onClick={() => setIsOpen(!isOpen)}
        >
          🔔
          {unreadCount > 0 && (
            <span className="notification-badge">{unreadCount}</span>
          )}
        </button>

        {isOpen && (
          <div className="notification-dropdown">
            <div className="notification-header">
              <h3>Notifications</h3>
              {unreadCount > 0 && (
                <button 
                  className="mark-all-read-btn"
                  onClick={markAllAsRead}
                >
                  Mark all as read
                </button>
              )}
            </div>

            <div className="notification-list">
              {notifications.length === 0 ? (
                <div className="no-notifications">
                  <p>No notifications</p>
                </div>
              ) : (
                notifications.slice(0, 10).map(notification => (
                  <div 
                    key={notification.id} 
                    className={`notification-item ${!notification.readStatus ? 'unread' : ''}`}
                    onClick={() => {
                      if (!notification.readStatus) {
                        markAsRead(notification.id);
                      }
                      if (notification.actionLink) {
                        window.location.href = notification.actionLink;
                      }
                    }}
                  >
                    <div className="notification-icon" style={{ 
                      backgroundColor: getNotificationColor(notification.notificationType) 
                    }}>
                      {getNotificationIcon(notification.notificationType)}
                    </div>
                    
                    <div className="notification-content">
                      <p className="notification-message">{notification.message}</p>
                      <span className="notification-time">
                        {new Date(notification.createdAt).toLocaleString()}
                      </span>
                    </div>
                    
                    {!notification.readStatus && (
                      <div className="unread-indicator"></div>
                    )}
                  </div>
                ))
              )}
            </div>

            {notifications.length > 10 && (
              <div className="notification-footer">
                <button className="view-all-btn">View all notifications</button>
              </div>
            )}
          </div>
        )}
      </div>

      <div className="notification-stats">
        <div className="stat-item">
          <div className="stat-value">{unreadCount}</div>
          <div className="stat-label">Unread</div>
        </div>
        <div className="stat-item">
          <div className="stat-value">{notifications.length}</div>
          <div className="stat-label">Total</div>
        </div>
      </div>
    </div>
  );
};

export default NotificationCenter;





'use client';

import React, { useState, useEffect } from 'react';
import apiClient from '@/lib/api';

type Notification = {
  id: number;
  message: string;
  notificationType: 'ALERT' | 'WARNING' | 'SUCCESS' | 'INFO';
  actionLink?: string;
  readStatus: boolean;
  createdAt: string;
};

type NotificationCenterProps = {
  userId: string;
};

export default function NotificationCenter({ userId }: NotificationCenterProps) {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isOpen, setIsOpen] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (userId) {
      fetchNotifications();
      fetchUnreadCount();
      
      // Poll for new notifications every 5 seconds
      const interval = setInterval(() => {
        fetchNotifications();
        fetchUnreadCount();
      }, 5000);

      return () => clearInterval(interval);
    }
  }, [userId]);

  const fetchNotifications = async () => {
    try {
      const response = await apiClient.get(`/notifications/user/${userId}`);
      setNotifications(response.data || []);
    } catch (error: any) {
      console.error('Error fetching notifications:', error);
      setNotifications([]);
    }
  };

  const fetchUnreadCount = async () => {
    try {
      const response = await apiClient.get(`/notifications/unread-count/${userId}`);
      const count = typeof response.data === 'number' ? response.data : parseInt(response.data, 10) || 0;
      setUnreadCount(count);
    } catch (error: any) {
      console.error('Error fetching unread count:', error);
      setUnreadCount(0);
    }
  };

  const markAsRead = async (notificationId: number) => {
    try {
      await apiClient.put(`/notifications/${notificationId}/read`);
      fetchNotifications();
      fetchUnreadCount();
    } catch (error: any) {
      console.error('Error marking notification as read:', error);
    }
  };

  const markAllAsRead = async () => {
    try {
      await apiClient.put(`/notifications/user/${userId}/read-all`);
      fetchNotifications();
      fetchUnreadCount();
    } catch (error: any) {
      console.error('Error marking all as read:', error);
    }
  };

  const getNotificationIcon = (type: string) => {
    switch (type) {
      case 'ALERT': return '⚠️';
      case 'WARNING': return '⚠️';
      case 'SUCCESS': return '✅';
      case 'INFO': return 'ℹ️';
      default: return '📢';
    }
  };

  const getNotificationColor = (type: string) => {
    switch (type) {
      case 'ALERT': return '#dc3545';
      case 'WARNING': return '#ffc107';
      case 'SUCCESS': return '#28a745';
      case 'INFO': return '#17a2b8';
      default: return '#6c757d';
    }
  };

  return (
    <div className="relative">
      <button
        className="relative px-3 py-2 text-gray-700 hover:text-gray-900 transition-colors"
        onClick={() => setIsOpen(!isOpen)}
      >
        <span className="text-2xl">🔔</span>
        {unreadCount > 0 && (
          <span className="absolute top-0 right-0 bg-red-600 text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <>
          <div
            className="fixed inset-0 z-40"
            onClick={() => setIsOpen(false)}
          ></div>
          <div className="absolute right-0 mt-2 w-80 bg-white border border-gray-300 rounded-lg shadow-xl z-50">
            <div className="bg-[#1e3a8a] px-4 py-3 flex justify-between items-center rounded-t-lg">
              <h3 className="text-white font-semibold">Notifications</h3>
              {unreadCount > 0 && (
                <button
                  className="text-white text-xs hover:text-gray-200 underline"
                  onClick={markAllAsRead}
                >
                  Mark all as read
                </button>
              )}
            </div>

            <div className="max-h-96 overflow-y-auto">
              {notifications.length === 0 ? (
                <div className="p-8 text-center text-gray-500">
                  <p>No notifications</p>
                </div>
              ) : (
                notifications.slice(0, 10).map(notification => (
                  <div
                    key={notification.id}
                    className={`p-4 border-b border-gray-200 cursor-pointer hover:bg-gray-50 transition-colors ${
                      !notification.readStatus ? 'bg-blue-50' : ''
                    }`}
                    onClick={() => {
                      if (!notification.readStatus) {
                        markAsRead(notification.id);
                      }
                      if (notification.actionLink) {
                        window.location.href = notification.actionLink;
                      }
                      setIsOpen(false);
                    }}
                  >
                    <div className="flex gap-3">
                      <div
                        className="w-8 h-8 rounded-full flex items-center justify-center text-white text-sm flex-shrink-0"
                        style={{ backgroundColor: getNotificationColor(notification.notificationType) }}
                      >
                        {getNotificationIcon(notification.notificationType)}
                      </div>
                      <div className="flex-1">
                        <p className={`text-sm ${!notification.readStatus ? 'font-semibold' : 'font-normal'} text-gray-900`}>
                          {notification.message}
                        </p>
                        <span className="text-xs text-gray-500 mt-1 block">
                          {new Date(notification.createdAt).toLocaleString()}
                        </span>
                      </div>
                      {!notification.readStatus && (
                        <div className="w-2 h-2 bg-[#1e3a8a] rounded-full flex-shrink-0 mt-2"></div>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>

            {notifications.length > 10 && (
              <div className="p-3 border-t border-gray-200 text-center">
                <button className="text-sm text-[#1e3a8a] hover:text-[#1e40af] font-medium">
                  View all notifications
                </button>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}


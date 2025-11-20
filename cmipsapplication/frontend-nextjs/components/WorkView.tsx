'use client';

import React, { useState, useEffect } from 'react';
import apiClient from '@/lib/api';

type Task = {
  id: number;
  title: string;
  description?: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'CLOSED' | 'ESCALATED' | string;
  priority: 'HIGH' | 'MEDIUM' | 'LOW' | string;
  assignedTo?: string;
  workQueue?: string;
  dueDate?: string;
  actionLink?: string;
};

type WorkViewProps = {
  username: string;
};

export default function WorkView({ username }: WorkViewProps) {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [filteredTasks, setFilteredTasks] = useState<Task[]>([]);
  const [taskCounts, setTaskCounts] = useState({ open: 0, inProgress: 0, closed: 0 });
  const [selectedStatus, setSelectedStatus] = useState<'ALL' | 'OPEN' | 'IN_PROGRESS' | 'CLOSED'>('ALL');
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (username) {
      fetchTasks();
      fetchTaskCounts();
    }
  }, [username]);

  useEffect(() => {
    if (selectedStatus === 'ALL') {
      setFilteredTasks(tasks);
    } else {
      setFilteredTasks(tasks.filter(task => task.status === selectedStatus));
    }
  }, [tasks, selectedStatus]);

  const fetchTasks = async () => {
    setLoading(true);
    try {
      // For caseworkers, include tasks from subscribed queues
      // Check if user is a caseworker by checking localStorage
      const userStr = typeof window !== 'undefined' ? localStorage.getItem('user') : null;
      const user = userStr ? JSON.parse(userStr) : null;
      const isCaseWorker = user?.role === 'CASE_WORKER' || user?.roles?.includes('CASE_WORKER');
      
      const url = isCaseWorker 
        ? `/tasks?username=${username}&includeSubscribedQueues=true`
        : `/tasks?username=${username}`;
      
      const response = await apiClient.get(url);
      setTasks(response.data || []);
    } catch (error: any) {
      console.error('Error fetching tasks:', error);
      setTasks([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchTaskCounts = async () => {
    try {
      const response = await apiClient.get(`/tasks/count/${username}`);
      setTaskCounts(response.data || { open: 0, inProgress: 0, closed: 0 });
    } catch (error: any) {
      console.error('Error fetching task counts:', error);
      setTaskCounts({ open: 0, inProgress: 0, closed: 0 });
    }
  };

  const updateTaskStatus = async (taskId: number, newStatus: string) => {
    try {
      await apiClient.put(`/tasks/${taskId}/status`, { status: newStatus });
      fetchTasks();
      fetchTaskCounts();
      setSelectedTask(null);
    } catch (error: any) {
      console.error('Error updating task status:', error);
      alert('Failed to update task status');
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'OPEN': return '#ffc107';
      case 'IN_PROGRESS': return '#17a2b8';
      case 'CLOSED': return '#28a745';
      case 'ESCALATED': return '#dc3545';
      default: return '#6c757d';
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'HIGH': return '#dc3545';
      case 'MEDIUM': return '#ffc107';
      case 'LOW': return '#17a2b8';
      default: return '#6c757d';
    }
  };

  if (loading) {
    return (
      <div className="bg-white border border-gray-300 rounded-lg p-6">
        <div className="text-center py-8">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#1e3a8a] mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading tasks...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white border border-gray-300 rounded-lg overflow-hidden">
      <div className="bg-[#1e3a8a] px-6 py-4 flex justify-between items-center">
        <h2 className="text-lg font-bold text-white">📋 Work View</h2>
        <button 
          onClick={fetchTasks}
          className="px-4 py-2 bg-white text-[#1e3a8a] rounded hover:bg-gray-100 text-sm font-medium"
        >
          🔄 Refresh
        </button>
      </div>

      {/* Task Counts */}
      <div className="px-6 py-4 bg-gray-50 border-b border-gray-300 grid grid-cols-3 gap-4">
        <div className="text-center">
          <div className="text-2xl font-bold text-gray-900">{taskCounts.open}</div>
          <div className="text-xs text-gray-600">Open</div>
        </div>
        <div className="text-center">
          <div className="text-2xl font-bold text-gray-900">{taskCounts.inProgress}</div>
          <div className="text-xs text-gray-600">In Progress</div>
        </div>
        <div className="text-center">
          <div className="text-2xl font-bold text-gray-900">{taskCounts.closed}</div>
          <div className="text-xs text-gray-600">Closed</div>
        </div>
      </div>

      {/* Filters */}
      <div className="px-6 py-3 bg-gray-50 border-b border-gray-300 flex gap-2">
        <button
          className={`px-4 py-2 text-sm font-medium rounded ${
            selectedStatus === 'ALL'
              ? 'bg-[#1e3a8a] text-white'
              : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-100'
          }`}
          onClick={() => setSelectedStatus('ALL')}
        >
          All Tasks
        </button>
        <button
          className={`px-4 py-2 text-sm font-medium rounded ${
            selectedStatus === 'OPEN'
              ? 'bg-[#1e3a8a] text-white'
              : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-100'
          }`}
          onClick={() => setSelectedStatus('OPEN')}
        >
          Open ({taskCounts.open})
        </button>
        <button
          className={`px-4 py-2 text-sm font-medium rounded ${
            selectedStatus === 'IN_PROGRESS'
              ? 'bg-[#1e3a8a] text-white'
              : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-100'
          }`}
          onClick={() => setSelectedStatus('IN_PROGRESS')}
        >
          In Progress ({taskCounts.inProgress})
        </button>
        <button
          className={`px-4 py-2 text-sm font-medium rounded ${
            selectedStatus === 'CLOSED'
              ? 'bg-[#1e3a8a] text-white'
              : 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-100'
          }`}
          onClick={() => setSelectedStatus('CLOSED')}
        >
          Closed ({taskCounts.closed})
        </button>
      </div>

      {/* Task List */}
      <div className="p-6">
        {filteredTasks.length === 0 ? (
          <div className="text-center py-8 text-gray-500">
            <p>No tasks found</p>
          </div>
        ) : (
          <div className="space-y-3">
            {filteredTasks.map(task => (
              <div
                key={task.id}
                className="border border-gray-300 rounded-lg p-4 hover:border-[#1e3a8a] hover:shadow-md transition-all cursor-pointer"
                onClick={() => setSelectedTask(task)}
              >
                <div className="flex justify-between items-start mb-2">
                  <h3 className="font-semibold text-gray-900">{task.title}</h3>
                  <div className="flex gap-2">
                    <span
                      className="px-2 py-1 rounded-full text-xs font-semibold text-white"
                      style={{ backgroundColor: getStatusColor(task.status) }}
                    >
                      {task.status}
                    </span>
                    <span
                      className="px-2 py-1 rounded-full text-xs font-semibold text-white"
                      style={{ backgroundColor: getPriorityColor(task.priority) }}
                    >
                      {task.priority}
                    </span>
                  </div>
                </div>
                {task.description && (
                  <p className="text-sm text-gray-600 mb-2">{task.description}</p>
                )}
                <div className="flex gap-4 text-xs text-gray-500">
                  {task.dueDate && (
                    <span>📅 Due: {new Date(task.dueDate).toLocaleDateString()}</span>
                  )}
                  <span>👤 Assigned: {task.assignedTo || 'Unassigned'}</span>
                  {task.workQueue && <span>📋 Queue: {task.workQueue}</span>}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Task Detail Modal */}
      {selectedTask && (
        <TaskDetailModal
          task={selectedTask}
          onClose={() => setSelectedTask(null)}
          onUpdateStatus={updateTaskStatus}
        />
      )}
    </div>
  );
}

const TaskDetailModal = ({
  task,
  onClose,
  onUpdateStatus,
}: {
  task: Task;
  onClose: () => void;
  onUpdateStatus: (taskId: number, newStatus: string) => void;
}) => {
  const getStatusOptions = () => {
    switch (task.status) {
      case 'OPEN':
        return ['IN_PROGRESS', 'CLOSED'];
      case 'IN_PROGRESS':
        return ['CLOSED'];
      default:
        return [];
    }
  };

  return (
    <div
      className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      onClick={onClose}
    >
      <div
        className="bg-white rounded-lg w-full max-w-2xl max-h-[80vh] overflow-y-auto"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex justify-between items-center p-6 border-b border-gray-300 bg-[#1e3a8a]">
          <h2 className="text-xl font-bold text-white">{task.title}</h2>
          <button
            onClick={onClose}
            className="text-white hover:text-gray-200 text-2xl font-bold leading-none w-8 h-8 flex items-center justify-center rounded hover:bg-[#1e40af] transition-colors"
          >
            ×
          </button>
        </div>

        <div className="p-6">
          {task.description && (
            <div className="mb-6">
              <h4 className="text-base font-semibold text-gray-900 mb-2">Description</h4>
              <p className="text-gray-600 leading-relaxed">{task.description}</p>
            </div>
          )}

          <div className="mb-6">
            <h4 className="text-base font-semibold text-gray-900 mb-3">Details</h4>
            <div className="grid grid-cols-2 gap-4">
              <div className="flex flex-col gap-1">
                <label className="font-semibold text-gray-900 text-sm">Status:</label>
                <span className="text-gray-600">{task.status}</span>
              </div>
              <div className="flex flex-col gap-1">
                <label className="font-semibold text-gray-900 text-sm">Priority:</label>
                <span className="text-gray-600">{task.priority}</span>
              </div>
              <div className="flex flex-col gap-1">
                <label className="font-semibold text-gray-900 text-sm">Assigned To:</label>
                <span className="text-gray-600">{task.assignedTo || 'Unassigned'}</span>
              </div>
              {task.dueDate && (
                <div className="flex flex-col gap-1">
                  <label className="font-semibold text-gray-900 text-sm">Due Date:</label>
                  <span className="text-gray-600">{new Date(task.dueDate).toLocaleDateString()}</span>
                </div>
              )}
              {task.workQueue && (
                <div className="flex flex-col gap-1">
                  <label className="font-semibold text-gray-900 text-sm">Work Queue:</label>
                  <span className="text-gray-600">{task.workQueue}</span>
                </div>
              )}
            </div>
          </div>

          {task.actionLink && (
            <div className="mb-6">
              <a
                href={task.actionLink}
                className="inline-block px-4 py-2 bg-[#1e3a8a] text-white rounded hover:bg-[#1e40af] font-medium"
              >
                Open Related Entity
              </a>
            </div>
          )}

          <div>
            <h4 className="text-base font-semibold text-gray-900 mb-3">Update Status</h4>
            <div className="flex flex-wrap gap-2">
              {getStatusOptions().map((status) => (
                <button
                  key={status}
                  onClick={() => {
                    onUpdateStatus(task.id, status);
                  }}
                  className="px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300 font-medium"
                >
                  Mark as {status.replace('_', ' ')}
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};


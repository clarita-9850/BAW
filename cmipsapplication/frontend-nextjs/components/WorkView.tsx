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
          <div className="spinner-border text-primary mx-auto" role="status" style={{ width: '3rem', height: '3rem' }}>
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="mt-4 text-gray-600">Loading tasks...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white border border-gray-300 rounded-lg overflow-hidden">
      <div className="bg-primary px-6 py-4 d-flex justify-content-between align-items-center" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
        <h2 className="h5 mb-0 text-white fw-bold">📋 Work View</h2>
        <button 
          onClick={fetchTasks}
          className="btn btn-light btn-sm"
          style={{ color: 'var(--color-p2, #046b99)' }}
        >
          🔄 Refresh
        </button>
      </div>

      {/* Task Counts */}
      <div className="px-4 py-3 bg-light border-bottom">
        <div className="row text-center">
          <div className="col-4">
            <div className="h3 fw-bold mb-1" style={{ color: 'var(--color-p2, #046b99)' }}>{taskCounts.open}</div>
            <div className="small text-muted">Open</div>
          </div>
          <div className="col-4">
            <div className="h3 fw-bold mb-1" style={{ color: 'var(--color-p2, #046b99)' }}>{taskCounts.inProgress}</div>
            <div className="small text-muted">In Progress</div>
          </div>
          <div className="col-4">
            <div className="h3 fw-bold mb-1" style={{ color: 'var(--color-p2, #046b99)' }}>{taskCounts.closed}</div>
            <div className="small text-muted">Closed</div>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="px-6 py-3 bg-gray-50 border-b border-gray-300 d-flex gap-2">
        <button
          className={`btn btn-sm ${selectedStatus === 'ALL' ? 'btn-primary' : 'btn-outline-primary'}`}
          onClick={() => setSelectedStatus('ALL')}
        >
          All Tasks
        </button>
        <button
          className={`btn btn-sm ${selectedStatus === 'OPEN' ? 'btn-primary' : 'btn-outline-primary'}`}
          onClick={() => setSelectedStatus('OPEN')}
        >
          Open ({taskCounts.open})
        </button>
        <button
          className={`btn btn-sm ${selectedStatus === 'IN_PROGRESS' ? 'btn-primary' : 'btn-outline-primary'}`}
          onClick={() => setSelectedStatus('IN_PROGRESS')}
        >
          In Progress ({taskCounts.inProgress})
        </button>
        <button
          className={`btn btn-sm ${selectedStatus === 'CLOSED' ? 'btn-primary' : 'btn-outline-primary'}`}
          onClick={() => setSelectedStatus('CLOSED')}
        >
          Closed ({taskCounts.closed})
        </button>
      </div>

      {/* Task List */}
      <div className="p-4">
        {filteredTasks.length === 0 ? (
          <div className="text-center py-5 text-muted">
            <p className="mb-0">No tasks found</p>
          </div>
        ) : (
          <div>
            {filteredTasks.map(task => (
              <div
                key={task.id}
                className="card mb-3"
                style={{ 
                  cursor: 'pointer',
                  transition: 'all 0.2s',
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.borderColor = 'var(--color-p2, #046b99)';
                  e.currentTarget.style.boxShadow = '0 0.125rem 0.25rem rgba(0, 0, 0, 0.075)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.borderColor = 'var(--bs-border-color, #dee2e6)';
                  e.currentTarget.style.boxShadow = 'none';
                }}
                onClick={() => setSelectedTask(task)}
              >
                <div className="card-body">
                  <div className="d-flex justify-content-between align-items-start mb-2">
                    <h5 className="card-title mb-0 fw-semibold">{task.title}</h5>
                    <div className="d-flex gap-2">
                      <span
                        className="badge"
                        style={{ backgroundColor: getStatusColor(task.status) }}
                      >
                        {task.status}
                      </span>
                      <span
                        className="badge"
                        style={{ backgroundColor: getPriorityColor(task.priority) }}
                      >
                        {task.priority}
                      </span>
                    </div>
                  </div>
                  {task.description && (
                    <p className="card-text text-muted small mb-2">{task.description}</p>
                  )}
                  <div className="d-flex gap-3 small text-muted">
                    {task.dueDate && (
                      <span>📅 Due: {new Date(task.dueDate).toLocaleDateString()}</span>
                    )}
                    <span>👤 Assigned: {task.assignedTo || 'Unassigned'}</span>
                    {task.workQueue && <span>📋 Queue: {task.workQueue}</span>}
                  </div>
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
      className="modal fade show d-block"
      style={{ backgroundColor: 'rgba(0, 0, 0, 0.5)', zIndex: 1050 }}
      onClick={onClose}
      role="dialog"
      aria-modal="true"
    >
      <div
        className="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="modal-content">
        <div className="d-flex justify-content-between align-items-center p-4 border-bottom bg-primary" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
          <h2 className="h4 mb-0 text-white fw-bold">{task.title}</h2>
          <button
            onClick={onClose}
            className="btn-close btn-close-white"
            aria-label="Close"
          ></button>
        </div>

        <div className="p-4">
          {task.description && (
            <div className="mb-4">
              <h4 className="h5 fw-semibold mb-2">Description</h4>
              <p className="text-muted">{task.description}</p>
            </div>
          )}

          <div className="mb-4">
            <h4 className="h5 fw-semibold mb-3">Details</h4>
            <div className="row">
              <div className="col-md-6 mb-3">
                <label className="fw-semibold small">Status:</label>
                <div className="text-muted">{task.status}</div>
              </div>
              <div className="col-md-6 mb-3">
                <label className="fw-semibold small">Priority:</label>
                <div className="text-muted">{task.priority}</div>
              </div>
              <div className="col-md-6 mb-3">
                <label className="fw-semibold small">Assigned To:</label>
                <div className="text-muted">{task.assignedTo || 'Unassigned'}</div>
              </div>
              {task.dueDate && (
                <div className="col-md-6 mb-3">
                  <label className="fw-semibold small">Due Date:</label>
                  <div className="text-muted">{new Date(task.dueDate).toLocaleDateString()}</div>
                </div>
              )}
              {task.workQueue && (
                <div className="col-md-6 mb-3">
                  <label className="fw-semibold small">Work Queue:</label>
                  <div className="text-muted">{task.workQueue}</div>
                </div>
              )}
            </div>
          </div>

          {task.actionLink && (
            <div className="mb-4">
              <a
                href={task.actionLink}
                className="btn btn-primary"
              >
                Open Related Entity
              </a>
            </div>
          )}

          <div>
            <h4 className="h5 fw-semibold mb-3">Update Status</h4>
            <div className="d-flex flex-wrap gap-2">
              {getStatusOptions().map((status) => (
                <button
                  key={status}
                  onClick={() => {
                    onUpdateStatus(task.id, status);
                  }}
                  className="btn btn-secondary"
                >
                  Mark as {status.replace('_', ' ')}
                </button>
              ))}
            </div>
          </div>
        </div>
        </div>
      </div>
    </div>
  );
};


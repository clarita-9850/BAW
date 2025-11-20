import React, { useState, useEffect } from 'react';
import { apiClient as api } from '../config/api';
import './WorkView.css';

const WorkView = ({ username }) => {
  const [tasks, setTasks] = useState([]);
  const [filteredTasks, setFilteredTasks] = useState([]);
  const [taskCounts, setTaskCounts] = useState({ open: 0, inProgress: 0, closed: 0 });
  const [selectedStatus, setSelectedStatus] = useState('ALL');
  const [selectedTask, setSelectedTask] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchTasks();
    fetchTaskCounts();
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
      const response = await api.get(`/tasks?username=${username}`);
      setTasks(response.data);
    } catch (error) {
      console.error('Error fetching tasks:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchTaskCounts = async () => {
    try {
      const response = await api.get(`/tasks/count/${username}`);
      setTaskCounts(response.data);
    } catch (error) {
      console.error('Error fetching task counts:', error);
    }
  };

  const updateTaskStatus = async (taskId, newStatus) => {
    try {
      await api.put(`/tasks/${taskId}/status`, { status: newStatus });
      fetchTasks();
      setSelectedTask(null);
    } catch (error) {
      console.error('Error updating task status:', error);
      alert('Failed to update task status');
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'OPEN': return '#ffc107';
      case 'IN_PROGRESS': return '#17a2b8';
      case 'CLOSED': return '#28a745';
      case 'ESCALATED': return '#dc3545';
      default: return '#6c757d';
    }
  };

  const getPriorityColor = (priority) => {
    switch (priority) {
      case 'HIGH': return '#dc3545';
      case 'MEDIUM': return '#ffc107';
      case 'LOW': return '#17a2b8';
      default: return '#6c757d';
    }
  };

  if (loading) {
    return (
      <div className="card">
        <div style={{ textAlign: 'center', padding: '40px' }}>
          <div className="spinner"></div>
          <p>Loading tasks...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="work-view">
      <div className="card">
        <div className="card-header">
          <h2 className="card-title">📋 Work View</h2>
          <button className="btn btn-primary" onClick={fetchTasks}>
            🔄 Refresh
          </button>
        </div>

        {/* Task Counts */}
        <div className="task-counts">
          <div className="count-item">
            <div className="count-value">{taskCounts.open}</div>
            <div className="count-label">Open</div>
          </div>
          <div className="count-item">
            <div className="count-value">{taskCounts.inProgress}</div>
            <div className="count-label">In Progress</div>
          </div>
          <div className="count-item">
            <div className="count-value">{taskCounts.closed}</div>
            <div className="count-label">Closed</div>
          </div>
        </div>

        {/* Filters */}
        <div className="filters">
          <button
            className={`filter-btn ${selectedStatus === 'ALL' ? 'active' : ''}`}
            onClick={() => setSelectedStatus('ALL')}
          >
            All Tasks
          </button>
          <button
            className={`filter-btn ${selectedStatus === 'OPEN' ? 'active' : ''}`}
            onClick={() => setSelectedStatus('OPEN')}
          >
            Open ({taskCounts.open})
          </button>
          <button
            className={`filter-btn ${selectedStatus === 'IN_PROGRESS' ? 'active' : ''}`}
            onClick={() => setSelectedStatus('IN_PROGRESS')}
          >
            In Progress ({taskCounts.inProgress})
          </button>
          <button
            className={`filter-btn ${selectedStatus === 'CLOSED' ? 'active' : ''}`}
            onClick={() => setSelectedStatus('CLOSED')}
          >
            Closed ({taskCounts.closed})
          </button>
        </div>

        {/* Task List */}
        <div className="task-list">
          {filteredTasks.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '40px', color: '#6c757d' }}>
              <p>No tasks found</p>
            </div>
          ) : (
            filteredTasks.map(task => (
              <div key={task.id} className="task-card" onClick={() => setSelectedTask(task)}>
                <div className="task-header">
                  <h3>{task.title}</h3>
                  <div className="task-badges">
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
                <p className="task-description">{task.description}</p>
                <div className="task-meta">
                  <span>📅 Due: {new Date(task.dueDate).toLocaleDateString()}</span>
                  <span>👤 Assigned: {task.assignedTo}</span>
                </div>
              </div>
            ))
          )}
        </div>
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
};

const TaskDetailModal = ({ task, onClose, onUpdateStatus }) => {
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
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{task.title}</h2>
          <button className="close-btn" onClick={onClose}>✕</button>
        </div>

        <div className="modal-body">
          <div className="detail-section">
            <h4>Description</h4>
            <p>{task.description}</p>
          </div>

          <div className="detail-section">
            <h4>Details</h4>
            <div className="detail-grid">
              <div className="detail-item">
                <label>Status:</label>
                <span>{task.status}</span>
              </div>
              <div className="detail-item">
                <label>Priority:</label>
                <span>{task.priority}</span>
              </div>
              <div className="detail-item">
                <label>Assigned To:</label>
                <span>{task.assignedTo}</span>
              </div>
              <div className="detail-item">
                <label>Due Date:</label>
                <span>{new Date(task.dueDate).toLocaleDateString()}</span>
              </div>
            </div>
          </div>

          {task.actionLink && (
            <div className="detail-section">
              <a href={task.actionLink} className="btn btn-primary">
                Open Related Entity
              </a>
            </div>
          )}

          <div className="detail-section">
            <h4>Update Status</h4>
            {getStatusOptions().map(status => (
              <button
                key={status}
                className="btn btn-secondary"
                onClick={() => {
                  onUpdateStatus(task.id, status);
                }}
                style={{ marginRight: '10px' }}
              >
                Mark as {status.replace('_', ' ')}
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default WorkView;





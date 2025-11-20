'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import apiClient from '@/lib/api';
import dynamic from 'next/dynamic';

type Task = {
  id: number;
  title: string;
  description?: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'CLOSED' | 'ESCALATED' | string;
  priority: 'HIGH' | 'MEDIUM' | 'LOW' | string;
  assignedTo?: string;
  workQueue?: string;
  dueDate?: string;
  createdAt?: string;
  actionLink?: string;
};

type QueueInfo = {
  name: string;
  displayName: string;
  description: string;
  supervisorOnly: boolean;
};

type QueueSubscription = {
  id: number;
  username: string;
  workQueue: string;
  subscribedBy: string;
  createdAt: string;
};

type User = {
  id: string;
  username: string;
  email?: string;
  firstName?: string;
  lastName?: string;
};

type UserQueueData = {
  username: string;
  queueName: string;
  queueDisplayName: string;
  tasks: Task[];
  taskCount: number;
};

type ViewType = 'myWorkspace' | 'workQueues' | 'queueTasks' | 'queueSubscriptions';

function SupervisorDashboardComponent() {
  const { user, logout, loading: authLoading } = useAuth();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');
  
  // Navigation state
  const [activeTab, setActiveTab] = useState<'myWorkspace' | 'workQueues'>('myWorkspace');
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  
  // Work Queues state
  const [queues, setQueues] = useState<QueueInfo[]>([]);
  const [selectedQueue, setSelectedQueue] = useState<QueueInfo | null>(null);
  const [queueView, setQueueView] = useState<ViewType>('myWorkspace' as ViewType);
  
  // My Workspace state
  const [userQueueData, setUserQueueData] = useState<UserQueueData[]>([]);
  const [escalatedTasks, setEscalatedTasks] = useState<Task[]>([]);
  const [allSubscriptions, setAllSubscriptions] = useState<QueueSubscription[]>([]);
  
  // Queue Tasks state
  const [queueTasks, setQueueTasks] = useState<Task[]>([]);
  const [selectedTasks, setSelectedTasks] = useState<Set<number>>(new Set());
  
  // Queue Subscriptions state
  const [queueSubscriptions, setQueueSubscriptions] = useState<QueueSubscription[]>([]);
  const [allUsers, setAllUsers] = useState<User[]>([]);
  const [showAddSubscriptionModal, setShowAddSubscriptionModal] = useState(false);
  const [selectedUserToAdd, setSelectedUserToAdd] = useState<string>('');

  useEffect(() => {
    if (authLoading) return;
    if (!user || (user.role !== 'SUPERVISOR' && !user.roles?.includes('SUPERVISOR'))) {
      window.location.href = '/login';
      return;
    }
    loadDashboardData();
  }, [user, authLoading]);

  useEffect(() => {
    if (activeTab === 'myWorkspace') {
      loadMyWorkspaceData();
    } else if (selectedQueue) {
      loadQueueTasks();
      loadQueueSubscriptions();
    }
  }, [activeTab, selectedQueue]);

  const loadDashboardData = async () => {
    setLoading(true);
    setError('');
    try {
      const catalogRes = await apiClient.get('/work-queues/catalog');
      const catalog: QueueInfo[] = catalogRes.data || [];
      setQueues(catalog);
      
      const usersRes = await apiClient.get('/work-queues/users');
      setAllUsers(usersRes.data || []);
      
      if (activeTab === 'myWorkspace') {
        await loadMyWorkspaceData();
      }
    } catch (err: any) {
      console.error('Error loading dashboard data:', err);
      setError(err?.response?.data?.error || err.message || 'Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const loadMyWorkspaceData = async () => {
    try {
      // Get all queues
      const catalogRes = await apiClient.get('/work-queues/catalog');
      const allQueues: QueueInfo[] = catalogRes.data || [];
      
      // Get all subscriptions
      const allSubs: QueueSubscription[] = [];
      for (const queue of allQueues) {
        if (!queue.supervisorOnly) {
          try {
            const subsRes = await apiClient.get(`/work-queues/queue/${queue.name}/details`);
            allSubs.push(...(subsRes.data || []));
          } catch (err) {
            console.log(`No subscriptions for ${queue.name}`);
          }
        }
      }
      setAllSubscriptions(allSubs);
      
      // Get escalated tasks (supervisor-only queue)
      try {
        const escalatedRes = await apiClient.get('/work-queues/ESCALATED/tasks');
        setEscalatedTasks(escalatedRes.data || []);
      } catch (err) {
        console.log('No escalated tasks');
        setEscalatedTasks([]);
      }
      
      // Group subscriptions by user and get their tasks
      const userQueueMap = new Map<string, UserQueueData>();
      
      for (const sub of allSubs) {
        const key = `${sub.username}_${sub.workQueue}`;
        if (!userQueueMap.has(key)) {
          const queueInfo = allQueues.find(q => q.name === sub.workQueue);
          userQueueMap.set(key, {
            username: sub.username,
            queueName: sub.workQueue,
            queueDisplayName: queueInfo?.displayName || sub.workQueue,
            tasks: [],
            taskCount: 0,
          });
        }
      }
      
      // Get tasks for each user-queue combination
      for (const [key, userQueue] of userQueueMap.entries()) {
        try {
          const tasksRes = await apiClient.get(`/tasks?username=${userQueue.username}`);
          const allUserTasks: Task[] = tasksRes.data || [];
          const queueTasks = allUserTasks.filter(t => t.workQueue === userQueue.queueName);
          userQueue.tasks = queueTasks;
          userQueue.taskCount = queueTasks.length;
        } catch (err) {
          console.log(`Error loading tasks for ${userQueue.username} in ${userQueue.queueName}`);
        }
      }
      
      setUserQueueData(Array.from(userQueueMap.values()));
    } catch (err: any) {
      console.error('Error loading workspace data:', err);
      setError(err?.response?.data?.error || err.message || 'Failed to load workspace data');
    }
  };

  const loadQueueTasks = async () => {
    if (!selectedQueue) return;
    try {
      const tasksRes = await apiClient.get(`/work-queues/${selectedQueue.name}/tasks`);
      setQueueTasks(tasksRes.data || []);
    } catch (err: any) {
      console.error('Error loading queue tasks:', err);
      setQueueTasks([]);
    }
  };

  const loadQueueSubscriptions = async () => {
    if (!selectedQueue) return;
    try {
      const subsRes = await apiClient.get(`/work-queues/queue/${selectedQueue.name}/details`);
      setQueueSubscriptions(subsRes.data || []);
    } catch (err: any) {
      console.error('Error loading queue subscriptions:', err);
      setQueueSubscriptions([]);
    }
  };

  const handleViewQueue = (queue: QueueInfo) => {
    setSelectedQueue(queue);
    setQueueView('queueTasks');
  };

  const handleReserveTasks = async (count?: number) => {
    if (!selectedQueue) return;
    
    try {
      const tasksToReserve = count 
        ? queueTasks.filter(t => t.status === 'OPEN').slice(0, count)
        : Array.from(selectedTasks).map(id => queueTasks.find(t => t.id === id)).filter(Boolean) as Task[];
      
      if (tasksToReserve.length === 0) {
        alert('No tasks selected or available to reserve');
        return;
      }
      
      for (const task of tasksToReserve) {
        await apiClient.put(`/tasks/${task.id}`, {
          ...task,
          assignedTo: user?.username,
          status: 'IN_PROGRESS'
        });
      }
      
      alert(`Reserved ${tasksToReserve.length} task(s) successfully!`);
      setSelectedTasks(new Set());
      loadQueueTasks();
    } catch (err: any) {
      console.error('Error reserving tasks:', err);
      alert('Failed to reserve tasks: ' + (err?.response?.data?.error || err.message));
    }
  };

  const handleForwardTasks = async () => {
    if (selectedTasks.size === 0) {
      alert('Please select tasks to forward');
      return;
    }
    
    const forwardTo = prompt('Enter username to forward tasks to:');
    if (!forwardTo) return;
    
    try {
      for (const taskId of selectedTasks) {
        const task = queueTasks.find(t => t.id === taskId);
        if (task) {
          await apiClient.put(`/tasks/${taskId}`, {
            ...task,
            assignedTo: forwardTo
          });
        }
      }
      
      alert(`Forwarded ${selectedTasks.size} task(s) to ${forwardTo}`);
      setSelectedTasks(new Set());
      loadQueueTasks();
    } catch (err: any) {
      console.error('Error forwarding tasks:', err);
      alert('Failed to forward tasks: ' + (err?.response?.data?.error || err.message));
    }
  };

  const handleAddSubscription = async () => {
    if (!selectedQueue || !selectedUserToAdd) {
      alert('Please select a user');
      return;
    }
    
    try {
      await apiClient.post('/work-queues/subscribe', {
        username: selectedUserToAdd,
        workQueue: selectedQueue.name,
        subscribedBy: user?.username,
      });
      
      alert('User added to queue successfully!');
      setShowAddSubscriptionModal(false);
      setSelectedUserToAdd('');
      loadQueueSubscriptions();
      if (activeTab === 'myWorkspace') {
        loadMyWorkspaceData();
      }
    } catch (err: any) {
      console.error('Error adding subscription:', err);
      alert('Failed to add user: ' + (err?.response?.data?.error || err.message));
    }
  };

  const handleRemoveSubscription = async (username: string) => {
    if (!selectedQueue) return;
    
    if (!confirm(`Remove ${username} from ${selectedQueue.displayName}?`)) {
      return;
    }
    
    try {
      await apiClient.delete('/work-queues/unsubscribe', {
        data: {
          username,
          workQueue: selectedQueue.name,
        },
      });
      
      alert('User removed from queue successfully!');
      loadQueueSubscriptions();
      if (activeTab === 'myWorkspace') {
        loadMyWorkspaceData();
      }
    } catch (err: any) {
      console.error('Error removing subscription:', err);
      alert('Failed to remove user: ' + (err?.response?.data?.error || err.message));
    }
  };

  const toggleTaskSelection = (taskId: number) => {
    const newSelected = new Set(selectedTasks);
    if (newSelected.has(taskId)) {
      newSelected.delete(taskId);
    } else {
      newSelected.add(taskId);
    }
    setSelectedTasks(newSelected);
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

  if (loading || authLoading || !user) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-white">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#1e3a8a] mx-auto"></div>
          <p className="mt-4 text-gray-600 font-medium">Loading Supervisor Dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white flex">
      {/* Left Sidebar */}
      <div className={`${sidebarCollapsed ? 'w-16' : 'w-64'} bg-[#1e3a8a] text-white transition-all duration-300 flex flex-col shadow-xl`}>
        <div className="p-4 flex items-center justify-between border-b border-[#1e40af]">
          <h2 className={`${sidebarCollapsed ? 'hidden' : 'block'} font-bold text-lg`}>SHORTCUTS</h2>
          <button
            onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
            className="text-white hover:text-gray-300 transition-colors"
          >
            {sidebarCollapsed ? '→' : '←'}
          </button>
        </div>
        
        <nav className="flex-1 px-4 py-4">
          <div className="space-y-1">
            <div 
              className={`py-2 px-3 cursor-pointer transition-colors ${
                activeTab === 'myWorkspace' ? 'bg-[#1e40af]' : 'hover:bg-[#1e40af]/50'
              }`}
              onClick={() => {
                setActiveTab('myWorkspace');
                setQueueView('myWorkspace' as ViewType);
              }}
            >
              <div className="flex items-center justify-between">
                <span className={sidebarCollapsed ? 'hidden' : 'block text-sm'}>My Workspace</span>
                {!sidebarCollapsed && <span className="text-xs">▼</span>}
              </div>
            </div>
            <div className="py-2 px-3 cursor-pointer hover:bg-[#1e40af]/50">
              <div className="flex items-center justify-between">
                <span className={sidebarCollapsed ? 'hidden' : 'block text-sm'}>Organization</span>
                {!sidebarCollapsed && <span className="text-xs">▼</span>}
              </div>
            </div>
            <div className="py-2 px-3 cursor-pointer hover:bg-[#1e40af]/50">
              <div className="flex items-center justify-between">
                <span className={sidebarCollapsed ? 'hidden' : 'block text-sm'}>User Search</span>
                {!sidebarCollapsed && <span className="text-xs">▼</span>}
              </div>
            </div>
            <div 
              className={`py-2 px-3 cursor-pointer transition-colors ${
                activeTab === 'workQueues' ? 'bg-[#1e40af]' : 'hover:bg-[#1e40af]/50'
              }`}
              onClick={() => {
                setActiveTab('workQueues');
                setQueueView('workQueues' as ViewType);
              }}
            >
              <div className="flex items-center justify-between">
                <span className={sidebarCollapsed ? 'hidden' : 'block text-sm'}>Work Queues</span>
                {!sidebarCollapsed && <span className="text-xs">▲</span>}
              </div>
              {!sidebarCollapsed && activeTab === 'workQueues' && (
                <div className="mt-1 ml-4 py-1 px-2 bg-[#1e40af]">
                  <span className="text-sm">Work Queues</span>
                </div>
              )}
            </div>
          </div>
        </nav>
      </div>

      {/* Main Content */}
      <div className="flex-1 flex flex-col">
        {/* Top Header */}
        <header className="bg-[#1e3a8a] text-white px-6 py-4 flex justify-between items-center shadow-lg">
          <h1 className="text-xl font-bold">CMIPSII Case Management Information Payroll System II</h1>
          <div className="flex items-center gap-4">
            <span className="font-medium">Welcome, {user?.username}</span>
            <button onClick={logout} className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors font-medium shadow-md">
              Logout
            </button>
          </div>
        </header>

        {/* Main Content Area */}
        <main className="flex-1 bg-white overflow-auto">
          {/* Top Tabs */}
          <div className="border-b border-gray-300 bg-gray-100 px-4">
            <div className="flex items-center gap-1">
              <button
                onClick={() => {
                  setActiveTab('myWorkspace');
                  setQueueView('myWorkspace' as ViewType);
                }}
                className={`px-4 py-2 text-sm font-medium border-t border-l border-r rounded-t ${
                  activeTab === 'myWorkspace'
                    ? 'bg-white border-gray-300 text-gray-900'
                    : 'bg-gray-200 border-gray-300 text-gray-600 hover:bg-gray-100'
                }`}
              >
                My Workspace
              </button>
              <button
                onClick={() => {
                  setActiveTab('workQueues');
                  setQueueView('workQueues' as ViewType);
                }}
                className={`px-4 py-2 text-sm font-medium border-t border-l border-r rounded-t relative ${
                  activeTab === 'workQueues'
                    ? 'bg-white border-gray-300 text-gray-900'
                    : 'bg-gray-200 border-gray-300 text-gray-600 hover:bg-gray-100'
                }`}
              >
                Work Queues
                {activeTab === 'workQueues' && (
                  <span className="ml-2 text-gray-500 text-xs">×</span>
                )}
              </button>
            </div>
          </div>
          
          <div className="p-6">
          {activeTab === 'myWorkspace' && (
            <div className="space-y-6">
              {/* Header */}
              <div className="flex justify-between items-center">
                <div>
                  <h2 className="text-3xl font-bold text-gray-900">My Workspace</h2>
                  <p className="text-gray-600 mt-1">Overview of users, queues, and tasks</p>
                </div>
                <button
                  onClick={loadMyWorkspaceData}
                  className="px-5 py-2.5 bg-[#1e3a8a] text-white rounded-lg hover:bg-[#1e40af] transition-colors font-medium shadow-md flex items-center gap-2"
                >
                  <span>🔄</span> Refresh
                </button>
              </div>

              {error && (
                <div className="bg-red-50 border-l-4 border-red-500 text-red-700 px-4 py-3 rounded-md shadow-sm">
                  {error}
                </div>
              )}

              {/* Escalated Tasks Section */}
              <div className="bg-white rounded-xl shadow-lg border border-gray-200 overflow-hidden">
                <div className="bg-[#1e3a8a] px-6 py-4">
                  <h3 className="text-xl font-bold text-white flex items-center gap-2">
                    <span>⚠️</span> Escalated Tasks (Supervisor Only)
                  </h3>
                  <p className="text-gray-200 text-sm mt-1">Tasks requiring immediate supervisor attention</p>
                </div>
                <div className="p-6">
                  {escalatedTasks.length === 0 ? (
                    <p className="text-gray-500 text-center py-8">No escalated tasks at this time</p>
                  ) : (
                    <div className="space-y-3">
                      {escalatedTasks.map((task) => (
                        <div
                          key={task.id}
                          className="border border-gray-300 rounded-lg p-4 hover:border-gray-400 transition-all bg-white"
                        >
                          <div className="flex justify-between items-start mb-2">
                            <h4 className="font-semibold text-gray-900">{task.title}</h4>
                            <div className="flex gap-2">
                              <span
                                className="px-3 py-1 rounded-full text-xs font-semibold text-white"
                                style={{ backgroundColor: getStatusColor(task.status) }}
                              >
                                {task.status}
                              </span>
                              <span
                                className="px-3 py-1 rounded-full text-xs font-semibold text-white"
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
                            <span>👤 Assigned: {task.assignedTo || 'Unassigned'}</span>
                            {task.dueDate && (
                              <span>📅 Due: {new Date(task.dueDate).toLocaleDateString()}</span>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>

              {/* Users by Queue Section */}
              <div className="bg-white rounded-xl shadow-lg border border-gray-200 overflow-hidden">
                <div className="bg-[#1e3a8a] px-6 py-4">
                  <h3 className="text-xl font-bold text-white flex items-center gap-2">
                    <span>👥</span> Users by Work Queue
                  </h3>
                  <p className="text-gray-200 text-sm mt-1">View which users are in which queues and their active tasks</p>
                </div>
                <div className="p-6">
                  {userQueueData.length === 0 ? (
                    <p className="text-gray-500 text-center py-8">No users assigned to work queues yet</p>
                  ) : (
                    <div className="space-y-6">
                      {userQueueData.map((userQueue, idx) => (
                        <div
                          key={`${userQueue.username}_${userQueue.queueName}_${idx}`}
                          className="border border-gray-300 rounded-lg p-5 hover:border-gray-400 transition-all bg-white"
                        >
                          <div className="flex justify-between items-start mb-4">
                            <div>
                              <h4 className="text-lg font-bold text-gray-900">
                                👤 {userQueue.username}
                              </h4>
                              <p className="text-sm text-gray-700 font-medium mt-1">
                                📋 Queue: {userQueue.queueDisplayName}
                              </p>
                            </div>
                            <div className="bg-[#1e3a8a] text-white px-4 py-2 rounded-lg font-bold">
                              {userQueue.taskCount} Task{userQueue.taskCount !== 1 ? 's' : ''}
                            </div>
                          </div>
                          
                          {userQueue.tasks.length === 0 ? (
                            <p className="text-gray-500 text-sm">No active tasks</p>
                          ) : (
                            <div className="space-y-2 mt-4">
                              {userQueue.tasks.map((task) => (
                                <div
                                  key={task.id}
                                  className="bg-white border border-gray-200 rounded-lg p-3 hover:shadow-md transition-shadow"
                                >
                                  <div className="flex justify-between items-start">
                                    <div className="flex-1">
                                      <h5 className="font-semibold text-gray-900 text-sm">{task.title}</h5>
                                      {task.description && (
                                        <p className="text-xs text-gray-600 mt-1">{task.description}</p>
                                      )}
                                    </div>
                                    <div className="flex gap-2 ml-4">
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
                                  {task.dueDate && (
                                    <p className="text-xs text-gray-500 mt-2">
                                      📅 Due: {new Date(task.dueDate).toLocaleDateString()}
                                    </p>
                                  )}
                                </div>
                              ))}
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {activeTab === 'workQueues' && (
            <>
              {queueView === 'workQueues' && (
                <div>
                  <div className="flex justify-between items-center mb-6">
                    <div>
                      <h2 className="text-3xl font-bold text-gray-900">Work Queues</h2>
                      <p className="text-gray-600 mt-1">Manage work queues and subscriptions</p>
                    </div>
                    <button
                      onClick={loadDashboardData}
                      className="px-5 py-2.5 bg-[#1e3a8a] text-white rounded-lg hover:bg-[#1e40af] transition-colors font-medium shadow-md flex items-center gap-2"
                    >
                      <span>🔄</span> Refresh
                    </button>
                  </div>

                  {error && (
                    <div className="bg-red-50 border-l-4 border-red-500 text-red-700 px-4 py-3 rounded-md shadow-sm mb-6">
                      {error}
                    </div>
                  )}

                  {/* Work Queues Table */}
                  <div className="bg-white border border-gray-300 overflow-hidden">
                    <table className="min-w-full divide-y divide-gray-200">
                      <thead className="bg-gray-50">
                        <tr>
                          <th className="px-4 py-3 text-left text-xs font-semibold text-gray-700 uppercase border-b border-gray-300">Action</th>
                          <th className="px-4 py-3 text-left text-xs font-semibold text-gray-700 uppercase border-b border-gray-300">Name</th>
                          <th className="px-4 py-3 text-left text-xs font-semibold text-gray-700 uppercase border-b border-gray-300">Administrator</th>
                          <th className="px-4 py-3 text-left text-xs font-semibold text-gray-700 uppercase border-b border-gray-300">User Subscription</th>
                        </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-gray-200">
                        {queues.map((queue) => (
                          <tr key={queue.name} className="hover:bg-gray-50">
                            <td className="px-4 py-3 whitespace-nowrap">
                              <button
                                onClick={() => handleViewQueue(queue)}
                                className="text-[#1e3a8a] hover:text-[#1e40af] text-sm font-medium hover:underline"
                              >
                                View
                              </button>
                            </td>
                            <td className="px-4 py-3 whitespace-nowrap">
                              <div className="text-sm text-gray-900">{queue.displayName}</div>
                            </td>
                            <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-700">
                              ADMIN
                            </td>
                            <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-700">
                              Yes
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              {queueView === 'queueTasks' && selectedQueue && (
                <div>
                  <div className="flex justify-between items-center mb-6">
                    <div>
                      <h2 className="text-3xl font-bold text-gray-900">
                        Work Queue Tasks: {selectedQueue.displayName}
                      </h2>
                      <p className="text-gray-600 mt-1">{selectedQueue.description}</p>
                    </div>
                    <button
                      onClick={() => setQueueView('workQueues' as ViewType)}
                      className="px-5 py-2.5 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors font-medium shadow-md"
                    >
                      ← Back to Queues
                    </button>
                  </div>

                  {/* Action Buttons */}
                  <div className="bg-white rounded-xl shadow-lg border border-gray-200 p-6 mb-6">
                    <div className="flex gap-3 flex-wrap">
                      <button
                        onClick={() => handleReserveTasks(5)}
                        className="px-5 py-2.5 bg-[#1e3a8a] text-white rounded-lg hover:bg-[#1e40af] transition-colors font-medium shadow-md"
                      >
                        Reserve Next 5 Tasks...
                      </button>
                      <button
                        onClick={() => handleReserveTasks(20)}
                        className="px-5 py-2.5 bg-[#1e3a8a] text-white rounded-lg hover:bg-[#1e40af] transition-colors font-medium shadow-md"
                      >
                        Reserve Next 20 Tasks...
                      </button>
                      <button
                        onClick={() => handleReserveTasks()}
                        className="px-5 py-2.5 bg-[#1e3a8a] text-white rounded-lg hover:bg-[#1e40af] transition-colors font-medium shadow-md disabled:bg-gray-400 disabled:cursor-not-allowed"
                        disabled={selectedTasks.size === 0}
                      >
                        Reserve Selected Tasks...
                      </button>
                      <button
                        onClick={handleForwardTasks}
                        className="px-5 py-2.5 bg-[#1e3a8a] text-white rounded-lg hover:bg-[#1e40af] transition-colors font-medium shadow-md disabled:bg-gray-400 disabled:cursor-not-allowed"
                        disabled={selectedTasks.size === 0}
                      >
                        Forward Selected Tasks...
                      </button>
                    </div>
                  </div>

                  {/* Tasks Table */}
                  <div className="bg-white rounded-xl shadow-lg border border-gray-200 overflow-hidden">
                    <div className="px-6 py-4 bg-[#1e3a8a]">
                      <h3 className="text-lg font-bold text-white">Total Tasks: {queueTasks.length}</h3>
                    </div>
                    <table className="min-w-full divide-y divide-gray-200">
                      <thead className="bg-gray-50">
                        <tr>
                          <th className="px-6 py-3 text-left text-xs font-bold text-gray-700 uppercase tracking-wider">Action</th>
                          <th className="px-6 py-3 text-left text-xs font-bold text-gray-700 uppercase tracking-wider">Task ID</th>
                          <th className="px-6 py-3 text-left text-xs font-bold text-gray-700 uppercase tracking-wider">Subject</th>
                          <th className="px-6 py-3 text-left text-xs font-bold text-gray-700 uppercase tracking-wider">Priority</th>
                          <th className="px-6 py-3 text-left text-xs font-bold text-gray-700 uppercase tracking-wider">Status</th>
                          <th className="px-6 py-3 text-left text-xs font-bold text-gray-700 uppercase tracking-wider">Deadline</th>
                        </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-gray-200">
                        {queueTasks.length === 0 ? (
                          <tr>
                            <td colSpan={6} className="px-6 py-8 text-center text-gray-500">
                              No tasks found in this queue
                            </td>
                          </tr>
                        ) : (
                          queueTasks.map((task) => (
                            <tr key={task.id} className="hover:bg-gray-50 transition-colors">
                              <td className="px-6 py-4 whitespace-nowrap">
                                <input
                                  type="checkbox"
                                  checked={selectedTasks.has(task.id)}
                                  onChange={() => toggleTaskSelection(task.id)}
                                  className="mr-2 w-4 h-4 text-[#1e3a8a]"
                                />
                                <button className="text-[#1e3a8a] hover:text-[#1e40af] font-semibold hover:underline">
                                  Reserve...
                                </button>
                              </td>
                              <td className="px-6 py-4 whitespace-nowrap">
                                <button className="text-[#1e3a8a] hover:text-[#1e40af] font-semibold hover:underline">
                                  {task.id}
                                </button>
                              </td>
                              <td className="px-6 py-4 text-sm font-medium text-gray-900">{task.title}</td>
                              <td className="px-6 py-4 whitespace-nowrap">
                                <span
                                  className="px-3 py-1 rounded-full text-xs font-semibold text-white"
                                  style={{ backgroundColor: getPriorityColor(task.priority) }}
                                >
                                  {task.priority}
                                </span>
                              </td>
                              <td className="px-6 py-4 whitespace-nowrap">
                                <span
                                  className="px-3 py-1 rounded-full text-xs font-semibold text-white"
                                  style={{ backgroundColor: getStatusColor(task.status) }}
                                >
                                  {task.status}
                                </span>
                              </td>
                              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-700">
                                {task.dueDate ? new Date(task.dueDate).toLocaleDateString() : '-'}
                              </td>
                            </tr>
                          ))
                        )}
                      </tbody>
                    </table>
                  </div>

                  {/* View Tabs */}
                  <div className="mt-4 border-b border-gray-300">
                    <nav className="flex">
                      <button
                        onClick={() => setQueueView('queueTasks' as ViewType)}
                        className={`px-4 py-2 text-sm font-medium border-t border-l border-r rounded-t ${
                          (queueView as string) === 'queueTasks'
                            ? 'bg-white border-gray-300 text-gray-900'
                            : 'bg-gray-100 border-gray-300 text-gray-600 hover:bg-gray-50'
                        }`}
                      >
                        View Work Queue
                      </button>
                      <button
                        onClick={() => setQueueView('queueSubscriptions' as ViewType)}
                        className={`px-4 py-2 text-sm font-medium border-t border-l border-r rounded-t ${
                          (queueView as string) === 'queueSubscriptions'
                            ? 'bg-white border-gray-300 text-gray-900'
                            : 'bg-gray-100 border-gray-300 text-gray-600 hover:bg-gray-50'
                        }`}
                      >
                        Work Queues Subscriptions
                      </button>
                    </nav>
                  </div>
                </div>
              )}

              {queueView === 'queueSubscriptions' && selectedQueue && (
                <div>
                  <div className="mb-4">
                    <h2 className="text-xl font-semibold text-gray-900 mb-2">
                      Work Queue Subscriptions: {selectedQueue.displayName}
                    </h2>
                  </div>

                  {/* View Tabs */}
                  <div className="mb-4 border-b border-gray-300">
                    <nav className="flex">
                      <button
                        onClick={() => setQueueView('queueTasks' as ViewType)}
                        className={`px-4 py-2 text-sm font-medium border-t border-l border-r rounded-t ${
                          (queueView as string) === 'queueTasks'
                            ? 'bg-white border-gray-300 text-gray-900'
                            : 'bg-gray-100 border-gray-300 text-gray-600 hover:bg-gray-50'
                        }`}
                      >
                        View Work Queue
                      </button>
                      <button
                        onClick={() => setQueueView('queueSubscriptions' as ViewType)}
                        className={`px-4 py-2 text-sm font-medium border-t border-l border-r rounded-t ${
                          (queueView as string) === 'queueSubscriptions'
                            ? 'bg-white border-gray-300 text-gray-900'
                            : 'bg-gray-100 border-gray-300 text-gray-600 hover:bg-gray-50'
                        }`}
                      >
                        Work Queues Subscriptions
                      </button>
                    </nav>
                  </div>

                  <div className="mb-4">
                    <button
                      onClick={() => setShowAddSubscriptionModal(true)}
                      className="px-4 py-2 bg-[#1e3a8a] text-white text-sm font-medium hover:bg-[#1e40af]"
                    >
                      New...
                    </button>
                  </div>

                  {/* Subscriptions Table */}
                  <div className="bg-white border border-gray-300 overflow-hidden">
                    <table className="min-w-full divide-y divide-gray-200">
                      <thead className="bg-gray-50">
                        <tr>
                          <th className="px-4 py-3 text-left text-xs font-semibold text-gray-700 uppercase border-b border-gray-300">Action</th>
                          <th className="px-4 py-3 text-left text-xs font-semibold text-gray-700 uppercase border-b border-gray-300">Name</th>
                          <th className="px-4 py-3 text-left text-xs font-semibold text-gray-700 uppercase border-b border-gray-300">
                            Subscription Date
                            <span className="ml-1 text-gray-400">▼</span>
                          </th>
                        </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-gray-200">
                        {queueSubscriptions.length === 0 ? (
                          <tr>
                            <td colSpan={3} className="px-4 py-8 text-center text-gray-500">
                              No users subscribed to this queue
                            </td>
                          </tr>
                        ) : (
                          queueSubscriptions.map((sub) => (
                            <tr key={sub.id} className="hover:bg-gray-50">
                              <td className="px-4 py-3 whitespace-nowrap">
                                <button
                                  onClick={() => handleRemoveSubscription(sub.username)}
                                  className="text-[#1e3a8a] hover:text-[#1e40af] text-sm font-medium hover:underline"
                                >
                                  Remove...
                                </button>
                              </td>
                              <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-900">
                                {sub.username}
                              </td>
                              <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-700">
                                {new Date(sub.createdAt).toLocaleDateString('en-US', { 
                                  year: 'numeric', 
                                  month: '2-digit', 
                                  day: '2-digit',
                                  hour: '2-digit',
                                  minute: '2-digit'
                                })}
                              </td>
                            </tr>
                          ))
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </>
          )}
          </div>
        </main>
      </div>

      {/* Add Subscription Modal */}
      {showAddSubscriptionModal && selectedQueue && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
          onClick={() => setShowAddSubscriptionModal(false)}
        >
          <div
            className="bg-white rounded-xl w-full max-w-lg shadow-2xl"
            onClick={(e) => e.stopPropagation()}
          >
            {/* Modal Header */}
            <div className="flex justify-between items-center px-6 py-4 border-b border-gray-200 bg-[#1e3a8a] rounded-t-xl">
              <h3 className="text-lg font-bold text-white">
                Add Work Queue Subscription: {selectedQueue.displayName}
              </h3>
              <button
                onClick={() => setShowAddSubscriptionModal(false)}
                className="text-white hover:text-gray-200 text-2xl font-bold leading-none w-8 h-8 flex items-center justify-center rounded hover:bg-[#1e40af] transition-colors"
                aria-label="Close"
              >
                ×
              </button>
            </div>
            
            {/* Modal Body */}
            <div className="px-6 py-6 space-y-6">
              <div>
                <label className="block text-sm font-bold text-gray-900 mb-2">
                  User: <span className="text-red-500">*</span>
                </label>
                <select
                  value={selectedUserToAdd}
                  onChange={(e) => setSelectedUserToAdd(e.target.value)}
                  className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#1e3a8a] focus:border-[#1e3a8a] text-gray-900 bg-white text-sm font-medium"
                >
                  <option value="" className="text-gray-500">-- Select User --</option>
                  {allUsers.map((u) => (
                    <option key={u.id} value={u.username} className="text-gray-900">
                      {u.username} {u.firstName && u.lastName && `(${u.firstName} ${u.lastName})`}
                    </option>
                  ))}
                </select>
                <p className="mt-2 text-xs text-gray-500">
                  <span className="text-red-500">*</span> required field
                </p>
              </div>
            </div>
            
            {/* Modal Footer */}
            <div className="flex gap-3 justify-end px-6 py-4 border-t border-gray-200 bg-gray-50 rounded-b-xl">
              <button
                onClick={() => {
                  setShowAddSubscriptionModal(false);
                  setSelectedUserToAdd('');
                }}
                className="px-6 py-2.5 bg-white border-2 border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 hover:border-gray-400 font-medium text-sm transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleAddSubscription}
                disabled={!selectedUserToAdd}
                className="px-6 py-2.5 bg-[#1e3a8a] text-white rounded-lg hover:bg-[#1e40af] disabled:bg-gray-400 disabled:cursor-not-allowed font-medium text-sm transition-colors shadow-md"
              >
                Save
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default dynamic(() => Promise.resolve(SupervisorDashboardComponent), { ssr: false });

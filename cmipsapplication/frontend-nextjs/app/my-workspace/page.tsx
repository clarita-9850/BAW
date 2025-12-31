'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useTranslation } from 'react-i18next';
import Breadcrumb from '@/components/Breadcrumb';
import { useAuth } from '@/contexts/AuthContext';
import styles from './my-workspace.module.css';
import Link from 'next/link';

interface Task {
  id: number;
  title: string;
  subject: string;
  due_date: string;
  priority: string;
  status: string;
}

export default function MyWorkspacePage() {
  const { t } = useTranslation();
  const router = useRouter();
  const { isAuthenticated, loading: authLoading } = useAuth();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showAllTasks, setShowAllTasks] = useState(false);
  const [showMarkdownEditor, setShowMarkdownEditor] = useState(false);
  const initialTaskCount = 5;

  const displayedTasks = showAllTasks ? tasks : tasks.slice(0, initialTaskCount);
  const hasMoreTasks = tasks.length > initialTaskCount;

  const fetchTasks = async () => {
    try {
      setLoading(true);
      const { taskService } = await import('@/lib/services/task.service');
      const tasksData = await taskService.getAllTasks();
      setTasks(tasksData);
    } catch (err) {
      setError('Network error while fetching tasks');
      console.error('Error fetching tasks:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.replace('/login');
    }
  }, [authLoading, isAuthenticated, router]);

  useEffect(() => {
    if (isAuthenticated) {
      fetchTasks();
    }
  }, [isAuthenticated]);

  const getPriorityClass = (priority: string) => {
    switch (priority) {
      case 'urgent':
        return styles.priorityUrgent;
      case 'high':
        return styles.priorityHigh;
      case 'normal':
        return styles.priorityNormal;
      case 'low':
        return styles.priorityLow;
      default:
        return styles.priorityNormal;
    }
  };

  const getStatusClass = (status: string) => {
    switch (status) {
      case 'completed':
        return styles.statusCompleted;
      case 'in_progress':
        return styles.statusInProgress;
      case 'pending':
        return styles.statusPending;
      case 'cancelled':
        return styles.statusCancelled;
      default:
        return styles.statusPending;
    }
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return 'No due date';
    const date = new Date(dateString);
    return date.toLocaleDateString();
  };

  if (authLoading || !isAuthenticated) {
    return (
      <div className="container py-5 text-center">
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <div className={styles.pageContainer}>
      <div className="container">
        <Breadcrumb path={['Home']} currentPage="My Workspace" />

        <div className={styles.workspaceContent}>
          <div className={styles.workspaceHeader}>
            <div className={styles.workspaceTitle}>
              <h1>{t('myWorkspace.title')}</h1>
              <p>{t('myWorkspace.subtitle')}</p>
            </div>
            <button className={styles.helpBtn} title={t('myWorkspace.helpButton')}>
              ?
            </button>
          </div>

          <div className={styles.workspaceGrid}>
            {/* My Tasks Section */}
            <div className={styles.tasksSection}>
              <div className={styles.tasksHeader}>
                <h2>{t('myWorkspace.tasks.title')}</h2>
                <div className={styles.headerControls}>
                  <button
                    className={styles.markdownIconBtn}
                    title={t('myWorkspace.tasks.markdownEditor')}
                    onClick={() => setShowMarkdownEditor(!showMarkdownEditor)}
                  >
                    {showMarkdownEditor ? '📝' : '✏️'}
                  </button>
                  <button className={styles.refreshBtn} title="Refresh tasks" onClick={fetchTasks}>
                    🔄
                  </button>
                </div>
              </div>

              <div className={styles.tasksContainer}>
                {/* Markdown Editor (Conditional) */}
                {showMarkdownEditor && (
                  <div className={styles.markdownEditorSection}>
                    <h3>{t('myWorkspace.tasks.taskEditor')}</h3>
                    <div className={styles.markdownEditor}>
                      <textarea
                        placeholder={t('myWorkspace.tasks.taskEditorPlaceholder')}
                        className={styles.markdownTextarea}
                      ></textarea>
                      <div className={styles.editorToolbar}>
                        <button className={styles.toolbarBtn}>{t('myWorkspace.tasks.toolbar.bold')}</button>
                        <button className={styles.toolbarBtn}>{t('myWorkspace.tasks.toolbar.italic')}</button>
                        <button className={styles.toolbarBtn}>{t('myWorkspace.tasks.toolbar.list')}</button>
                        <button className={styles.toolbarBtn}>{t('myWorkspace.tasks.toolbar.link')}</button>
                      </div>
                    </div>
                  </div>
                )}

                {/* Tasks Data Table */}
                <div className={styles.tasksTableSection}>
                  <h3>{t('myWorkspace.tasks.taskList')}</h3>

                  {loading ? (
                    <div className={styles.loading}>Loading tasks...</div>
                  ) : error ? (
                    <div className={styles.error}>Error: {error}</div>
                  ) : tasks.length === 0 ? (
                    <div className={styles.noTasks}>No tasks found.</div>
                  ) : (
                    <>
                      <div className={styles.tableContainer}>
                        <table className={styles.tasksTable}>
                          <thead>
                            <tr>
                              <th>{t('myWorkspace.tasks.columns.task')}</th>
                              <th>{t('myWorkspace.tasks.columns.subject')}</th>
                              <th>{t('myWorkspace.tasks.columns.dueDate')}</th>
                              <th>{t('myWorkspace.tasks.columns.priority')}</th>
                              <th>Status</th>
                            </tr>
                          </thead>
                          <tbody>
                            {displayedTasks.map((task) => (
                              <tr key={task.id}>
                                <td>{task.title}</td>
                                <td>{task.subject}</td>
                                <td>{formatDate(task.due_date)}</td>
                                <td>
                                  <span className={`${styles.priorityBadge} ${getPriorityClass(task.priority)}`}>
                                    {task.priority}
                                  </span>
                                </td>
                                <td>
                                  <span className={`${styles.statusBadge} ${getStatusClass(task.status)}`}>
                                    {task.status.replace('_', ' ')}
                                  </span>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>

                      {/* Show More/Less Button */}
                      {hasMoreTasks && (
                        <div className={styles.tasksToggleContainer}>
                          <button className={styles.tasksToggleBtn} onClick={() => setShowAllTasks(!showAllTasks)}>
                            {showAllTasks ? 'Show Less' : `Show ${tasks.length - initialTaskCount} More Tasks`}
                          </button>
                        </div>
                      )}
                    </>
                  )}
                </div>
              </div>
            </div>

            {/* My Shortcuts Section */}
            <div className={styles.shortcutsSection}>
              <div className={styles.shortcutsHeader}>
                <h2>{t('myWorkspace.shortcuts.title')}</h2>
              </div>

              <div className={styles.shortcutsContainer}>
                <div className={styles.shortcutsList}>
                  <Link href="#" className={styles.shortcutLink}>
                    <span className={styles.shortcutIcon}>📋</span>
                    <span className={styles.shortcutText}>{t('myWorkspace.shortcuts.newReferral')}</span>
                  </Link>
                  <Link href="/new-application" className={styles.shortcutLink}>
                    <span className={styles.shortcutIcon}>📝</span>
                    <span className={styles.shortcutText}>{t('myWorkspace.shortcuts.newApplication')}</span>
                  </Link>
                  <Link href="#" className={styles.shortcutLink}>
                    <span className={styles.shortcutIcon}>👤</span>
                    <span className={styles.shortcutText}>{t('myWorkspace.shortcuts.findPerson')}</span>
                  </Link>
                  <Link href="#" className={styles.shortcutLink}>
                    <span className={styles.shortcutIcon}>⚖️</span>
                    <span className={styles.shortcutText}>{t('myWorkspace.shortcuts.findHearingCase')}</span>
                  </Link>
                  <Link href="#" className={styles.shortcutLink}>
                    <span className={styles.shortcutIcon}>🏥</span>
                    <span className={styles.shortcutText}>{t('myWorkspace.shortcuts.registerProvider')}</span>
                  </Link>
                  <Link href="#" className={styles.shortcutLink}>
                    <span className={styles.shortcutIcon}>🔄</span>
                    <span className={styles.shortcutText}>{t('myWorkspace.shortcuts.mergeSSN')}</span>
                  </Link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}


import apiClient from '../api';

export interface Task {
  id: number;
  title: string;
  description?: string;
  subject: string;
  due_date: string;
  priority: 'urgent' | 'high' | 'normal' | 'low';
  status: 'completed' | 'in_progress' | 'pending' | 'cancelled';
  tags?: string;
  created_at?: string;
}

export interface TasksResponse {
  tasks: Task[];
}

export const taskService = {
  /**
   * Get all tasks
   */
  async getAllTasks(): Promise<Task[]> {
    const response = await apiClient.get<TasksResponse>('/tasks');
    return response.data.tasks || [];
  },

  /**
   * Get task by ID
   */
  async getTaskById(taskId: number): Promise<Task> {
    const response = await apiClient.get<Task>(`/tasks/${taskId}`);
    return response.data;
  },

  /**
   * Create a new task
   */
  async createTask(task: Partial<Task>): Promise<Task> {
    const response = await apiClient.post<Task>('/tasks', task);
    return response.data;
  },

  /**
   * Update a task
   */
  async updateTask(taskId: number, task: Partial<Task>): Promise<Task> {
    const response = await apiClient.put<Task>(`/tasks/${taskId}`, task);
    return response.data;
  },

  /**
   * Delete a task
   */
  async deleteTask(taskId: number): Promise<void> {
    await apiClient.delete(`/tasks/${taskId}`);
  },
};


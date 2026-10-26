/**
 * Unified API Client for CareerForge Spring Boot Backend
 * Incorporates JWT Bearer injection, error standardization, and rate limiting resilience.
 */

const API_BASE = '/api/v1';

class ApiError extends Error {
  constructor(message, status = 500, retryAfterSeconds = null) {
    super(message);
    this.status = status;
    this.retryAfterSeconds = retryAfterSeconds;
  }
}

export const getToken = () => localStorage.getItem('cf_token');
export const setAuthData = (token, user) => {
  localStorage.setItem('cf_token', token);
  localStorage.setItem('cf_user', JSON.stringify(user));
};
export const clearAuthData = () => {
  localStorage.removeItem('cf_token');
  localStorage.removeItem('cf_user');
};
export const getStoredUser = () => {
  const user = localStorage.getItem('cf_user');
  return user ? JSON.parse(user) : null;
};

async function request(endpoint, options = {}) {
  const token = getToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });

  // Handle 429 Rate Limiting
  if (response.status === 429) {
    const retryAfter = response.headers.get('X-Rate-Limit-Retry-After-Seconds') || 'a few';
    throw new ApiError(`Rate limit exceeded. Please retry after ${retryAfter} seconds.`, 429, retryAfter);
  }

  // Handle 401 Unauthorized
  if (response.status === 401 && !endpoint.includes('/auth/login')) {
    clearAuthData();
    window.dispatchEvent(new Event('auth:unauthorized'));
    throw new ApiError('Session expired. Please log in again.', 401);
  }

  let body;
  try {
    body = await response.json();
  } catch (err) {
    body = null;
  }

  if (!response.ok) {
    const message = body?.message || `Request failed with status ${response.status}`;
    throw new ApiError(message, response.status);
  }

  return body?.data !== undefined ? body.data : body;
}

// 1. Authentication APIs
export const authApi = {
  login: async (credentials) => {
    const data = await request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });
    setAuthData(data.token, {
      userId: data.userId,
      name: data.name,
      email: data.email,
      role: data.role,
    });
    return data;
  },

  register: async (userData) => {
    const data = await request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(userData),
    });
    setAuthData(data.token, {
      userId: data.userId,
      name: data.name,
      email: data.email,
      role: data.role,
    });
    return data;
  },

  me: () => request('/auth/me'),
  logout: () => {
    clearAuthData();
  },
};

// 2. Jobs & Skills APIs
export const jobsApi = {
  getAll: (params = {}) => {
    const query = new URLSearchParams();
    if (params.page !== undefined) query.set('page', params.page);
    if (params.size !== undefined) query.set('size', params.size);
    if (params.search) query.set('search', params.search);
    if (params.status) query.set('status', params.status);
    const qs = query.toString();
    return request(`/jobs${qs ? `?${qs}` : ''}`);
  },

  getById: (id) => request(`/jobs/${id}`),

  create: (jobData) => request('/jobs', {
    method: 'POST',
    body: JSON.stringify(jobData),
  }),

  update: (id, jobData) => request(`/jobs/${id}`, {
    method: 'PUT',
    body: JSON.stringify(jobData),
  }),

  delete: (id) => request(`/jobs/${id}`, {
    method: 'DELETE',
  }),

  getSkills: () => request('/skills'),
  getLocations: () => request('/locations'),
  getCompanies: () => request('/companies'),
};

// 3. Profiles APIs
export const profileApi = {
  getCandidateProfile: () => request('/candidate/profile'),
  updateCandidateProfile: (profileData) => request('/candidate/profile', {
    method: 'PUT',
    body: JSON.stringify(profileData),
  }),

  getRecruiterProfile: () => request('/recruiter/profile'),
  updateRecruiterProfile: (profileData) => request('/recruiter/profile', {
    method: 'PUT',
    body: JSON.stringify(profileData),
  }),
};

// 4. Resume Storage & PDFBox APIs
export const resumeApi = {
  upload: async (file, analyzeWithAi = true) => {
    const token = getToken();
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch(`${API_BASE}/candidate/resume/upload?analyzeWithAi=${analyzeWithAi}`, {
      method: 'POST',
      headers: {
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: formData,
    });

    if (!response.ok) {
      const err = await response.json().catch(() => ({}));
      throw new ApiError(err.message || 'Resume upload failed', response.status);
    }

    const json = await response.json();
    return json.data;
  },

  downloadUrl: (fileName) => `${API_BASE}/candidate/resume/download/${fileName}`,
};

// 5. AI Matching Engine APIs
export const matchingApi = {
  getMatchForJob: (jobId) => request(`/jobs/${jobId}/match`),
  getRecommendedJobs: (limit = 10) => request(`/candidate/job-matches?limit=${limit}`),
  getTopCandidatesForJob: (jobId, limit = 10) => request(`/recruiter/jobs/${jobId}/candidate-matches?limit=${limit}`),
};

// 6. Applications & Saved Jobs APIs
export const applicationApi = {
  apply: (jobId, notes = '', resumeId = null) => request(`/jobs/${jobId}/apply`, {
    method: 'POST',
    body: JSON.stringify({ notes, resumeId }),
  }),

  getMyApplications: (page = 0, size = 10, status = '') => {
    const q = new URLSearchParams({ page, size });
    if (status) q.append('status', status);
    return request(`/candidate/applications?${q.toString()}`);
  },

  getJobApplications: (jobId, page = 0, size = 10, status = '') => {
    const q = new URLSearchParams({ page, size });
    if (status) q.append('status', status);
    return request(`/recruiter/jobs/${jobId}/applications?${q.toString()}`);
  },

  getAllRecruiterApplications: (page = 0, size = 10, status = '') => {
    const q = new URLSearchParams({ page, size });
    if (status) q.append('status', status);
    return request(`/recruiter/applications?${q.toString()}`);
  },

  updateStatus: (appId, status, notes = '') => request(`/recruiter/applications/${appId}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status, notes }),
  }),

  saveJob: (jobId) => request(`/jobs/${jobId}/save`, { method: 'POST' }),
  unsaveJob: (jobId) => request(`/jobs/${jobId}/save`, { method: 'DELETE' }),
  getSavedJobs: (page = 0, size = 10) => request(`/candidate/saved-jobs?page=${page}&size=${size}`),
  isJobSaved: (jobId) => request(`/jobs/${jobId}/is-saved`),
};

// 7. Notification APIs
export const notificationApi = {
  getNotifications: (page = 0, size = 10) => request(`/notifications?page=${page}&size=${size}`),
  getUnreadCount: () => request('/notifications/unread-count'),
  markAsRead: (id) => request(`/notifications/${id}/read`, { method: 'PATCH' }),
  markAllAsRead: () => request('/notifications/read-all', { method: 'PATCH' }),
  testPush: (title, message) => request(`/notifications/test-push?title=${encodeURIComponent(title)}&message=${encodeURIComponent(message)}`, { method: 'POST' }),
};

// 8. Recruitment Analytics APIs
export const analyticsApi = {
  getPlatformKpis: () => request('/analytics/kpis'),
  getSkillDemandSupply: () => request('/analytics/skills/demand-supply'),
};

// 9. Gemini Generative AI APIs
export const aiApi = {
  generateJobDescription: (data) => request('/ai/jobs/generate-description', {
    method: 'POST',
    body: JSON.stringify(data),
  }),

  analyzeResume: (data) => request('/ai/resume/analyze', {
    method: 'POST',
    body: JSON.stringify(data),
  }),
};


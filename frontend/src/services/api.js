import axios from 'axios';

// 1. Axios Instance Configuration
const API_BASE = '/api/v1';

export const apiClient = axios.create({
  baseURL: API_BASE,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Helper to access token and user state
const TOKEN_KEY = 'careerforge_token';
const USER_KEY = 'careerforge_user';

export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const setToken = (token) => {
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
};

export const getStoredUser = () => {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
};

export const setStoredUser = (user) => {
  if (user) localStorage.setItem(USER_KEY, JSON.stringify(user));
  else localStorage.removeItem(USER_KEY);
};

export const clearAuthData = () => {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
};

// 2. Request Interceptor: Attach JWT Bearer Token
apiClient.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 3. Response Interceptor: Extract Data, Handle 401 & 429
apiClient.interceptors.response.use(
  (response) => {
    // If backend returns standard ApiResponse { success, message, data }
    if (response.data && response.data.data !== undefined) {
      return response.data.data;
    }
    return response.data;
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response;
      
      // 401 Unauthorized: Clear stale token & notify app
      if (status === 401) {
        setToken(null);
        window.dispatchEvent(new CustomEvent('careerforge:unauthorized'));
      }

      // 429 Rate Limit
      if (status === 429) {
        const retryAfter = error.response.headers['retry-after'] || error.response.headers['x-rate-limit-retry-after-seconds'];
        const message = data?.message || `Rate limit exceeded. Please retry in ${retryAfter || 60} seconds.`;
        return Promise.reject(new Error(message));
      }

      const message = data?.message || error.message || 'Request failed';
      return Promise.reject(new Error(message));
    }
    return Promise.reject(error);
  }
);

// -------------------------------------------------------------
// MODULE APIS
// -------------------------------------------------------------

// 1. Authentication APIs
export const authApi = {
  login: async (credentials) => {
    const data = await apiClient.post('/auth/login', credentials);
    if (data && data.token) {
      setToken(data.token);
      setStoredUser({
        userId: data.userId,
        name: data.name,
        email: data.email,
        role: data.role,
      });
    }
    return data;
  },
  register: async (userData) => {
    const data = await apiClient.post('/auth/register', userData);
    if (data && data.token) {
      setToken(data.token);
      setStoredUser({
        userId: data.userId,
        name: data.name,
        email: data.email,
        role: data.role,
      });
    }
    return data;
  },
  logout: () => {
    clearAuthData();
  },
};

// 2. Job Listings APIs
export const jobsApi = {
  getAll: (params = {}) => {
    const cleanParams = {};
    if (params.search) cleanParams.search = params.search;
    if (params.locationId) cleanParams.locationId = params.locationId;
    if (params.employmentType) cleanParams.employmentType = params.employmentType;
    if (params.page !== undefined) cleanParams.page = params.page;
    if (params.size !== undefined) cleanParams.size = params.size;
    return apiClient.get('/jobs', { params: cleanParams });
  },
  getById: (id) => apiClient.get(`/jobs/${id}`),
  create: (jobData) => apiClient.post('/jobs', jobData),
  update: (id, jobData) => apiClient.put(`/jobs/${id}`, jobData),
  updateStatus: (id, status) => apiClient.patch(`/jobs/${id}/status`, { status }),
  delete: (id) => apiClient.delete(`/jobs/${id}`),
};

// 3. Taxonomy & Skills APIs
export const skillsApi = {
  getAll: () => apiClient.get('/skills'),
  create: (skill) => apiClient.post('/skills', skill),
  getCandidateSkills: () => apiClient.get('/candidate/skills'),
  addCandidateSkill: (skillId) => apiClient.post(`/candidate/skills/${skillId}`),
  removeCandidateSkill: (skillId) => apiClient.delete(`/candidate/skills/${skillId}`),
};

// 4. Companies & Locations APIs
export const companyApi = {
  getAll: () => apiClient.get('/companies'),
  getById: (id) => apiClient.get(`/companies/${id}`),
  create: (companyData) => apiClient.post('/companies', companyData),
};

// 5. Candidate & Recruiter Profile APIs
export const profileApi = {
  getCandidateProfile: () => apiClient.get('/candidate/profile'),
  updateCandidateBio: (bioData) => apiClient.put('/candidate/profile', bioData),
  addEducation: (edu) => apiClient.post('/candidate/education', edu),
  deleteEducation: (id) => apiClient.delete(`/candidate/education/${id}`),
  addExperience: (exp) => apiClient.post('/candidate/experience', exp),
  deleteExperience: (id) => apiClient.delete(`/candidate/experience/${id}`),
  uploadResumeMeta: (resumeDto) => apiClient.post('/candidate/resume', resumeDto),

  getRecruiterProfile: () => apiClient.get('/recruiter/profile'),
  updateRecruiterProfile: (profileData) => apiClient.put('/recruiter/profile', profileData),
};

// 6. Resume Upload & Document Processing (Multipart Axios)
export const resumeApi = {
  upload: (file, analyzeWithAi = true) => {
    const formData = new FormData();
    formData.append('file', file);
    return apiClient.post(`/candidate/resume/upload?analyzeWithAi=${analyzeWithAi}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
  downloadUrl: (fileName) => `/api/v1/candidate/resume/download/${fileName}`,
};

// 7. AI Matching Engine APIs
export const matchingApi = {
  getMatchForJob: (jobId) => apiClient.get(`/jobs/${jobId}/match`),
  getRecommendedJobs: (limit = 10) => apiClient.get(`/candidate/job-matches?limit=${limit}`),
  getTopCandidatesForJob: (jobId, limit = 10) => apiClient.get(`/recruiter/jobs/${jobId}/candidate-matches?limit=${limit}`),
};

// 8. Applications & Saved Jobs APIs
export const applicationApi = {
  apply: (jobId, notes = '', resumeId = null) => apiClient.post(`/jobs/${jobId}/apply`, { notes, resumeId }),
  getMyApplications: (params = {}) => apiClient.get('/candidate/applications', { params }),
  getApplicationById: (id) => apiClient.get(`/candidate/applications/${id}`),
  getJobApplications: (jobId, params = {}) => apiClient.get(`/recruiter/jobs/${jobId}/applications`, { params }),
  getAllRecruiterApplications: (params = {}) => apiClient.get('/recruiter/applications', { params }),
  updateStatus: (appId, status, notes = '') => apiClient.patch(`/recruiter/applications/${appId}/status`, { status, notes }),
  
  saveJob: (jobId) => apiClient.post(`/jobs/${jobId}/save`),
  unsaveJob: (jobId) => apiClient.delete(`/jobs/${jobId}/save`),
  getSavedJobs: (params = {}) => apiClient.get('/candidate/saved-jobs', { params }),
  isJobSaved: (jobId) => apiClient.get(`/jobs/${jobId}/is-saved`),
};

// 9. Notification APIs
export const notificationApi = {
  getNotifications: (params = { page: 0, size: 10 }) => apiClient.get('/notifications', { params }),
  getUnreadCount: () => apiClient.get('/notifications/unread-count'),
  markAsRead: (id) => apiClient.patch(`/notifications/${id}/read`),
  markAllAsRead: () => apiClient.patch('/notifications/read-all'),
  testPush: (title, message) => apiClient.post('/notifications/test-push', null, {
    params: { title, message }
  }),
  broadcast: (title, message) => apiClient.post('/notifications/broadcast', null, {
    params: { title, message }
  }),
};

// 10. Analytics APIs
export const analyticsApi = {
  getPlatformKpis: () => apiClient.get('/analytics/kpis'),
  getSkillDemandSupply: () => apiClient.get('/analytics/skills/demand-supply'),
};

// 11. Gemini AI APIs
export const aiApi = {
  generateJobDescription: (data) => apiClient.post('/ai/jobs/generate-description', data),
  analyzeResume: (data) => apiClient.post('/ai/resume/analyze', data),
};

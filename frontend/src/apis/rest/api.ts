import { ApiConfig, apiRequest, ApiRequestOptions } from './apiRequest';

export const api = {
  get: <T>(url: string, options: Omit<ApiRequestOptions<T>, 'method' | 'data'> = {}) =>
    apiRequest<T, undefined>(url, { ...options, method: 'GET' }),

  post: <T, TData>(
    url: string,
    data?: TData,
    options: Omit<ApiRequestOptions<TData>, 'method' | 'data'> = {}
  ) => apiRequest<T, TData>(url, { ...options, method: 'POST', data }),

  put: <T, TData>(
    url: string,
    data?: TData,
    options: Omit<ApiRequestOptions<TData>, 'method' | 'data'> = {}
  ) => apiRequest<T, TData>(url, { ...options, method: 'PUT', data }),

  patch: <T, TData>(
    url: string,
    data?: TData,
    options: Omit<ApiRequestOptions<TData>, 'method' | 'data'> = {}
  ) => apiRequest<T, TData>(url, { ...options, method: 'PATCH', data }),

  delete: <T>(url: string, options: Omit<ApiRequestOptions<T>, 'method' | 'data'> = {}) =>
    apiRequest<T, undefined>(url, { ...options, method: 'DELETE' }),

  setDefaultConfig: (config: ApiConfig) => {
    api.defaultConfig = { ...api.defaultConfig, ...config };
  },

  defaultConfig: {},
};

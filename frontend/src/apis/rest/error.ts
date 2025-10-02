import { Method } from './apiRequest';

export type ErrorDisplayMode = 'fallback' | 'toast';

export class ApiError extends Error {
  constructor(
    public status: number,
    public message: string,
    public data = null,
    public displayMode: ErrorDisplayMode = 'toast',
    public method: Method
  ) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
    this.displayMode = displayMode;
    this.method = method;
  }
}

export class NetworkError extends Error {
  constructor(
    public message: string,
    public displayMode: ErrorDisplayMode = 'fallback'
  ) {
    super(message);
    this.name = 'NetworkError';
    this.displayMode = displayMode;
  }
}

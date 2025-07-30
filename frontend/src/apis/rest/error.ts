export class ApiError extends Error {
  constructor(
    public status: number,
    public message: string,
    public data = null
  ) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

export class NetworkError extends Error {
  constructor(public message: string) {
    super(message);
    this.name = 'NetworkError';
  }
}

export type NetworkRequest = {
  id: string;
  type: 'fetch' | 'websocket';
  context: string;
  url: string;
  timestamp: number;
  method?: string;
  status?: number | string;
  responseBody?: string | null;
  durationMs?: number;
  data?: string;
  errorMessage?: string;
};

export type NetworkCollector = {
  getRequests: () => NetworkRequest[];
  clear: () => void;
  subscribe: (listener: (request: NetworkRequest) => void) => () => void;
};

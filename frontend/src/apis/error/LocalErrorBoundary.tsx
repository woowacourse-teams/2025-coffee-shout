import { Component, ReactNode } from 'react';
import { ApiError, NetworkError } from '@/apis/rest/error';
import LocalErrorFallback from './LocalErrorFallback';

interface Props {
  children: ReactNode;
  fallback?: (error: Error, retry: () => void) => ReactNode;
}

interface State {
  error: Error | null;
}

class LocalErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { error: null };
  }

  static getDerivedStateFromError(error: Error): State {
    if (error instanceof ApiError) {
      if (error.method !== 'GET') {
        throw error;
      }

      if (error.displayMode === 'fallback') {
        return { error: error as ApiError };
      }
    }

    if (error instanceof NetworkError) {
      return { error: error as NetworkError };
    }

    throw error;
  }

  handleRetry = (): void => {
    this.setState({ error: null });
  };

  render(): ReactNode {
    if (this.state.error) {
      if (this.props.fallback) {
        return this.props.fallback(this.state.error, this.handleRetry);
      }

      return <LocalErrorFallback error={this.state.error} handleRetry={this.handleRetry} />;
    }

    return this.props.children;
  }
}

export default LocalErrorBoundary;

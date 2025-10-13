import { ContextType, Component, ReactNode } from 'react';
import { ApiError, NetworkError } from '@/apis/rest/error';
import { ToastContext } from '@/components/@common/Toast/ToastContext';
import GlobalErrorFallback from '@/components/@common/ErrorFallback/GlobalErrorFallback';

type Props = {
  children: ReactNode;
  fallback?: ReactNode;
};

type State = {
  error: Error | null;
};

class GlobalErrorBoundary extends Component<Props, State> {
  static contextType = ToastContext;
  declare context: ContextType<typeof ToastContext>;

  constructor(props: Props) {
    super(props);
    this.state = { error: null };
  }

  static getDerivedStateFromError(error: Error): State {
    if (error instanceof ApiError && error.displayMode === 'toast') {
      return {
        error: null,
      };
    }

    if (error instanceof NetworkError && error.displayMode === 'toast') {
      return {
        error: null,
      };
    }

    return { error: error as Error };
  }

  componentDidCatch(error: Error) {
    if (error instanceof ApiError && error.displayMode === 'toast') {
      this.context?.showToast({
        type: 'error',
        message: error.message,
      });
    }

    if (error instanceof NetworkError && error.displayMode === 'toast') {
      this.context?.showToast({
        type: 'error',
        message: '네트워크 오류가 발생했습니다. 다시 시도해주세요.',
      });
    }
  }

  render(): ReactNode {
    const { fallback = <GlobalErrorFallback error={this.state.error!} /> } = this.props;

    if (this.state.error) {
      return fallback;
    }

    return this.props.children;
  }
}

export default GlobalErrorBoundary;

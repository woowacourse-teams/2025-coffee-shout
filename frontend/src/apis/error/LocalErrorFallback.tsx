import Button from '@/components/@common/Button/Button';
import * as S from './LocalErrorFallback.styled';
import Headline3 from '@/components/@common/Headline3/Headline3';
import { ApiError, NetworkError } from '@/apis/rest/error';
import ErrorIcon from './ErrorIcon';

type HTTP_ERROR_STATUS = keyof typeof HTTP_ERROR_MESSAGE;

const HTTP_ERROR_MESSAGE = {
  400: {
    message: '잘못된 요청',
    description: '잘못된 요청입니다. 다시 시도해주세요.',
  },
  403: {
    message: '권한 없음',
    description: '권한이 없습니다. 다시 시도해주세요.',
  },
  404: {
    message: '리소스를 찾을 수 없음',
    description: '리소스를 찾을 수 없음입니다. 다시 시도해주세요.',
  },
  500: {
    message: '서버 오류',
    description: '서버 오류입니다. 다시 시도해주세요.',
  },
};

const LocalErrorFallback = ({ error, handleRetry }: { error: Error; handleRetry: () => void }) => {
  const { message, description } = getErrorInfo(error);

  return (
    <S.Container>
      <ErrorIcon />
      <Headline3>{message}</Headline3>
      <S.Message>{description}</S.Message>
      <Button variant="secondary" width="50%" onClick={handleRetry}>
        다시 시도하기
      </Button>
    </S.Container>
  );
};

export default LocalErrorFallback;

const getErrorInfo = (error: Error): { message: string; description: string } => {
  if (error instanceof ApiError) {
    if (!HTTP_ERROR_MESSAGE[error.status as HTTP_ERROR_STATUS]) {
      return {
        message: '알 수 없는 오류',
        description: '예상치 못한 문제가 발생했습니다. 다시 시도해주세요.',
      };
    }

    return {
      message: HTTP_ERROR_MESSAGE[error.status as HTTP_ERROR_STATUS].message,
      description: HTTP_ERROR_MESSAGE[error.status as HTTP_ERROR_STATUS].description,
    };
  }

  if (error instanceof NetworkError) {
    return {
      message: '네트워크 연결 실패',
      description: '네트워크 연결이 실패했습니다. 인터넷 연결을 확인하고 다시 시도해주세요.',
    };
  }

  return {
    message: '알 수 없는 오류',
    description: '예상치 못한 문제가 발생했습니다. 다시 시도해주세요.',
  };
};

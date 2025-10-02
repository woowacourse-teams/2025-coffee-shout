import Button from '@/components/@common/Button/Button';
import * as S from './LocalErrorFallback.styled';
import Headline3 from '@/components/@common/Headline3/Headline3';
import ErrorIcon from './ErrorIcon';
import { getErrorInfo } from './errorMessages';

const GlobalErrorFallback = ({ error }: { error: Error }) => {
  const { message, description } = getErrorInfo(error);

  return (
    <S.Container>
      <ErrorIcon />
      <Headline3>{message}</Headline3>
      <S.Message>{description}</S.Message>
      <Button variant="secondary" width="50%" onClick={() => (window.location.href = '/')}>
        메인으로 돌아가기
      </Button>
    </S.Container>
  );
};

export default GlobalErrorFallback;

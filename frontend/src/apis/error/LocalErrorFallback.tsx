import Button from '@/components/@common/Button/Button';
import * as S from './LocalErrorFallback.styled';
import Headline3 from '@/components/@common/Headline3/Headline3';

const LocalErrorFallback = ({ error, handleRetry }: { error: Error; handleRetry: () => void }) => {
  console.log(error);

  return (
    <S.Container>
      <Headline3>데이터를 불러오지 못했어요</Headline3>
      <S.Message>에러에 따른 적절한 안내 문구 설정하기</S.Message>
      <Button onClick={handleRetry}>다시 시도하기</Button>
    </S.Container>
  );
};

export default LocalErrorFallback;

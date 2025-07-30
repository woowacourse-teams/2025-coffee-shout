import CopyIcon from '@/assets/copy-icon.svg';
import Headline4 from '@/components/@common/Headline4/Headline4';
import Paragraph from '@/components/@common/Paragraph/Paragraph';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import * as S from './JoinCodeModal.styled';

const JoinCodeModal = () => {
  const { joinCode } = useIdentifier();

  const handleCopy = () => {
    alert('초대 코드가 복사되었습니다.');
  };

  return (
    <S.Container>
      <S.Wrapper>
        <Paragraph>초대코드를 복사하여</Paragraph>
        <Paragraph>친구들을 초대해보아요</Paragraph>
      </S.Wrapper>
      <S.CodeBox>
        <S.EmptyBox />
        <Headline4>{joinCode}</Headline4>
        <S.CopyIcon src={CopyIcon} onClick={handleCopy} />
      </S.CodeBox>
    </S.Container>
  );
};

export default JoinCodeModal;

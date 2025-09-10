import { useState } from 'react';
import CopyIcon from '@/assets/copy-icon.svg';
import Headline4 from '@/components/@common/Headline4/Headline4';
import Paragraph from '@/components/@common/Paragraph/Paragraph';
import TabBar from '@/features/room/lobby/components/TabBar/TabBar';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import * as S from './JoinCodeModal.styled';

type props = {
  onClose: () => void;
  qrCodeUrl: string;
};

const JoinCodeModal = ({ onClose, qrCodeUrl }: props) => {
  const { joinCode } = useIdentifier();
  const [activeTab, setActiveTab] = useState(0);
  const tabs = ['QR코드', '초대코드'];

  const handleCopy = async () => {
    await navigator.clipboard.writeText(joinCode);
    alert('초대 코드가 복사되었습니다.');
    onClose();
  };

  const handleShareLink = async () => {
    const shareUrl = `${window.location.origin}/join/${joinCode}`;
    await navigator.clipboard.writeText(shareUrl);
    alert('링크가 복사되었습니다.');
    onClose();
  };

  return (
    <S.Container>
      <TabBar tabs={tabs} activeTab={activeTab} onTabChange={setActiveTab} />
      {activeTab === 0 ? (
        <S.QRSection>
          <S.QRCode>
            <img src={qrCodeUrl} alt="QR Code" />
          </S.QRCode>
          <S.ShareButton onClick={handleShareLink}>링크 공유하기</S.ShareButton>
          <S.Wrapper>
            <Paragraph>QR코드를 스캔하면</Paragraph>
            <Paragraph>바로 게임에 참여할 수 있어요!</Paragraph>
          </S.Wrapper>
        </S.QRSection>
      ) : (
        <S.CodeSection>
          <S.Wrapper>
            <Paragraph>초대코드를 복사하여</Paragraph>
            <Paragraph>친구들을 초대해보세요!</Paragraph>
          </S.Wrapper>
          <S.CodeBox>
            <S.EmptyBox />
            <Headline4>{joinCode}</Headline4>
            <S.CopyIcon src={CopyIcon} onClick={handleCopy} />
          </S.CodeBox>
        </S.CodeSection>
      )}
    </S.Container>
  );
};

export default JoinCodeModal;

import { useState } from 'react';
import CopyIcon from '@/assets/copy-icon.svg';
import Headline4 from '@/components/@common/Headline4/Headline4';
import Paragraph from '@/components/@common/Paragraph/Paragraph';
import TabBar from '@/features/room/lobby/components/TabBar/TabBar';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import * as S from './InvitationModal.styled';
import useToast from '@/components/@common/Toast/useToast';

type props = {
  onClose: () => void;
};

const InvitationModal = ({ onClose }: props) => {
  const { showToast } = useToast();
  const { joinCode, qrCodeUrl } = useIdentifier();
  const [activeTabIndex, setActiveTabIndex] = useState(0);
  const tabs = ['QR코드', '초대코드'];

  const handleCopy = async () => {
    copyToClipboard(joinCode, '초대 코드가 복사되었습니다.');
  };

  const handleShareLink = async () => {
    const shareUrl = `${window.location.origin}/join/${joinCode}`;
    copyToClipboard(shareUrl, '참여 링크가 복사되었습니다.');
  };

  const copyToClipboard = async (text: string, message: string) => {
    await navigator.clipboard.writeText(text);
    showToast({
      type: 'success',
      message,
    });
    onClose();
  };

  return (
    <S.Container>
      <TabBar tabs={tabs} activeTabIndex={activeTabIndex} onTabChange={setActiveTabIndex} />
      {activeTabIndex === 0 ? (
        <QRSection qrCodeUrl={qrCodeUrl} handleShareLink={handleShareLink} />
      ) : (
        <CodeSection joinCode={joinCode} handleCopy={handleCopy} />
      )}
    </S.Container>
  );
};

export default InvitationModal;

type QRSectionProps = {
  qrCodeUrl: string;
  handleShareLink: () => void;
};

type CodeSectionProps = {
  joinCode: string;
  handleCopy: () => void;
};

const QRSection = ({ qrCodeUrl, handleShareLink }: QRSectionProps) => {
  return (
    <S.QRSection>
      <S.QRCode>
        <img src={qrCodeUrl} alt="QR Code" />
      </S.QRCode>
      <S.ShareButton onClick={handleShareLink}>
        <Paragraph>링크 공유하기</Paragraph>
      </S.ShareButton>
      <S.Wrapper>
        <Paragraph>QR코드를 스캔하면</Paragraph>
        <Paragraph>바로 게임에 참여할 수 있어요!</Paragraph>
      </S.Wrapper>
    </S.QRSection>
  );
};

const CodeSection = ({ joinCode, handleCopy }: CodeSectionProps) => {
  return (
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
  );
};

import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import ShareIcon from '@/assets/share-icon.svg';
import BackButton from '@/components/@common/BackButton/BackButton';
import Button from '@/components/@common/Button/Button';
import useModal from '@/components/@common/Modal/useModal';
import ToggleButton from '@/components/@common/ToggleButton/ToggleButton';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import Layout from '@/layouts/Layout';
import { ReactElement, useCallback, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import JoinCodeModal from '../components/JoinCodeModal/JoinCodeModal';
import { MiniGameSection } from '../components/MiniGameSection/MiniGameSection';
import { ParticipantSection } from '../components/ParticipantSection/ParticipantSection';
import { RouletteSection } from '../components/RouletteSection/RouletteSection';
import * as S from './LobbyPage.styled';

export type SectionType = '참가자' | '룰렛' | '미니게임';
type SectionComponents = Record<SectionType, ReactElement>;
type MiniGameResponse = string[];

const LobbyPage = () => {
  const navigate = useNavigate();
  const { openModal } = useModal();
  const { playerType } = usePlayerType();
  const { joinCode } = useIdentifier();
  const { send } = useWebSocket();
  const [currentSection, setCurrentSection] = useState<SectionType>('참가자');
  const [selectedMiniGames, setSelectedMiniGames] = useState<string[]>([]);

  const handleMiniGameData = useCallback((data: MiniGameResponse) => {
    setSelectedMiniGames(data);
  }, []);

  useWebSocketSubscription<MiniGameResponse>(`/room/${joinCode}/minigame`, handleMiniGameData);

  const handleClickBackButton = () => {
    navigate(-1);
  };

  const handleClickGameStartButton = () => {
    navigate('/room/:roomId/:miniGameId/ready');
  };

  const handleSectionChange = (option: SectionType) => {
    setCurrentSection(option);
  };

  const handleShare = () => {
    openModal(<JoinCodeModal />, {
      title: '초대 코드',
      showCloseButton: true,
    });
  };

  const handleMiniGameClick = (miniGameType: string) => {
    if (playerType === 'GUEST') return;

    const updatedMiniGames = selectedMiniGames.includes(miniGameType)
      ? selectedMiniGames.filter((game) => game !== miniGameType)
      : [...selectedMiniGames, miniGameType];

    setSelectedMiniGames(updatedMiniGames);

    send(
      `/room/${joinCode}/update-minigames`,
      JSON.stringify({
        hostName: playerType,
        miniGameType: updatedMiniGames,
      })
    );
  };

  const SECTIONS: SectionComponents = {
    참가자: <ParticipantSection />,
    룰렛: <RouletteSection />,
    미니게임: (
      <MiniGameSection
        selectedMiniGames={selectedMiniGames}
        handleMiniGameClick={handleMiniGameClick}
      />
    ),
  };

  if (!playerType) return null;

  return (
    <Layout>
      <Layout.TopBar left={<BackButton onClick={handleClickBackButton} />} />
      <Layout.Content>
        <S.Container>
          {SECTIONS[currentSection]}
          <S.Wrapper>
            <ToggleButton
              options={['참가자', '룰렛', '미니게임']}
              selectedOption={currentSection}
              onSelectOption={handleSectionChange}
            />
          </S.Wrapper>
        </S.Container>
      </Layout.Content>
      {playerType === 'HOST' ? (
        <Layout.ButtonBar flexRatios={[5.5, 1]}>
          <Button variant="primary" onClick={handleClickGameStartButton}>
            게임 시작
          </Button>
          <Button variant="primary" onClick={handleShare}>
            <img src={ShareIcon} alt="공유" />
          </Button>
        </Layout.ButtonBar>
      ) : (
        <Layout.ButtonBar flexRatios={[5.5, 1]}>
          <Button variant="loading">게임 대기중</Button>
        </Layout.ButtonBar>
      )}
    </Layout>
  );
};

export default LobbyPage;

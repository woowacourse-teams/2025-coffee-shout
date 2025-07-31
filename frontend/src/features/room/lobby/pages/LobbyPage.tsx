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
import { MiniGameType } from '@/types/miniGame';
import { ReactElement, useCallback, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import JoinCodeModal from '../components/JoinCodeModal/JoinCodeModal';
import { MiniGameSection } from '../components/MiniGameSection/MiniGameSection';
import { ParticipantSection } from '../components/ParticipantSection/ParticipantSection';
import { RouletteSection } from '../components/RouletteSection/RouletteSection';
import * as S from './LobbyPage.styled';

type SectionType = '참가자' | '룰렛' | '미니게임';
type SectionComponents = Record<SectionType, ReactElement>;

const LobbyPage = () => {
  const navigate = useNavigate();
  const { send } = useWebSocket();
  const { openModal } = useModal();
  const { roomId } = useParams();
  const { playerType } = usePlayerType();
  const { joinCode, myName } = useIdentifier();
  const [currentSection, setCurrentSection] = useState<SectionType>('참가자');
  const [selectedMiniGames, setSelectedMiniGames] = useState<MiniGameType[]>([]);

  const handleMiniGameData = useCallback((data: MiniGameType[]) => {
    setSelectedMiniGames(data);
  }, []);

  useWebSocketSubscription<MiniGameType[]>(`/room/${joinCode}/minigame`, handleMiniGameData);

  const handleClickBackButton = () => {
    navigate('/');
  };

  const handleClickGameStartButton = () => {
    navigate(`/room/${roomId}/${selectedMiniGames[0]}/ready`);
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

  const handleMiniGameClick = (miniGameType: MiniGameType) => {
    if (playerType === 'GUEST') return;

    const updatedMiniGames = selectedMiniGames.includes(miniGameType)
      ? selectedMiniGames.filter((game) => game !== miniGameType)
      : [...selectedMiniGames, miniGameType];

    setSelectedMiniGames(updatedMiniGames);

    send(`/room/${joinCode}/update-minigames`, {
      hostName: myName,
      miniGameTypes: updatedMiniGames,
    });
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

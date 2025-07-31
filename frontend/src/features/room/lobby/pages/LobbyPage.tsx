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
import { Player } from '@/types/player';
import { ReactElement, useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import JoinCodeModal from '../components/JoinCodeModal/JoinCodeModal';
import { MiniGameSection } from '../components/MiniGameSection/MiniGameSection';
import { ParticipantSection } from '../components/ParticipantSection/ParticipantSection';
import { RouletteSection } from '../components/RouletteSection/RouletteSection';
import * as S from './LobbyPage.styled';

type SectionType = '참가자' | '룰렛' | '미니게임';
type SectionComponents = Record<SectionType, ReactElement>;

type ParticipantResponse = Player[];

const LobbyPage = () => {
  const navigate = useNavigate();

  const { send } = useWebSocket();
  const { myName, joinCode } = useIdentifier();
  const { openModal } = useModal();
  const { playerType } = usePlayerType();
  const [currentSection, setCurrentSection] = useState<SectionType>('참가자');
  const [selectedMiniGames, setSelectedMiniGames] = useState<MiniGameType[]>([]);
  const [participants, setParticipants] = useState<ParticipantResponse>([]);

  const handleParticipant = useCallback((data: ParticipantResponse) => {
    setParticipants(data);
  }, []);

  const handleMiniGameData = useCallback((data: MiniGameType[]) => {
    setSelectedMiniGames(data);
  }, []);

  useWebSocketSubscription<ParticipantResponse>(`/room/${joinCode}`, handleParticipant);
  useWebSocketSubscription<MiniGameType[]>(`/room/${joinCode}/minigame`, handleMiniGameData);

  const handleGameStart = useCallback(
    (data: { miniGameType: MiniGameType }) => {
      const { miniGameType: nextMiniGame } = data;
      navigate(`/room/${joinCode}/${nextMiniGame}/ready`);
    },

    [joinCode, navigate]
  );

  useWebSocketSubscription(`/room/${joinCode}/round`, handleGameStart);

  useEffect(() => {
    if (joinCode) {
      send(`/room/${joinCode}/update-players`);
    }
  }, [playerType, joinCode, send]);

  const handleClickBackButton = () => {
    navigate('/');
  };

  const handleClickGameStartButton = () => {
    send(`/room/${joinCode}/minigame/command`, {
      commandType: 'START_MINI_GAME',
      commandRequest: {
        hostName: myName,
      },
    });
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
    참가자: <ParticipantSection participants={participants} />,
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

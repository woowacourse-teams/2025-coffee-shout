import { api } from '@/apis/rest/api';
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
import { PlayerProbability, Probability } from '@/types/roulette';
import { ReactElement, useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import JoinCodeModal from '../components/JoinCodeModal/JoinCodeModal';
import { MiniGameSection } from '../components/MiniGameSection/MiniGameSection';
import { ParticipantSection } from '../components/ParticipantSection/ParticipantSection';
import { RouletteSection } from '../components/RouletteSection/RouletteSection';
import GameStartButton from '../components/GameStartButton/GameStartButton';
import HostWaitingButton from '../components/HostWaitingButton/HostWaitingButton';
import GameReadyButton from '../components/GameReadyButton/GameReadyButton';
import * as S from './LobbyPage.styled';
import { colorList } from '@/constants/color';

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
  const isAllReady = participants.every((participant) => participant.isReady);
  const isReady =
    participants.find((participant) => participant.playerName === myName)?.isReady ?? false;

  const handleParticipant = useCallback((data: ParticipantResponse) => {
    setParticipants(data);
  }, []);

  // TODO: 나중에 외부 state 로 분리할 것
  const [playerProbabilities, setPlayerProbabilities] = useState<PlayerProbability[]>([]);

  const handleMiniGameData = useCallback((data: MiniGameType[]) => {
    setSelectedMiniGames(data);
  }, []);

  const handlePlayerProbabilitiesData = useCallback((data: Probability[]) => {
    const parsedData = data.map((item) => ({
      playerName: item.playerResponse.playerName,
      probability: item.probability,
      playerColor: colorList[item.playerResponse.colorIndex],
    }));

    setPlayerProbabilities(parsedData);
  }, []);

  const handleGameStart = useCallback(
    (data: { miniGameType: MiniGameType }) => {
      const { miniGameType: nextMiniGame } = data;
      navigate(`/room/${joinCode}/${nextMiniGame}/ready`);
    },

    [joinCode, navigate]
  );

  useWebSocketSubscription<ParticipantResponse>(`/room/${joinCode}`, handleParticipant);
  useWebSocketSubscription<MiniGameType[]>(`/room/${joinCode}/minigame`, handleMiniGameData);
  useWebSocketSubscription<Probability[]>(
    `/room/${joinCode}/roulette`,
    handlePlayerProbabilitiesData
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
    if (participants.length < 2) return;

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

  const handleGameReadyButtonClick = () => {
    send(`/room/${joinCode}/update-ready`, {
      joinCode,
      playerName: myName,
      isReady: !isReady,
    });
  };

  const renderGameButton = () => {
    if (playerType === 'HOST') {
      if (isAllReady) {
        return <GameStartButton onClick={handleClickGameStartButton} />;
      }
      return (
        <HostWaitingButton
          currentReadyCount={participants.filter((participant) => participant.isReady).length}
          totalParticipantCount={participants.length}
        />
      );
    }

    return <GameReadyButton isReady={isReady} onClick={handleGameReadyButtonClick} />;
  };

  useEffect(() => {
    if (playerType === 'GUEST' && joinCode) {
      send(`/room/${joinCode}/get-probabilities`);
    }
  }, [playerType, joinCode, send]);

  useEffect(() => {
    (async () => {
      const _selectedMiniGames = await api.get<MiniGameType[]>(
        `/rooms/minigames/selected?joinCode=${joinCode}`
      );
      setSelectedMiniGames(_selectedMiniGames);
    })();
  }, [joinCode]);

  const SECTIONS: SectionComponents = {
    참가자: <ParticipantSection participants={participants} />,
    룰렛: <RouletteSection playerProbabilities={playerProbabilities} />,
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

      <Layout.ButtonBar flexRatios={[5.5, 1]}>
        {renderGameButton()}
        <Button variant="primary" onClick={handleShare}>
          <img src={ShareIcon} alt="공유" />
        </Button>
      </Layout.ButtonBar>
    </Layout>
  );
};

export default LobbyPage;

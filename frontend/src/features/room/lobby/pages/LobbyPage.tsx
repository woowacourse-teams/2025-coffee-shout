import { api } from '@/apis/rest/api';
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import ShareIcon from '@/assets/share-icon.svg';
import BackButton from '@/components/@common/BackButton/BackButton';
import Button from '@/components/@common/Button/Button';
import useModal from '@/components/@common/Modal/useModal';
import useToast from '@/components/@common/Toast/useToast';
import ToggleButton from '@/components/@common/ToggleButton/ToggleButton';
import { colorList } from '@/constants/color';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useParticipants } from '@/contexts/Participants/ParticipantsContext';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import { useProbabilityHistory } from '@/contexts/ProbabilityHistory/ProbabilityHistoryContext';
import Layout from '@/layouts/Layout';
import { MiniGameType } from '@/types/miniGame/common';
import { Player } from '@/types/player';
import { PlayerProbability, Probability } from '@/types/roulette';
import { ReactElement, useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { storageManager, STORAGE_KEYS } from '@/utils/StorageManager';
import GameReadyButton from '../components/GameReadyButton/GameReadyButton';
import GameStartButton from '../components/GameStartButton/GameStartButton';
import GuideModal from '../components/GuideModal/GuideModal';
import HostWaitingButton from '../components/HostWaitingButton/HostWaitingButton';
import InvitationModal from '../components/JoinCodeModal/InvitationModal';
import { MiniGameSection } from '../components/MiniGameSection/MiniGameSection';
import { ParticipantSection } from '../components/ParticipantSection/ParticipantSection';
import { RouletteSection } from '../components/RouletteSection/RouletteSection';
import { useParticipantValidation } from '../hooks/useParticipantValidation';
import * as S from './LobbyPage.styled';

type SectionType = '참가자' | '룰렛' | '미니게임';
type SectionComponents = Record<SectionType, ReactElement>;

const LobbyPage = () => {
  const navigate = useNavigate();
  const { send, isConnected } = useWebSocket();
  const { myName, joinCode } = useIdentifier();
  const { openModal, closeModal } = useModal();
  const { showToast } = useToast();
  const { playerType, setPlayerType } = usePlayerType();
  const { updateCurrentProbabilities } = useProbabilityHistory();
  const { participants, setParticipants, isAllReady, checkPlayerReady } = useParticipants();
  const [currentSection, setCurrentSection] = useState<SectionType>('참가자');
  const [selectedMiniGames, setSelectedMiniGames] = useState<MiniGameType[]>([]);
  const isReady = checkPlayerReady(myName) ?? false;

  useParticipantValidation({ isConnected });

  const handleParticipant = useCallback(
    (data: Player[]) => {
      setParticipants(data);

      const currentPlayer = data.find((player) => player.playerName === myName);
      if (currentPlayer) {
        setPlayerType(currentPlayer.playerType);
      }
    },
    [setParticipants, myName, setPlayerType]
  );

  // TODO: 나중에 외부 state 로 분리할 것
  const [playerProbabilities, setPlayerProbabilities] = useState<PlayerProbability[]>([]);

  const handleMiniGameData = useCallback((data: MiniGameType[]) => {
    setSelectedMiniGames(data);
  }, []);

  const handlePlayerProbabilitiesData = useCallback(
    (data: Probability[]) => {
      const parsedData = data.map((item) => ({
        playerName: item.playerResponse.playerName,
        probability: item.probability,
        playerColor: colorList[item.playerResponse.colorIndex],
      }));

      setPlayerProbabilities(parsedData);
      updateCurrentProbabilities(parsedData);
    },
    [updateCurrentProbabilities]
  );

  const handleGameStart = useCallback(
    (data: { miniGameType: MiniGameType }) => {
      const { miniGameType: nextMiniGame } = data;
      navigate(`/room/${joinCode}/${nextMiniGame}/ready`);
    },

    [joinCode, navigate]
  );

  useWebSocketSubscription<Player[]>(`/room/${joinCode}`, handleParticipant);
  useWebSocketSubscription<MiniGameType[]>(`/room/${joinCode}/minigame`, handleMiniGameData);
  useWebSocketSubscription<Probability[]>(
    `/room/${joinCode}/roulette`,
    handlePlayerProbabilitiesData
  );
  useWebSocketSubscription(`/room/${joinCode}/round`, handleGameStart);

  useEffect(() => {
    if (joinCode && isConnected) {
      send(`/room/${joinCode}/update-players`);
    }
  }, [playerType, joinCode, send, isConnected]);

  const handleNavigateToHome = () => {
    navigate('/');
  };

  const handleClickGameStartButton = () => {
    if (participants.length < 2) {
      showToast({
        type: 'error',
        message: '참여자가 없어 게임을 진행할 수 없습니다.',
      });
      return;
    }

    if (selectedMiniGames.length === 0) {
      showToast({
        type: 'error',
        message: '선택된 미니게임이 없어 게임을 진행할 수 없습니다.',
      });
      return;
    }

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
    openModal(<InvitationModal onClose={closeModal} />, {
      title: '친구 초대하기',
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
    if (joinCode && isConnected) {
      send(`/room/${joinCode}/get-probabilities`);
    }
  }, [playerType, joinCode, send, isConnected]);

  useEffect(() => {
    (async () => {
      if (joinCode) {
        const _selectedMiniGames = await api.get<MiniGameType[]>(
          `/rooms/minigames/selected?joinCode=${joinCode}`
        );
        setSelectedMiniGames(_selectedMiniGames);
      }
    })();
  }, [joinCode]);

  useEffect(() => {
    const isFirstTimeUser = storageManager.getItem(STORAGE_KEYS.FIRST_TIME_USER, 'localStorage');

    if (!isFirstTimeUser) {
      openModal(
        <GuideModal
          onClose={() => {
            storageManager.setItem(STORAGE_KEYS.FIRST_TIME_USER, 'true', 'localStorage');
            closeModal();
          }}
        />,
        {
          showCloseButton: false,
        }
      );
    }
  }, [openModal, closeModal]);

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

  return (
    <Layout>
      <Layout.TopBar left={<BackButton onClick={handleNavigateToHome} />} />
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

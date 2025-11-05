import useFetch from '@/apis/rest/useFetch';
import useMutation from '@/apis/rest/useMutation';
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import ShareIcon from '@/assets/share-icon.svg';
import BackButton from '@/components/@common/BackButton/BackButton';
import Button from '@/components/@common/Button/Button';
import LocalErrorBoundary from '@/components/@common/ErrorBoundary/LocalErrorBoundary';
import useModal from '@/components/@common/Modal/useModal';
import ScreenReaderOnly from '@/components/@common/ScreenReaderOnly/ScreenReaderOnly';
import useToast from '@/components/@common/Toast/useToast';
import ToggleButton from '@/components/@common/ToggleButton/ToggleButton';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import { colorList } from '@/constants/color';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useParticipants } from '@/contexts/Participants/ParticipantsContext';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import { useProbabilityHistory } from '@/contexts/ProbabilityHistory/ProbabilityHistoryContext';
import { useReplaceNavigate } from '@/hooks/useReplaceNavigate';
import Layout from '@/layouts/Layout';
import { MiniGameType } from '@/types/miniGame/common';
import { Player } from '@/types/player';
import { QRCodeEvent } from '@/types/qrCode';
import { STORAGE_KEYS, storageManager } from '@/utils/StorageManager';
import { ReactElement, useCallback, useEffect, useState } from 'react';
import ConfirmModal from '../components/ConfirmModal/ConfirmModal';
import GameReadyButton from '../components/GameReadyButton/GameReadyButton';
import GameStartButton from '../components/GameStartButton/GameStartButton';
import GuideModal from '../components/GuideModal/GuideModal';
import HostWaitingButton from '../components/HostWaitingButton/HostWaitingButton';
import InvitationModal from '../components/InvitationModal/InvitationModal';
import { MiniGameSection } from '../components/MiniGameSection/MiniGameSection';
import { ParticipantSection } from '../components/ParticipantSection/ParticipantSection';
import { RouletteSection } from '../components/RouletteSection/RouletteSection';
import useGameAnnouncement from '../hooks/useGameAnnouncement';
import * as S from './LobbyPage.styled';

type SectionType = '참가자' | '룰렛' | '미니게임';
type SectionComponents = Record<SectionType, ReactElement>;

const LobbyPage = () => {
  const navigate = useReplaceNavigate();
  const { send, isConnected } = useWebSocket();
  const { myName, joinCode, setQrCodeUrl } = useIdentifier();
  const { openModal, closeModal } = useModal();
  const { showToast } = useToast();
  const { playerType, setPlayerType } = usePlayerType();
  const { probabilityHistory, updateCurrentProbabilities } = useProbabilityHistory();
  const { participants, setParticipants, isAllReady, checkPlayerReady } = useParticipants();
  const [currentSection, setCurrentSection] = useState<SectionType>('참가자');
  const [selectedMiniGames, setSelectedMiniGames] = useState<MiniGameType[]>([]);
  const [qrCodeStatus, setQrCodeStatus] = useState<'PENDING' | 'SUCCESS' | 'ERROR' | null>(null);
  const isReady = checkPlayerReady(myName) ?? false;
  const leaveRoom = useMutation<void, void>({
    endpoint: `/rooms/${joinCode}/players/${myName}`,
    method: 'DELETE',
    errorDisplayMode: 'toast',
  });
  const announcement = useGameAnnouncement({
    isAllReady,
    participants,
    playerType,
    myName,
  });

  const handleParticipant = useCallback(
    (data: Player[]) => {
      console.log('📌📌📌📌📌📌📌📌📌📌📌📌data participants: ', data);
      setParticipants(data);

      const currentPlayer = data.find((player) => player.playerName === myName);
      if (currentPlayer) {
        setPlayerType(currentPlayer.playerType);
      }

      updateCurrentProbabilities(
        data.map((player) => ({
          playerName: player.playerName,
          probability: player.probability,
          playerColor: colorList[player.colorIndex],
        }))
      );
    },
    [setParticipants, myName, setPlayerType, updateCurrentProbabilities]
  );

  useFetch<MiniGameType[]>({
    endpoint: `/rooms/minigames/selected?joinCode=${joinCode}`,
    enabled: !!joinCode,
    onSuccess: (data) => {
      setSelectedMiniGames(data);
    },
  });

  const handleMiniGameData = useCallback((data: MiniGameType[]) => {
    setSelectedMiniGames(data);
  }, []);

  const handleMiniGameError = useCallback(() => {
    if (playerType === 'GUEST') return;
    showToast({
      type: 'error',
      message: '미니게임 선택에 실패하였습니다. 다시 시도해주세요.',
    });
  }, [playerType, showToast]);

  const handleGameStart = useCallback(
    (data: { miniGameType: MiniGameType }) => {
      const { miniGameType: nextMiniGame } = data;
      navigate(`/room/${joinCode}/${nextMiniGame}/ready`);
    },

    [joinCode, navigate]
  );

  const handleQRCodeEvent = useCallback(
    (data: QRCodeEvent) => {
      setQrCodeStatus(data.status);
      switch (data.status) {
        case 'PENDING':
          break;
        case 'SUCCESS':
          if (data.qrCodeUrl) {
            setQrCodeUrl(data.qrCodeUrl);
          }
          break;
        case 'ERROR':
          showToast({
            type: 'error',
            message: 'QR 코드 생성에 실패했습니다.',
          });
          break;
      }
    },
    [setQrCodeUrl, showToast]
  );

  useWebSocketSubscription<Player[]>(`/room/${joinCode}`, handleParticipant);
  useWebSocketSubscription<MiniGameType[]>(
    `/room/${joinCode}/minigame`,
    handleMiniGameData,
    handleMiniGameError
  );
  useWebSocketSubscription(`/room/${joinCode}/round`, handleGameStart);
  useWebSocketSubscription<QRCodeEvent>(
    `/room/${joinCode}/qr-code`,
    handleQRCodeEvent,
    undefined,
    qrCodeStatus !== 'SUCCESS'
  );

  useEffect(() => {
    if (joinCode && isConnected) {
      const timeoutId = setTimeout(() => {
        send(`/room/${joinCode}/update-players`);
      }, 100);

      return () => clearTimeout(timeoutId);
    }
  }, [playerType, joinCode, send, isConnected]);

  const handleBackClick = () => {
    openModal(
      <ConfirmModal
        message="방을 나가시겠습니까?"
        onConfirm={async () => {
          closeModal();
          await leaveRoom.mutate();
          navigate('/');
        }}
        onCancel={closeModal}
      />,
      {
        title: '방 나가기',
        showCloseButton: false,
      }
    );
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

    send(
      `/room/${joinCode}/update-minigames`,
      {
        hostName: myName,
        miniGameTypes: updatedMiniGames,
      },
      handleMiniGameError
    );
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
          closeOnBackdropClick: false,
        }
      );
    }
  }, [openModal, closeModal]);

  const SECTIONS: SectionComponents = {
    참가자: <ParticipantSection participants={participants} />,
    룰렛: <RouletteSection playerProbabilities={probabilityHistory.current} />,
    미니게임: (
      <>
        <SectionTitle title="미니게임" description="미니게임을 선택해주세요" />
        <LocalErrorBoundary>
          <MiniGameSection
            selectedMiniGames={selectedMiniGames}
            handleMiniGameClick={handleMiniGameClick}
          />
        </LocalErrorBoundary>
      </>
    ),
  };

  return (
    <Layout>
      <Layout.TopBar left={<BackButton onClick={handleBackClick} text="방 나가기" />} />
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
        <Button variant="primary" onClick={handleShare} aria-label="친구 초대하기">
          <img src={ShareIcon} aria-hidden="true" alt="" />
        </Button>
      </Layout.ButtonBar>
      <ScreenReaderOnly aria-live="assertive">{announcement}</ScreenReaderOnly>
    </Layout>
  );
};

export default LobbyPage;

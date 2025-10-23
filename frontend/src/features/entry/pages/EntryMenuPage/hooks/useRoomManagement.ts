import useMutation from '@/apis/rest/useMutation';
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import useToast from '@/components/@common/Toast/useToast';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import { Menu, TemperatureOption } from '@/types/menu';
import { useNavigate } from 'react-router-dom';
import { createRoomRequestBody, createUrl } from '../utils/roomApiHelpers';

export type RoomRequest = {
  playerName: string;
  menu: {
    id: number;
    customName: string | null;
    temperature: TemperatureOption;
  };
};

type RoomResponse = {
  joinCode: string;
};

export const useRoomManagement = () => {
  const navigate = useNavigate();

  //TODO: 웹소켓 관련 로직은 Lobby에서 관리하도록 수정해야함
  const { startSocket } = useWebSocket();

  const { playerType } = usePlayerType();
  const { joinCode, myName, setJoinCode } = useIdentifier();
  const { showToast } = useToast();

  const createOrJoinRoom = useMutation<RoomResponse, RoomRequest>({
    endpoint: createUrl(playerType, joinCode),
    method: 'POST',
    onSuccess: (data, variables) => {
      const { joinCode } = data;
      setJoinCode(joinCode);
      startSocket(joinCode, variables.playerName);
    },
    errorDisplayMode: 'toast',
  });

  const isMenuSelectionValid = (selectedMenu: Menu | null, customMenuName: string | null) => {
    if (!selectedMenu && !customMenuName) {
      showToast({
        type: 'error',
        message: '메뉴를 선택하지 않았습니다.',
      });
      return false;
    }
    return true;
  };

  const isPlayerNameValid = () => {
    if (!myName) {
      showToast({
        type: 'error',
        message: '닉네임을 다시 입력해주세요.',
      });
      navigate(-1);
      return false;
    }
    return true;
  };

  const proceedToRoom = async (
    selectedMenu: Menu | null,
    customMenuName: string | null,
    selectedTemperature: TemperatureOption
  ) => {
    if (!isPlayerNameValid()) return;
    if (!isMenuSelectionValid(selectedMenu, customMenuName)) return;

    const requestBody = createRoomRequestBody(
      myName,
      selectedMenu,
      customMenuName,
      selectedTemperature
    );

    await createOrJoinRoom.mutate(requestBody);
  };

  return {
    proceedToRoom,
    isLoading: createOrJoinRoom.loading,
    error: createOrJoinRoom.error,
  };
};

import { useNavigate } from 'react-router-dom';
import { api } from '@/apis/rest/api';
import { ApiError, NetworkError } from '@/apis/rest/error';
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import useToast from '@/components/@common/Toast/useToast';
import { Menu, TemperatureOption } from '@/types/menu';
import { createRoomRequestBody, createUrl } from '../utils/roomApiHelpers';
import { useState } from 'react';

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
  qrCodeUrl: string;
};

export const useRoomManagement = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);

  //TODO: 웹소켓 관련 로직은 Lobby에서 관리하도록 수정해야함
  const { startSocket, isConnected } = useWebSocket();

  const { playerType } = usePlayerType();
  const { joinCode, myName, setJoinCode } = useIdentifier();
  const { showToast } = useToast();
  const { setQrCodeUrl } = useIdentifier();

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

    try {
      setIsLoading(true);

      const { joinCode: _joinCode, qrCodeUrl } = await api.post<RoomResponse, RoomRequest>(
        createUrl(playerType, joinCode),
        createRoomRequestBody(myName, selectedMenu, customMenuName, selectedTemperature)
      );

      setJoinCode(_joinCode);
      setQrCodeUrl(qrCodeUrl);

      startSocket(_joinCode, myName);

      const isReadyToNavigateLobby =
        isConnected && _joinCode && qrCodeUrl && (selectedMenu || customMenuName);

      if (isReadyToNavigateLobby) {
        navigate(`/room/${_joinCode}/lobby`);
      }
    } catch (error) {
      if (error instanceof ApiError) {
        showToast({
          type: 'error',
          message: '방 생성/참가에 실패했습니다.',
        });
      } else if (error instanceof NetworkError) {
        showToast({
          type: 'error',
          message: '네트워크 연결을 확인해주세요.',
        });
      } else {
        showToast({
          type: 'error',
          message: '알 수 없는 오류가 발생했습니다.',
        });
      }
    }
  };

  return {
    proceedToRoom,
    isLoading,
  };
};

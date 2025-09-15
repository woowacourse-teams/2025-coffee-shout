import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '@/apis/rest/api';
import { ApiError, NetworkError } from '@/apis/rest/error';
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import useToast from '@/components/@common/Toast/useToast';
import { Menu, TemperatureOption } from '@/types/menu';

type RoomRequest = {
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
  const { startSocket } = useWebSocket();
  const { playerType } = usePlayerType();
  const { joinCode, myName, setJoinCode } = useIdentifier();
  const { showToast } = useToast();
  const [qrCodeUrl, setQrCodeUrl] = useState<string>('');

  const createRoomRequestBody = (
    selectedMenu: Menu | null,
    customMenuName: string | null,
    selectedTemperature: TemperatureOption
  ): RoomRequest => {
    return {
      playerName: myName,
      menu: {
        id: selectedMenu ? selectedMenu.id : 0,
        customName: customMenuName,
        temperature: selectedTemperature,
      },
    };
  };

  const createUrl = () => {
    if (playerType === 'HOST') {
      return `/rooms`;
    } else {
      return `/rooms/${joinCode}`;
    }
  };

  const handleRoomRequest = async (
    selectedMenu: Menu | null,
    customMenuName: string | null,
    selectedTemperature: TemperatureOption
  ) => {
    const { joinCode, qrCodeUrl } = await api.post<RoomResponse, RoomRequest>(
      createUrl(),
      createRoomRequestBody(selectedMenu, customMenuName, selectedTemperature)
    );
    setJoinCode(joinCode);
    setQrCodeUrl(qrCodeUrl);
    startSocket(joinCode, myName);
  };

  const validateMenuSelection = (selectedMenu: Menu | null, customMenuName: string | null) => {
    if (!selectedMenu && !customMenuName) {
      showToast({
        type: 'error',
        message: '메뉴를 선택하지 않았습니다.',
      });
      return false;
    }
    return true;
  };

  const validatePlayerName = () => {
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
    if (!validatePlayerName()) return;
    if (!validateMenuSelection(selectedMenu, customMenuName)) return;

    try {
      await handleRoomRequest(selectedMenu, customMenuName, selectedTemperature);
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
    qrCodeUrl,
    proceedToRoom,
  };
};

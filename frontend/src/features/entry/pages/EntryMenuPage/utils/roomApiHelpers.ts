import { Menu, TemperatureOption } from '@/types/menu';
import { PlayerType } from '@/types/player';
import { RoomRequest } from '../hooks/useRoomManagement';

export const createRoomRequestBody = (
  playerName: string,
  selectedMenu: Menu | null,
  customMenuName: string | null,
  selectedTemperature: TemperatureOption
): RoomRequest => {
  return {
    playerName,
    menu: {
      id: selectedMenu ? selectedMenu.id : 0,
      customName: customMenuName,
      temperature: selectedTemperature,
    },
  };
};

export const createUrl = (playerType: PlayerType | null, joinCode: string | null) => {
  if (playerType === null) {
    throw new Error('playerType is null');
  }
  if (joinCode === null) {
    throw new Error('joinCode is null');
  }
  if (joinCode.length < 5) {
    throw new Error('joinCode is less than 5');
  }

  if (playerType === 'HOST') {
    return `/rooms`;
  } else {
    return `/rooms/${joinCode}`;
  }
};

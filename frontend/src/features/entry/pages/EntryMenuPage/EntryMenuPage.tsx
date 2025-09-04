import { api } from '@/apis/rest/api';
import { ApiError, NetworkError } from '@/apis/rest/error';
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import BackButton from '@/components/@common/BackButton/BackButton';
import Button from '@/components/@common/Button/Button';
import Headline3 from '@/components/@common/Headline3/Headline3';
import SelectBox, { Option } from '@/components/@common/SelectBox/SelectBox';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import Layout from '@/layouts/Layout';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import * as S from './EntryMenuPage.styled';
import useToast from '@/components/@common/Toast/useToast';
import SelectCategory from './components/SelectCategory/SelectCategory';
import SelectMenu from './components/SelectMenu/SelectMenu';
import SelectTemperature from './components/SelectMenu/SelectTemperature/SelectTemperature';
import { Category, NewMenu } from '@/types/menu';
import { TemperatureOption } from '@/components/@common/TemperatureToggle/temperatureOption';

// TODO: category 타입 따로 관리 필요 (string이 아니라 유니온 타입으로 지정해서 아이콘 매핑해야함)
type MenusResponse = {
  id: number;
  name: string;
  category: string;
}[];

type CreateRoomRequest = {
  hostName: string;
  menuId: number;
};

type CreateRoomResponse = {
  joinCode: string;
};

type EnterRoomRequest = {
  joinCode: string;
  guestName: string;
  menuId: number;
};

type EnterRoomResponse = {
  joinCode: string;
};

const EntryMenuPage = () => {
  const navigate = useNavigate();
  const { startSocket, isConnected } = useWebSocket();
  const { playerType } = usePlayerType();
  const { joinCode, myName, setJoinCode } = useIdentifier();
  const { showToast } = useToast();
  const [selectedCategory, setSelectedCategory] = useState<Category | null>(null);
  const [selectedMenu, setSelectedMenu] = useState<NewMenu | null>(null);
  const [selectedTemperature, setSelectedTemperature] = useState<TemperatureOption>('ICED');
  const [loading, setLoading] = useState(true);
  const [currentView, setCurrentView] = useState<'category' | 'menu'>('category');

  useEffect(() => {
    if (isConnected) {
      navigate(`/room/${joinCode}/lobby`);
    }
  }, [isConnected, joinCode, navigate]);

  const handleNavigateToBefore = () => {
    if (currentView === 'menu' && !selectedMenu) {
      setCurrentView('category');
    } else if (currentView === 'menu' && selectedMenu) {
      setSelectedMenu(null);
    } else if (currentView === 'category') {
      navigate('/entry/name');
    }
  };

  const handleNavigateToLobby = async () => {
    if (!myName) {
      showToast({
        type: 'error',
        message: '닉네임을 다시 입력해주세요.',
      });
      navigate(-1);
      return;
    }

    if (!selectedMenu) {
      showToast({
        type: 'error',
        message: '메뉴를 선택하지 않았습니다.',
      });
      return;
    }

    const handleHost = async () => {
      // api 형식 수정 필요
      const { joinCode } = await api.post<CreateRoomResponse, CreateRoomRequest>('/rooms', {
        hostName: myName,
        menuId: selectedMenu.id,
      });
      setJoinCode(joinCode);
      startSocket(joinCode, myName);
    };

    const handleGuest = async () => {
      //api 형식 수정 필요
      const { joinCode: _joinCode } = await api.post<EnterRoomResponse, EnterRoomRequest>(
        '/rooms/enter',
        {
          joinCode,
          guestName: myName,
          menuId: selectedMenu.id,
        }
      );
      setJoinCode(_joinCode);
      startSocket(_joinCode, myName);
    };

    try {
      setLoading(true);
      if (playerType === 'HOST') return await handleHost();
      if (playerType === 'GUEST') return await handleGuest();
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
    } finally {
      setLoading(false);
    }
  };

  const isHost = playerType === 'HOST';

  const handleSetSelectedCategory = (category: Category) => {
    setSelectedCategory(category);
    setCurrentView('menu');
  };

  const handleSetSelectedMenu = (menu: NewMenu) => {
    setSelectedMenu(menu);
  };

  const handleChangeTemperature = (temperature: TemperatureOption) => {
    setSelectedTemperature(temperature);
  };

  return (
    <Layout>
      <Layout.TopBar left={<BackButton onClick={handleNavigateToBefore} />} />
      <Layout.Content>
        <S.Container>
          {currentView === 'category' && (
            <SelectCategory onClickCategory={handleSetSelectedCategory} />
          )}
          {currentView === 'menu' && (
            <SelectMenu
              onClickMenu={handleSetSelectedMenu}
              categoryId={selectedCategory?.id ?? 0}
              selectedMenu={selectedMenu}
              selectedTemperature={selectedTemperature}
              onChangeTemperature={handleChangeTemperature}
            />
          )}
        </S.Container>
      </Layout.Content>
      {currentView === 'menu' && selectedMenu && (
        <Layout.ButtonBar>
          <Button onClick={handleNavigateToLobby}>방만들러 가기</Button>
        </Layout.ButtonBar>
      )}
    </Layout>
  );
};

export default EntryMenuPage;

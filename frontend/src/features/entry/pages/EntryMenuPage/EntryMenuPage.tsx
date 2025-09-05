import { api } from '@/apis/rest/api';
import { ApiError, NetworkError } from '@/apis/rest/error';
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import BackButton from '@/components/@common/BackButton/BackButton';
import Button from '@/components/@common/Button/Button';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import Layout from '@/layouts/Layout';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import * as S from './EntryMenuPage.styled';
import useToast from '@/components/@common/Toast/useToast';
import SelectCategory from './components/SelectCategory/SelectCategory';
import SelectMenu from './components/SelectMenu/SelectMenu';
import { Category, NewMenu } from '@/types/menu';
import { TemperatureOption } from '@/components/@common/TemperatureToggle/temperatureOption';

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
};

type CategoriesResponse = Category[];

const EntryMenuPage = () => {
  const navigate = useNavigate();
  const { startSocket, isConnected } = useWebSocket();
  const { playerType } = usePlayerType();
  const { joinCode, myName, setJoinCode } = useIdentifier();
  const { showToast } = useToast();
  const [selectedCategory, setSelectedCategory] = useState<Category | null>(null);
  const [selectedMenu, setSelectedMenu] = useState<NewMenu | null>(null);
  const [customMenuName, setCustomMenuName] = useState<string | null>(null);
  const [selectedTemperature, setSelectedTemperature] = useState<TemperatureOption>('ICED');
  const [loading, setLoading] = useState(true);
  const [currentView, setCurrentView] = useState<'category' | 'menu'>('category');
  const [categories, setCategories] = useState<Category[]>([]);

  useEffect(() => {
    (async () => {
      // const data = await api.get<CategoriesResponse>('/menu-categories');
      // setCategories(data);
      setCategories([
        {
          id: 1,
          name: '커피',
          imgUrl: 'https://picsum.photos/200',
        },
        {
          id: 2,
          name: '스무디',
          imgUrl: 'https://picsum.photos/200',
        },
        {
          id: 3,
          name: '에이드',
          imgUrl: 'https://picsum.photos/200',
        },
        {
          id: 4,
          name: '티',
          imgUrl: 'https://picsum.photos/200',
        },
        {
          id: 5,
          name: '티 라떼',
          imgUrl: 'https://picsum.photos/200',
        },
      ]);
    })();
  }, []);

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

  const createRoomRequestBody = () => {
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

  const handleRoomRequest = async () => {
    const { joinCode } = await api.post<RoomResponse, RoomRequest>(
      createUrl(),
      createRoomRequestBody()
    );
    setJoinCode(joinCode);
    startSocket(joinCode, myName);
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

    try {
      setLoading(true);
      await handleRoomRequest();
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
            <SelectCategory categories={categories} onClickCategory={handleSetSelectedCategory} />
          )}
          {currentView === 'menu' && selectedCategory && (
            <SelectMenu
              onClickMenu={handleSetSelectedMenu}
              selectedCategory={selectedCategory}
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

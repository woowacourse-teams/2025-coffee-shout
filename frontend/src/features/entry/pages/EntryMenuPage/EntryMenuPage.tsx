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
import useToast from '@/components/@common/Toast/useToast';
import SelectCategory from './components/SelectCategory/SelectCategory';
import SelectMenu from './components/SelectMenu/SelectMenu';
import { Category, CategoryWithColor, Menu } from '@/types/menu';
import { TemperatureOption } from '@/types/menu';
import CustomMenuButton from '@/components/@common/CustomMenuButton/CustomMenuButton';
import InputCustomMenu from './components/InputCustomMenu/InputCustomMenu';
import SelectTemperature from './components/SelectTemperature/SelectTemperature';
import { categoryColorList, MenuColorMap } from '@/constants/color';
import * as S from './EntryMenuPage.styled';

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

type CategoriesResponse = Category[];

type CurrentView = 'selectCategory' | 'selectMenu' | 'inputCustomMenu' | 'selectTemperature';

const EntryMenuPage = () => {
  const navigate = useNavigate();
  const { startSocket, isConnected } = useWebSocket();
  const { playerType } = usePlayerType();
  const { joinCode, myName, setJoinCode } = useIdentifier();
  const { showToast } = useToast();
  const [selectedCategory, setSelectedCategory] = useState<CategoryWithColor | null>(null);
  const [selectedMenu, setSelectedMenu] = useState<Menu | null>(null);
  const [customMenuName, setCustomMenuName] = useState<string | null>(null);
  const [isMenuInputCompleted, setIsMenuInputCompleted] = useState(false);
  const [selectedTemperature, setSelectedTemperature] = useState<TemperatureOption>('ICE');
  const [loading, setLoading] = useState(true);
  const [currentView, setCurrentView] = useState<CurrentView>('selectCategory');
  const [categories, setCategories] = useState<CategoryWithColor[]>([]);
  const [qrCodeUrl, setQrCodeUrl] = useState<string>('');

  useEffect(() => {
    (async () => {
      const data = await api.get<CategoriesResponse>('/menu-categories');
      setCategories(
        data.map((category, index) => ({
          ...category,
          color: categoryColorList[index % categoryColorList.length],
        }))
      );
    })();
    setLoading(false);
  }, []);

  useEffect(() => {
    if (isConnected) {
      navigate(`/room/${joinCode}/lobby`, {
        state: {
          qrCodeUrl,
        },
      });
    }
  }, [isConnected, joinCode, navigate, qrCodeUrl]);

  const resetMenuState = () => {
    setSelectedCategory(null);
    setSelectedMenu(null);
    setCustomMenuName(null);
    setIsMenuInputCompleted(false);
    setSelectedTemperature('ICE');
  };

  const handleNavigateToBefore = () => {
    switch (currentView) {
      case 'selectCategory':
        navigate('/entry/name');
        break;
      case 'selectMenu':
        setSelectedMenu(null);
        setCurrentView('selectCategory');
        break;
      case 'inputCustomMenu':
        setCustomMenuName(null);
        setCurrentView('selectCategory');
        break;
      case 'selectTemperature':
        resetMenuState();
        setCurrentView('selectCategory');
        break;
      default:
        navigate('/entry/name');
        break;
    }
  };

  const createRoomRequestBody = (): RoomRequest => {
    const requestBody = {
      playerName: myName,
      menu: {
        id: selectedMenu ? selectedMenu.id : 0,
        customName: customMenuName,
        temperature: selectedTemperature,
      },
    };
    return requestBody;
  };

  const createUrl = () => {
    if (playerType === 'HOST') {
      return `/rooms`;
    } else {
      return `/rooms/${joinCode}`;
    }
  };

  const handleRoomRequest = async () => {
    const { joinCode, qrCodeUrl } = await api.post<RoomResponse, RoomRequest>(
      createUrl(),
      createRoomRequestBody()
    );
    setJoinCode(joinCode);
    setQrCodeUrl(qrCodeUrl);
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

    if (!selectedMenu && !customMenuName) {
      showToast({
        type: 'error',
        message: '메뉴를 선택하지 않았습니다.',
      });
      return;
    }

    try {
      // setLoading(true);
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
      // setLoading(false);
    }
  };

  const handleSetSelectedCategory = (category: CategoryWithColor) => {
    setSelectedCategory(category);
    setCurrentView('selectMenu');
  };

  const handleSetSelectedMenu = (menu: Menu) => {
    setSelectedMenu(menu);
    if (menu.temperatureAvailability === 'ICE_ONLY') {
      setSelectedTemperature('ICE');
    } else if (menu.temperatureAvailability === 'HOT_ONLY') {
      setSelectedTemperature('HOT');
    }
    setCurrentView('selectTemperature');
  };

  const handleChangeTemperature = (temperature: TemperatureOption) => {
    setSelectedTemperature(temperature);
  };

  const handleNavigateToCustomMenu = () => {
    resetMenuState();
    setCurrentView('inputCustomMenu');
  };

  const handleChangeCustomMenuName = (customMenuName: string) => {
    setCustomMenuName(customMenuName);
  };

  const handleClickDoneButton = () => {
    setIsMenuInputCompleted(true);
    setCurrentView('selectTemperature');
  };

  const shouldShowButtonBar = currentView === 'selectTemperature';

  //임시 로딩 컴포넌트
  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <Layout>
      <Layout.TopBar left={<BackButton onClick={handleNavigateToBefore} />} />
      <Layout.Content>
        <S.Container>
          {currentView === 'selectCategory' && (
            <SelectCategory categories={categories} onClickCategory={handleSetSelectedCategory} />
          )}
          {(currentView === 'selectMenu' || currentView === 'selectTemperature') &&
            selectedCategory && (
              <SelectMenu
                onMenuSelect={handleSetSelectedMenu}
                selectedCategory={selectedCategory}
                selectedMenu={selectedMenu}
              >
                {selectedMenu && (
                  <SelectTemperature
                    menuName={selectedMenu.name}
                    temperatureAvailability={selectedMenu.temperatureAvailability}
                    selectedTemperature={selectedTemperature}
                    onChangeTemperature={handleChangeTemperature}
                    selectionCardColor={MenuColorMap[selectedCategory.color]}
                  />
                )}
              </SelectMenu>
            )}
          {(currentView === 'inputCustomMenu' || currentView === 'selectTemperature') &&
            !selectedMenu && (
              <InputCustomMenu
                customMenuName={customMenuName}
                onChangeCustomMenuName={handleChangeCustomMenuName}
                onClickDoneButton={handleClickDoneButton}
                isMenuInputCompleted={isMenuInputCompleted}
              >
                {isMenuInputCompleted && (
                  <SelectTemperature
                    menuName={customMenuName || ''}
                    temperatureAvailability={'BOTH'}
                    selectedTemperature={selectedTemperature}
                    onChangeTemperature={handleChangeTemperature}
                  />
                )}
              </InputCustomMenu>
            )}
        </S.Container>
        {currentView !== 'inputCustomMenu' && currentView !== 'selectTemperature' && (
          <CustomMenuButton onClick={handleNavigateToCustomMenu} />
        )}
      </Layout.Content>
      {shouldShowButtonBar &&
        (playerType === 'HOST' ? (
          <Layout.ButtonBar>
            <Button onClick={handleNavigateToLobby}>방 만들러 가기</Button>
          </Layout.ButtonBar>
        ) : (
          <Layout.ButtonBar>
            <Button onClick={handleNavigateToLobby}>방 참가하기</Button>
          </Layout.ButtonBar>
        ))}
    </Layout>
  );
};

export default EntryMenuPage;

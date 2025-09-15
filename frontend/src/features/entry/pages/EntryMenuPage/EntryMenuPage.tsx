import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import BackButton from '@/components/@common/BackButton/BackButton';
import Button from '@/components/@common/Button/Button';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import Layout from '@/layouts/Layout';
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import SelectCategory from './components/SelectCategory/SelectCategory';
import SelectMenu from './components/SelectMenu/SelectMenu';
import { CategoryWithColor, Menu } from '@/types/menu';
import CustomMenuButton from '@/components/@common/CustomMenuButton/CustomMenuButton';
import InputCustomMenu from './components/InputCustomMenu/InputCustomMenu';
import SelectTemperature from './components/SelectTemperature/SelectTemperature';
import { useMenuSelection } from './hooks/useMenuSelection';
import { useCustomMenu } from './hooks/useCustomMenu';
import { useRoomManagement } from './hooks/useRoomManagement';
import { useViewNavigation } from './hooks/useViewNavigation';
import { useCategories } from './hooks/useCategories';
import * as S from './EntryMenuPage.styled';

const EntryMenuPage = () => {
  const navigate = useNavigate();
  const { isConnected } = useWebSocket();
  const { joinCode } = useIdentifier();
  const { playerType } = usePlayerType();

  const {
    selectedCategory,
    selectedMenu,
    selectedTemperature,
    selectCategory,
    selectMenu,
    selectTemperature,
    resetMenuSelection,
  } = useMenuSelection();

  const {
    customMenuName,
    isCustomMenuInputCompleted,
    setCustomMenuName,
    completeMenuInput,
    resetCustomMenu,
  } = useCustomMenu();

  const {
    currentView,
    navigateToMenu,
    navigateToTemperature,
    navigateToCustomMenu,
    handleNavigateToBefore,
  } = useViewNavigation();

  const { loading, categories } = useCategories();

  const { qrCodeUrl, proceedToRoom } = useRoomManagement();

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
    resetMenuSelection();
    resetCustomMenu();
  };

  const handleCategorySelect = (category: CategoryWithColor) => {
    selectCategory(category);
    navigateToMenu();
  };

  const handleMenuSelect = (menu: Menu) => {
    selectMenu(menu);
    navigateToTemperature();
  };

  const handleGoBack = () => {
    handleNavigateToBefore(resetMenuState, resetCustomMenu);
  };

  const handleCustomMenuClick = () => {
    resetMenuState();
    navigateToCustomMenu();
  };

  const handleCustomMenuDone = () => {
    completeMenuInput();
    navigateToTemperature();
  };

  const handleProceedToRoom = () => {
    proceedToRoom(selectedMenu, customMenuName, selectedTemperature);
  };

  const shouldShowButtonBar = currentView === 'selectTemperature';

  //임시 로딩 컴포넌트
  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <Layout>
      <Layout.TopBar left={<BackButton onClick={handleGoBack} />} />
      <Layout.Content>
        <S.Container>
          {currentView === 'selectCategory' && (
            <SelectCategory categories={categories} onClickCategory={handleCategorySelect} />
          )}
          {(currentView === 'selectMenu' || currentView === 'selectTemperature') &&
            selectedCategory && (
              <SelectMenu
                onMenuSelect={handleMenuSelect}
                selectedCategory={selectedCategory}
                selectedMenu={selectedMenu}
              >
                {selectedMenu && (
                  <SelectTemperature
                    temperatureAvailability={selectedMenu.temperatureAvailability}
                    selectedTemperature={selectedTemperature}
                    onChangeTemperature={selectTemperature}
                  />
                )}
              </SelectMenu>
            )}
          {(currentView === 'inputCustomMenu' || currentView === 'selectTemperature') &&
            !selectedMenu && (
              <InputCustomMenu
                customMenuName={customMenuName}
                onChangeCustomMenuName={setCustomMenuName}
                onClickDoneButton={handleCustomMenuDone}
                isMenuInputCompleted={isCustomMenuInputCompleted}
              >
                {isCustomMenuInputCompleted && (
                  <SelectTemperature
                    temperatureAvailability={'BOTH'}
                    selectedTemperature={selectedTemperature}
                    onChangeTemperature={selectTemperature}
                  />
                )}
              </InputCustomMenu>
            )}
        </S.Container>
        {currentView !== 'inputCustomMenu' && currentView !== 'selectTemperature' && (
          <CustomMenuButton onClick={handleCustomMenuClick} />
        )}
      </Layout.Content>
      {shouldShowButtonBar &&
        (playerType === 'HOST' ? (
          <Layout.ButtonBar>
            <Button onClick={handleProceedToRoom}>방 만들러 가기</Button>
          </Layout.ButtonBar>
        ) : (
          <Layout.ButtonBar>
            <Button onClick={handleProceedToRoom}>방 참가하기</Button>
          </Layout.ButtonBar>
        ))}
    </Layout>
  );
};

export default EntryMenuPage;

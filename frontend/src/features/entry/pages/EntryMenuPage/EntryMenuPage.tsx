import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import BackButton from '@/components/@common/BackButton/BackButton';
import Button from '@/components/@common/Button/Button';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import Layout from '@/layouts/Layout';
import { ChangeEvent, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import SelectCategory from './components/SelectCategory/SelectCategory';
import { CategoryWithColor, Menu } from '@/types/menu';
import CustomMenuButton from '@/components/@common/CustomMenuButton/CustomMenuButton';
import { useMenuFlow } from './hooks/useMenuFlow';
import { useRoomManagement } from './hooks/useRoomManagement';
import { useViewNavigation } from './hooks/useViewNavigation';
import { useCategories } from './hooks/useCategories';
import { useMenus } from './hooks/useMenus';
import * as S from './EntryMenuPage.styled';
import MenuSelectionLayout from './components/MenuSelectionLayout/MenuSelectionLayout';
import SelectTemperature from './components/SelectTemperature/SelectTemperature';
import MenuList from './components/MenuList/MenuList';
import CustomMenuInput from '@/components/@common/CustomMenuInput/CustomMenuInput';

const EntryMenuPage = () => {
  const navigate = useNavigate();
  const { isConnected } = useWebSocket();
  const { joinCode, qrCodeUrl } = useIdentifier();
  const { playerType } = usePlayerType();

  const {
    selectedCategory,
    selectedMenu,
    selectedTemperature,
    customMenuName,
    categorySelection,
    menuSelection,
    temperatureAvailability,
    selectCategory,
    selectMenu,
    selectTemperature,
    setCustomMenuName,
    completeMenuInput,
    resetAll,
  } = useMenuFlow();

  const {
    currentView,
    navigateToMenu,
    navigateToTemperature,
    navigateToCustomMenu,
    handleNavigateToBefore,
  } = useViewNavigation();

  const { categories } = useCategories();
  const { menus, resetMenus } = useMenus(selectedCategory?.id ?? null);

  const { proceedToRoom } = useRoomManagement();

  useEffect(() => {
    const isReadyToNavigateLobby =
      joinCode && qrCodeUrl && (selectedMenu || customMenuName) && isConnected;
    if (isReadyToNavigateLobby) {
      navigate(`/room/${joinCode}/lobby`);
    }
  }, [joinCode, qrCodeUrl, selectedMenu, customMenuName, isConnected, navigate]);

  const resetMenuState = () => {
    resetAll();
    resetMenus();
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
    handleNavigateToBefore(resetMenuState);
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

  const handleChangeCustomMenuInput = (e: ChangeEvent<HTMLInputElement>) => {
    setCustomMenuName(e.target.value);
  };

  const viewChildren = {
    selectMenu: <MenuList menus={menus} onClickMenu={handleMenuSelect} />,
    selectTemperature: (
      <SelectTemperature
        temperatureAvailability={temperatureAvailability}
        selectedTemperature={selectedTemperature}
        onChangeTemperature={selectTemperature}
      />
    ),
    inputCustomMenu: (
      <CustomMenuInput
        placeholder="메뉴를 입력해주세요"
        value={customMenuName}
        onChange={handleChangeCustomMenuInput}
        onClickDoneButton={handleCustomMenuDone}
      />
    ),
  };

  const shouldShowButtonBar = currentView === 'selectTemperature';

  const shouldShowCustomMenuButton =
    currentView === 'selectCategory' || currentView === 'selectMenu';

  return (
    <Layout>
      <Layout.TopBar left={<BackButton onClick={handleGoBack} />} />
      <Layout.Content>
        <S.Container>
          {currentView === 'selectCategory' ? (
            <SelectCategory categories={categories} onClickCategory={handleCategorySelect} />
          ) : (
            <MenuSelectionLayout
              categorySelection={categorySelection}
              menuSelection={menuSelection}
              showSelectedMenuCard={currentView === 'selectTemperature'}
            >
              {viewChildren[currentView]}
            </MenuSelectionLayout>
          )}
        </S.Container>
        {shouldShowCustomMenuButton && <CustomMenuButton onClick={handleCustomMenuClick} />}
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

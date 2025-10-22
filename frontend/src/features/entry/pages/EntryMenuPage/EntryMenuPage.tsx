import BackButton from '@/components/@common/BackButton/BackButton';
import Button from '@/components/@common/Button/Button';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import Layout from '@/layouts/Layout';
import { ChangeEvent, useEffect, useState } from 'react';
import useAutoFocus from '@/hooks/useAutoFocus';
import SelectCategory from './components/SelectCategory/SelectCategory';
import { CategoryWithColor, Menu } from '@/types/menu';
import CustomMenuButton from '@/components/@common/CustomMenuButton/CustomMenuButton';
import { useMenuFlow } from './hooks/useMenuFlow';
import { useRoomManagement } from './hooks/useRoomManagement';
import { useViewNavigation } from './hooks/useViewNavigation';
import * as S from './EntryMenuPage.styled';
import MenuSelectionLayout from './components/MenuSelectionLayout/MenuSelectionLayout';
import SelectTemperature from './components/SelectTemperature/SelectTemperature';
import MenuList from './components/MenuList/MenuList';
import CustomMenuInput from '@/components/@common/CustomMenuInput/CustomMenuInput';
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useNavigate } from 'react-router-dom';
import Headline3 from '@/components/@common/Headline3/Headline3';
import LocalErrorBoundary from '@/components/@common/ErrorBoundary/LocalErrorBoundary';

const EntryMenuPage = () => {
  const navigate = useNavigate();
  const { playerType } = usePlayerType();
  const { isConnected } = useWebSocket();
  const { joinCode } = useIdentifier();
  const [isRoomLoading, setIsRoomLoading] = useState(false);
  const liveRef = useAutoFocus<HTMLHeadingElement>();

  const {
    category,
    menu,
    temperature,
    customMenu,
    categorySelection,
    menuSelection,
    temperatureAvailability,
    resetAll,
  } = useMenuFlow();

  const {
    currentView,
    navigateToMenu,
    navigateToTemperature,
    navigateToCustomMenu,
    handleNavigateToBefore,
  } = useViewNavigation();

  const { proceedToRoom, isLoading, error } = useRoomManagement();

  useEffect(() => {
    if (isLoading) setIsRoomLoading(true);
    if (error) setIsRoomLoading(false);
  }, [isLoading, error]);

  useEffect(() => {
    const isReadyToNavigateLobby = joinCode && (menu.value || customMenu.value) && isConnected;
    if (isReadyToNavigateLobby) {
      navigate(`/room/${joinCode}/lobby`);
    }
  }, [joinCode, menu.value, customMenu.value, isConnected, navigate]);

  const resetMenuState = () => {
    resetAll();
  };

  const handleCategorySelect = (categoryItem: CategoryWithColor) => {
    category.set(categoryItem);
    navigateToMenu();
  };

  const handleMenuSelect = (menuItem: Menu) => {
    menu.set(menuItem);
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
    customMenu.complete();
    navigateToTemperature();
  };

  const handleProceedToRoom = () => {
    proceedToRoom(menu.value, customMenu.value, temperature.value);
  };

  const handleChangeCustomMenuInput = (e: ChangeEvent<HTMLInputElement>) => {
    customMenu.set(e.target.value);
  };

  const viewChildren = {
    selectMenu: (
      <LocalErrorBoundary>
        <MenuList categoryId={category.value?.id ?? null} onClickMenu={handleMenuSelect} />
      </LocalErrorBoundary>
    ),
    selectTemperature: (
      <SelectTemperature
        temperatureAvailability={temperatureAvailability}
        selectedTemperature={temperature.value}
        onChangeTemperature={temperature.set}
      />
    ),
    inputCustomMenu: (
      <CustomMenuInput
        placeholder="메뉴를 입력해주세요"
        value={customMenu.value}
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
            <>
              <Headline3 ref={liveRef} tabIndex={0}>
                카테고리를 선택해주세요
              </Headline3>
              <LocalErrorBoundary>
                <SelectCategory onClickCategory={handleCategorySelect} />
              </LocalErrorBoundary>
            </>
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
      {shouldShowButtonBar && (
        <Layout.ButtonBar>
          {playerType === 'HOST' ? (
            <Button onClick={handleProceedToRoom} isLoading={isRoomLoading}>
              방 만들러 가기
            </Button>
          ) : (
            <Button onClick={handleProceedToRoom} isLoading={isRoomLoading}>
              방 참가하기
            </Button>
          )}
        </Layout.ButtonBar>
      )}
    </Layout>
  );
};

export default EntryMenuPage;

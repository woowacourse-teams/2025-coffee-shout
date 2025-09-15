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
import { useMenuSelection } from './hooks/useMenuSelection';
import { useCustomMenu } from './hooks/useCustomMenu';
import { useRoomManagement } from './hooks/useRoomManagement';
import { useViewNavigation } from './hooks/useViewNavigation';
import { useCategories } from './hooks/useCategories';
import { useMenu } from './hooks/useMenu';
import * as S from './EntryMenuPage.styled';
import MenuSelectionLayout from './components/MenuSelectionLayout/MenuSelectionLayout';
import { MenuColorMap } from '@/constants/color';
import { theme } from '@/styles/theme';
import SelectTemperature from './components/SelectTemperature/SelectTemperature';
import MenuList from './components/MenuList/MenuList';
import CustomMenuInput from '@/components/@common/CustomMenuInput/CustomMenuIntput';
import CustomMenuIcon from '@/assets/custom-menu-icon.svg';

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

  const { customMenuName, setCustomMenuName, completeMenuInput, resetCustomMenu } = useCustomMenu();

  const {
    currentView,
    navigateToMenu,
    navigateToTemperature,
    navigateToCustomMenu,
    handleNavigateToBefore,
  } = useViewNavigation();

  const { loading: categoriesLoading, categories, error: categoriesError } = useCategories();
  const { menus, loading: menusLoading, error: menusError } = useMenu(selectedCategory?.id ?? null);

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

  const handleChangeCustomMenuInput = (e: ChangeEvent<HTMLInputElement>) => {
    setCustomMenuName(e.target.value);
  };

  const viewChildren = {
    selectMenu: <MenuList menus={menus} onClickMenu={handleMenuSelect} />,
    selectTemperature: (
      <SelectTemperature
        temperatureAvailability={selectedMenu?.temperatureAvailability ?? 'BOTH'}
        selectedTemperature={selectedTemperature}
        onChangeTemperature={selectTemperature}
      />
    ),
    inputCustomMenu: (
      <CustomMenuInput
        placeholder="메뉴를 입력해주세요"
        value={customMenuName || ''}
        onChange={handleChangeCustomMenuInput}
        onClickDoneButton={handleCustomMenuDone}
      />
    ),
  };

  const categorySelection = {
    color: selectedCategory?.color ?? theme.color.point[200],
    name: selectedCategory?.name ?? '직접입력',
    imageUrl: selectedCategory?.imageUrl ?? CustomMenuIcon,
  };

  const menuSelection = {
    color: MenuColorMap[selectedCategory?.color ?? theme.color.point[200]],
    name: selectedMenu?.name ?? customMenuName ?? '',
  };

  const shouldShowButtonBar = currentView === 'selectTemperature';

  const shouldShowCustomMenuButton =
    currentView !== 'inputCustomMenu' && currentView !== 'selectTemperature';
  //임시 로딩 컴포넌트
  if (categoriesLoading || menusLoading) {
    return <div>Loading...</div>;
  }

  // 임시 에러 처리
  if (categoriesError) {
    return <div>카테고리를 불러오는데 실패했습니다: {categoriesError.message}</div>;
  }

  if (menusError) {
    return <div>메뉴를 불러오는데 실패했습니다: {menusError.message}</div>;
  }

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

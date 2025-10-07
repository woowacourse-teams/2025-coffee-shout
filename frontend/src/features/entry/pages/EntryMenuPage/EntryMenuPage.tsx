import BackButton from '@/components/@common/BackButton/BackButton';
import Button from '@/components/@common/Button/Button';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import Layout from '@/layouts/Layout';
import { ChangeEvent } from 'react';
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
  const { playerType } = usePlayerType();

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

  const { categories } = useCategories();
  const { menus } = useMenus(category.value?.id ?? null);

  const { proceedToRoom, isLoading: isRoomLoading } = useRoomManagement();

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
    selectMenu: <MenuList menus={menus} onClickMenu={handleMenuSelect} />,
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
            <Button onClick={handleProceedToRoom} isLoading={isRoomLoading}>
              방 만들러 가기
            </Button>
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

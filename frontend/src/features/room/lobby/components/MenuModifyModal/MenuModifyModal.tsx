import useFetch from '@/apis/rest/useFetch';
import Button from '@/components/@common/Button/Button';
import Paragraph from '@/components/@common/Paragraph/Paragraph';
import SelectBox, { Option } from '@/components/@common/SelectBox/SelectBox';
import { useState } from 'react';
import * as S from './MenuModifyModal.styled';
import { Menu } from '@/types/menu';
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';

type Props = {
  myMenu: string;
  onClose: () => void;
};

const MenuModifyModal = ({ myMenu, onClose }: Props) => {
  const [modifiedMenu, setModifiedMenu] = useState<Option>({
    id: -1,
    name: myMenu,
  });
  const { send } = useWebSocket();
  const { myName, joinCode } = useIdentifier();

  const {
    data: menus,
    loading,
    error,
  } = useFetch<Menu[]>({
    endpoint: '/menus',
  });

  const coffeeOptions =
    menus?.map((menu) => ({
      id: menu.id,
      name: menu.name,
    })) || [];

  const handleModify = async () => {
    if (!modifiedMenu) {
      alert('변경할 메뉴를 선택해주세요.');
      return;
    }

    send(`/room/${joinCode}/update-menus`, {
      playerName: myName,
      menuId: modifiedMenu.id,
    });

    onClose();
  };

  if (loading) return <div>로딩 중...</div>;
  if (error) return <div>{error.message}</div>;

  return (
    <S.Container>
      <Paragraph>변경할 메뉴를 선택해주세요</Paragraph>
      <SelectBox
        value={modifiedMenu.name}
        options={coffeeOptions}
        onChange={(value) => setModifiedMenu(value)}
      />
      <S.ButtonContainer>
        <Button variant="secondary" onClick={onClose}>
          취소
        </Button>
        <Button variant="primary" onClick={handleModify}>
          변경
        </Button>
      </S.ButtonContainer>
    </S.Container>
  );
};

export default MenuModifyModal;

import { api } from '@/apis/rest/api';
import { ApiError, NetworkError } from '@/apis/rest/error';
import Button from '@/components/@common/Button/Button';
import Paragraph from '@/components/@common/Paragraph/Paragraph';
import SelectBox, { Option } from '@/components/@common/SelectBox/SelectBox';
import { useEffect, useState } from 'react';
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
  const [coffeeOptions, setCoffeeOptions] = useState<Option[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { send } = useWebSocket();
  const { myName, joinCode } = useIdentifier();

  useEffect(() => {
    (async () => {
      try {
        setLoading(true);

        const menus = await api.get<Menu[]>('/menus');
        const options = menus.map((menu) => ({
          id: menu.id,
          name: menu.name,
        }));
        setCoffeeOptions(options);
      } catch (error) {
        if (error instanceof ApiError) {
          setError(error.message);
        } else if (error instanceof NetworkError) {
          setError('네트워크 연결을 확인해주세요');
        } else {
          setError('알 수 없는 오류가 발생했습니다');
        }
      } finally {
        setLoading(false);
      }
    })();
  }, []);

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

  return (
    <S.Container>
      <Paragraph>변경할 메뉴를 선택해주세요</Paragraph>
      {loading ? (
        <div>로딩 중...</div>
      ) : error ? (
        <div>{error}</div>
      ) : (
        <SelectBox
          value={modifiedMenu.name}
          options={coffeeOptions}
          onChange={(value) => setModifiedMenu(value)}
        />
      )}
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

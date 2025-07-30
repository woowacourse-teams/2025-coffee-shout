import { api } from '@/apis/rest/api';
import { ApiError, NetworkError } from '@/apis/rest/error';
import Button from '@/components/@common/Button/Button';
import Paragraph from '@/components/@common/Paragraph/Paragraph';
import SelectBox, { Option } from '@/components/@common/SelectBox/SelectBox';
import { useEffect, useState } from 'react';
import * as S from './MenuModifyModal.styled';

// TODO: category 타입 따로 관리 필요 (string이 아니라 유니온 타입으로 지정해서 아이콘 매핑해야함)
type MenusResponse = {
  id: number;
  name: string;
  category: string;
}[];

type Props = {
  onClose: () => void;
};

const MenuModifyModal = ({ onClose }: Props) => {
  // TODO: 현재 메뉴를 초깃값으로 설정 (웹소켓: 현재 본인 메뉴)
  const [modifiedMenu, setModifiedMenu] = useState<Option>({
    id: -1,
    name: '',
  });
  const [coffeeOptions, setCoffeeOptions] = useState<Option[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        setLoading(true);

        const menus = await api.get<MenusResponse>('/menus');
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

    // TODO 메뉴 변경 api 호출
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

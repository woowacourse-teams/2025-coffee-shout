import Button from '@/components/@common/Button/Button';
import Paragraph from '@/components/@common/Paragraph/Paragraph';
import { useState } from 'react';
import * as S from './MenuModifyModal.styled';
import SelectBox, { Option } from '@/components/@common/SelectBox/SelectBox';

type Props = {
  onClose: () => void;
};

const coffeeOptions: Option[] = [
  { value: 'americano', label: '아이스 아메리카노' },
  { value: 'latte', label: '카페 라떼' },
  { value: 'cappuccino', label: '카푸치노' },
  { value: 'macchiato', label: '마끼아또' },
  { value: 'mocha', label: '카페 모카' },
  { value: 'espresso', label: '에스프레소', disabled: true },
];

const MenuModifyModal = ({ onClose }: Props) => {
  // TODO 현재 메뉴를 초깃값으로 설정
  const [modifiedMenu, setModifiedMenu] = useState<string>('');

  const handleModify = () => {
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
      <SelectBox
        value={modifiedMenu}
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

import BackButton from '@/components/@common/BackButton/BackButton';
import Button from '@/components/@common/Button/Button';
import Headline3 from '@/components/@common/Headline3/Headline3';
import SelectBox, { Option } from '@/components/@common/SelectBox/SelectBox';
import Layout from '@/layouts/Layout';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import * as S from './EntryMenuPage.styled';

// TODO: 서버 데이터로 변경
const coffeeOptions: Option[] = [
  { value: 'americano', label: '아이스 아메리카노' },
  { value: 'latte', label: '카페 라떼' },
  { value: 'cappuccino', label: '카푸치노' },
  { value: 'macchiato', label: '마끼아또' },
  { value: 'mocha', label: '카페 모카' },
  { value: 'espresso', label: '에스프레소', disabled: true },
];

const EntryMenuPage = () => {
  const [selectedValue, setSelectedValue] = useState('');
  const navigate = useNavigate();

  const handleNavigateToName = () => navigate('/entry/name');
  const handleNavigateToLobby = () => navigate('/room/:roomId/lobby');
  const isButtonDisabled = selectedValue === '';

  return (
    <Layout>
      <Layout.TopBar left={<BackButton onClick={handleNavigateToName} />} />
      <Layout.Content>
        <S.Container>
          <Headline3>메뉴를 선택해주세요</Headline3>
          <SelectBox
            options={coffeeOptions}
            value={selectedValue}
            onChange={setSelectedValue}
            placeholder="메뉴를 선택해주세요"
          />
        </S.Container>
      </Layout.Content>
      <Layout.ButtonBar>
        <Button variant={isButtonDisabled ? 'disabled' : 'primary'} onClick={handleNavigateToLobby}>
          방 만들러 가기
        </Button>
      </Layout.ButtonBar>
    </Layout>
  );
};

export default EntryMenuPage;

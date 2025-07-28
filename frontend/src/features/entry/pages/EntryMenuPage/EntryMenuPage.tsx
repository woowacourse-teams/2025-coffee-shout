import { api } from '@/apis/api';
import { ApiError, NetworkError } from '@/apis/error';
import BackButton from '@/components/@common/BackButton/BackButton';
import Button from '@/components/@common/Button/Button';
import Headline3 from '@/components/@common/Headline3/Headline3';
import SelectBox, { Option } from '@/components/@common/SelectBox/SelectBox';
import Layout from '@/layouts/Layout';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import * as S from './EntryMenuPage.styled';

// TODO: category 타입 따로 관리 필요 (string이 아니라 유니온 타입으로 지정해서 아이콘 매핑해야함)
type Menus = { id: number; name: string; category: string }[];

const EntryMenuPage = () => {
  const [selectedValue, setSelectedValue] = useState('');
  const [coffeeOptions, setCoffeeOptions] = useState<Option[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    (async () => {
      try {
        setLoading(true);

        const menus = await api.get<Menus>('http://api.coffee-shout.com/menus');
        const options = menus.map((menu) => ({
          value: String(menu.id),
          label: menu.name,
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

  const handleNavigateToName = () => navigate('/entry/name');
  const handleNavigateToLobby = () => navigate('/room/:roomId/lobby');
  const isButtonDisabled = selectedValue === '';

  return (
    <Layout>
      <Layout.TopBar left={<BackButton onClick={handleNavigateToName} />} />
      <Layout.Content>
        <S.Container>
          <Headline3>메뉴를 선택해주세요</Headline3>
          {loading ? (
            <div>로딩 중...</div>
          ) : error ? (
            <div>{error}</div>
          ) : (
            <SelectBox
              options={coffeeOptions}
              value={selectedValue}
              onChange={setSelectedValue}
              placeholder="메뉴를 선택해주세요"
            />
          )}
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

import { api } from '@/apis/rest/api';
import { ApiError, NetworkError } from '@/apis/rest/error';
import BackButton from '@/components/@common/BackButton/BackButton';
import Button from '@/components/@common/Button/Button';
import Headline3 from '@/components/@common/Headline3/Headline3';
import SelectBox, { Option } from '@/components/@common/SelectBox/SelectBox';
import Layout from '@/layouts/Layout';
import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import * as S from './EntryMenuPage.styled';
import { useWebSocket } from '@/contexts/WebSocket/WebSocketContext';

export let _joinCode: string | null = null;

// TODO: category 타입 따로 관리 필요 (string이 아니라 유니온 타입으로 지정해서 아이콘 매핑해야함)
type MenusResponse = {
  id: number;
  name: string;
  category: string;
}[];

type CreateRoomRequest = {
  hostName: string;
  menuId: number;
};

type CreateRoomResponse = {
  joinCode: string;
};

const EntryMenuPage = () => {
  const { connect } = useWebSocket();

  const [selectedValue, setSelectedValue] = useState<Option>({
    id: -1,
    name: '',
  });

  const [coffeeOptions, setCoffeeOptions] = useState<Option[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const { state } = useLocation();

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

  const handleNavigateToName = () => navigate('/entry/name');
  const handleNavigateToLobby = async () => {
    if (!state.name) {
      alert('닉네임을 다시 입력해주세요.');
      navigate(-1);
      return;
    }

    const menuId = selectedValue.id;
    if (menuId === -1) {
      alert('메뉴를 선택하지 않았습니다.');
      return;
    }

    try {
      const { joinCode } = await api.post<CreateRoomResponse, CreateRoomRequest>('/rooms', {
        hostName: state.name,
        menuId,
      });
      //삭제 필요
      _joinCode = joinCode;

      // 웹소켓 연결
      connect();

      navigate('/room/:roomId/lobby', {
        state: {
          joinCode,
        },
      });
    } catch (error) {
      if (error instanceof ApiError) {
        alert(error.message);
      } else if (error instanceof NetworkError) {
        alert('네트워크 연결을 확인해주세요');
      } else {
        alert('방 생성 중 오류가 발생했습니다');
      }
    }
  };

  const isButtonDisabled = selectedValue.name === '';

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
              value={selectedValue.name}
              onChange={(value: Option) => setSelectedValue(value)}
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

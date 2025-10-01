import styled from '@emotion/styled';

export const Container = styled.div`
  position: absolute;
  top: 70px;
  left: 20px;
  min-width: 160px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.26), rgba(228, 221, 221, 0.65));
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  z-index: 200;
`;

export const Title = styled.div`
  font-size: 18px;
  font-weight: 700;
  color: #333;
  text-align: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e0e0e0;
`;

export const RankList = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;
`;

export const RankItem = styled.div<{ $rank: number; $isMe: boolean }>`
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background-color: ${({ $isMe }) => ($isMe ? 'rgba(76, 175, 80, 0.15)' : 'rgba(0, 0, 0, 0.03)')};
  border: 2px solid ${({ $isMe }) => ($isMe ? '#4caf50' : 'transparent')};
  border-radius: 12px;
  transition: all 0.3s ease;

  &:hover {
    background-color: ${({ $isMe }) => ($isMe ? 'rgba(76, 175, 80, 0.25)' : 'rgba(0, 0, 0, 0.08)')};
    transform: translateX(-2px);
  }
`;

export const RankNumber = styled.div<{ $rank: number }>`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  background: ${({ $rank }) => {
    if ($rank === 1) return 'linear-gradient(135deg, #ffd700, #ffed4e)';
    if ($rank === 2) return 'linear-gradient(135deg, #c0c0c0, #e8e8e8)';
    if ($rank === 3) return 'linear-gradient(135deg, #cd7f32, #e3a869)';
    return '#999';
  }};
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
`;

export const PlayerName = styled.div<{ $isMe: boolean }>`
  flex: 1;
  font-size: 15px;
  font-weight: ${({ $isMe }) => ($isMe ? '700' : '500')};
  color: ${({ $isMe }) => ($isMe ? '#4caf50' : '#333')};
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
`;

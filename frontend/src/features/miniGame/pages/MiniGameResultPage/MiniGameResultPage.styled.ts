import styled from '@emotion/styled';

export const Banner = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 10px;
  height: 100%;
  text-align: center;
`;

export const DescriptionWrapper = styled.div`
  display: flex;
  flex-direction: column;
  gap: 5px;
`;

export const ResultList = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;
  height: 100%;
  overflow: scroll;
`;

export const PlayerCardWrapper = styled.div<{ isHighlighted?: boolean }>`
  display: flex;
  align-items: center;
  padding: 0 20px 0 8px;
  gap: 24px;
  border-radius: 12px;
  background-color: ${({ isHighlighted, theme }) =>
    isHighlighted ? theme.color.point[100] : 'transparent'};
`;

const rankColorMap: Record<number, string> = {
  1: '#FFDE65',
  2: '#E5E7EB',
  3: '#FFC8A4',
};

export const RankNumber = styled.div<{ rank: number }>`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 35px;
  height: 35px;
  border-radius: 12px;
  background-color: ${({ rank }) => rankColorMap[rank] ?? 'none'};
  font-size: 20px;
  font-weight: 600;
  color: ${({ rank }) => (rank <= 3 ? '#FFFFFF' : '#666')};
`;

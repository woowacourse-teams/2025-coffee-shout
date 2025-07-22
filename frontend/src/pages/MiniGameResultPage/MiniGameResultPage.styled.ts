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
`;

export const PlayerCardWrapper = styled.div<{ isHighlighted?: boolean }>`
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 12px;
  background-color: ${({ isHighlighted, theme }) =>
    isHighlighted ? theme.color.point[100] : 'transparent'};
`;

export const RankNumber = styled.div<{ rank: number }>`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 35px;
  height: 35px;
  border-radius: 12px;
  background-color: ${({ rank }) => {
    switch (rank) {
      case 1:
        return '#FFDE65';
      case 2:
        return '#E5E7EB';
      case 3:
        return '#FFC8A4';
      default:
        return 'none';
    }
  }};
  font-size: 20px;
  font-weight: 600;
  color: ${({ rank }) => (rank <= 3 ? '#FFFFFF' : '#666')};
`;

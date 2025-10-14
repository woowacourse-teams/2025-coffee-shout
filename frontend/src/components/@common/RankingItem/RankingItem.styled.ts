import styled from '@emotion/styled';

const rankColorMap: Record<number, string> = {
  1: '#FFDE65',
  2: '#E5E7EB',
  3: '#FFC8A4',
};

type Props = {
  $rank: number;
};

export const Container = styled.div`
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background-color: ${({ theme }) => theme.color.white};
  padding: 0.3rem;
  border-radius: 8px;
`;

export const RankNumber = styled.div<Props>`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 35px;
  height: 35px;
  border-radius: 12px;
  background-color: ${({ $rank }) => rankColorMap[$rank] ?? '#ffffff'};
  flex-shrink: 0;
`;

export const Content = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 1;
  gap: 1rem;
`;

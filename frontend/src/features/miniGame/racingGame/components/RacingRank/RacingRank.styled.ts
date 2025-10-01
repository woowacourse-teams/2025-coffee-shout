import styled from '@emotion/styled';

export const Container = styled.div`
  position: absolute;
  top: 70px;
  left: 20px;
  min-width: 160px;
  z-index: 200;
`;

export const RankList = styled.div`
  display: flex;
  flex-direction: column;
`;

export const RankNumber = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 1rem;
  height: auto;
`;
export const RankItem = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
`;

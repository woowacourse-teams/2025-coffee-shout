import styled from '@emotion/styled';

export const Container = styled.div`
  position: absolute;
  left: 20px;
  z-index: 200;
  top: 4rem;
  left: 1rem;
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
  width: fit-content;
  min-width: max-content;
  background: linear-gradient(to right, rgba(19, 8, 8, 0.56) 0%, rgba(46, 35, 35, 0.19) 100%);
  padding: 2px;
`;

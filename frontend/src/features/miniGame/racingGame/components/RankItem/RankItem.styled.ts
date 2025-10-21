import styled from '@emotion/styled';

type Props = {
  $isFixed?: boolean;
};

export const Container = styled.div<Props>`
  display: flex;
  align-items: center;
  gap: 8px;
  width: fit-content;
  min-width: max-content;
  background: ${({ $isFixed }) =>
    $isFixed
      ? 'linear-gradient(to right, rgba(255, 255, 255, 0.8) 0%, rgba(255, 255, 255, 0.4) 100%)'
      : 'linear-gradient(to right, rgba(19, 8, 8, 0.56) 0%, rgba(46, 35, 35, 0.19) 100%)'};
  padding: 2px;
`;

export const RankNumber = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 1rem;
  height: auto;
`;

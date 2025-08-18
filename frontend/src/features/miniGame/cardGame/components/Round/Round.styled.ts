import styled from '@emotion/styled';

export const TitleContainer = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  position: relative;
`;

export const TitleWrapper = styled.div`
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  justify-content: center;
  align-items: center;
`;

export const CircularProgressWrapper = styled.div`
  position: absolute;
  right: 1rem;
`;

export const MyCardContainer = styled.div`
  display: flex;
  gap: 0.625rem;
  justify-content: center;
  align-items: center;
  padding: 2rem 0 2.5rem;
`;

export const CardContainer = styled.div`
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  place-items: center;
  margin: 0 auto;
  gap: 0.625rem;
`;

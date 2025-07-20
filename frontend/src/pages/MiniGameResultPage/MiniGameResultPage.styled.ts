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

export const Description = styled.p`
  ${({ theme }) => theme.typography.small}
  color: white;
`;

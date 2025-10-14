import styled from '@emotion/styled';

export const Container = styled.div`
  background-color: rgb(255, 255, 255);
  width: fit-content;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  height: 30px;
  color: ${({ theme }) => theme.color.white};
  display: flex;
  align-items: center;
  justify-content: center;
`;

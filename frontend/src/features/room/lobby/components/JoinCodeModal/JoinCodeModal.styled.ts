import styled from '@emotion/styled';

export const Container = styled.div`
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 0.5rem 0;
`;

export const Wrapper = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 8px;
  padding: 10px 0;
`;

export const CodeBox = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: ${({ theme }) => theme.color.gray[100]};
  padding: 1rem;
  border-radius: 12px;
`;

export const EmptyBox = styled.div`
  width: 20px;
`;

export const CopyIcon = styled.img`
  width: 20px;
  cursor: pointer;
`;

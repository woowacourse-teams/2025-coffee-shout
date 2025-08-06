import styled from '@emotion/styled';

export const PaginationContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-around;
`;

export const PaginationButton = styled.button`
  background: none;
  border: none;
  color: #333;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  min-width: 60px;

  &:hover:not(:disabled) {
    background: #f5f5f5;
  }

  &:disabled {
    color: #ccc;
    cursor: not-allowed;
  }
`;

export const DotsContainer = styled.div`
  display: flex;
  gap: 8px;
`;

export const Dot = styled.div<{ active: boolean }>`
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: ${(props) => (props.active ? '#ff6b6b' : '#ddd')};
  transition: all 0.2s ease;
`;

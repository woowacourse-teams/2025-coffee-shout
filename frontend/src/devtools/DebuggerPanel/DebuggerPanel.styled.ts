import styled from '@emotion/styled';

export const ToggleButton = styled.button<{ $isOpen: boolean }>`
  position: fixed;
  right: ${({ $isOpen }) => ($isOpen ? '400px' : '0')};
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 64px;
  background: #fff;
  border: 1px solid #ddd;
  border-right: none;
  border-radius: 4px 0 0 4px;
  cursor: pointer;
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  box-shadow: -2px 0 4px rgba(0, 0, 0, 0.1);
  transition: right 0.2s;

  &:hover {
    background: #f5f5f5;
  }
`;

export const Container = styled.div`
  position: fixed;
  right: 0;
  top: 0;
  width: 400px;
  height: 100vh;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  border-left: 1px solid #ddd;
  background: transparent;
`;

export const Header = styled.div`
  padding: 12px 16px;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid #ddd;
  background: transparent;
`;

export const FilterBar = styled.div`
  padding: 12px 16px;
  border-bottom: 1px solid #ddd;
  display: flex;
  gap: 16px;
  background: transparent;

  label {
    font-size: 12px;
    margin-right: 8px;
  }

  select {
    padding: 4px 8px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 12px;
    background: #fff;
    cursor: pointer;

    &:hover {
      border-color: #999;
    }
  }
`;

export const ListContainer = styled.div`
  flex: 1;
  overflow-y: auto;
  padding: 8px;
`;

export const RequestItem = styled.div`
  padding: 8px;
  margin-bottom: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: transparent;

  &:hover {
    background: #f9f9f9;
  }
`;

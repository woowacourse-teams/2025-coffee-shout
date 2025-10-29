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
  cursor: pointer;

  &:hover {
    background: #f9f9f9;
  }
`;

export const DetailContainer = styled.div`
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: transparent;
  display: flex;
  flex-direction: column;
  z-index: 100;
`;

export const TabBar = styled.div`
  display: flex;
  border-bottom: 1px solid #ddd;
  background: transparent;
`;

export const Tab = styled.button<{ $active: boolean }>`
  padding: 8px 16px;
  border: none;
  border-bottom: 2px solid ${({ $active }) => ($active ? '#1976D2' : 'transparent')};
  background: transparent;
  cursor: pointer;
  font-size: 12px;
  color: ${({ $active }) => ($active ? '#1976D2' : '#666')};
  font-weight: ${({ $active }) => ($active ? 600 : 400)};

  &:hover {
    background: #f5f5f5;
  }
`;

export const TabContent = styled.div`
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: transparent;
`;

export const HeaderRow = styled.div`
  display: flex;
  padding: 4px 0;
  font-size: 12px;
  border-bottom: 1px solid #f0f0f0;
`;

export const HeaderKey = styled.div`
  min-width: 180px;
  color: #666;
  font-weight: 500;
`;

export const HeaderValue = styled.div`
  flex: 1;
  color: #333;
  word-break: break-all;
`;

export const CodeBlock = styled.pre`
  background: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  font-size: 11px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  overflow-x: auto;
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  color: #333;
`;

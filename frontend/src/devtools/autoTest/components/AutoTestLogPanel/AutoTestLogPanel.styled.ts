import styled from '@emotion/styled';

export const ToggleButton = styled.button`
  appearance: none;
  border: 1px solid rgba(0, 0, 0, 0.12);
  background: #ffffff;
  color: #222;
  padding: 6px 10px;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.15s ease;

  &:hover {
    background: #f6f6f6;
  }
`;

export const Panel = styled.div<{ height: number }>`
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: ${({ height }) => height}px;
  z-index: 1000;
  background: #ffffff;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  font-family: 'Segoe UI', system-ui, sans-serif;
  font-size: 12px;
`;

export const ResizeHandle = styled.div`
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 5px;
  cursor: ns-resize;
  z-index: 1001;
  background: transparent;
  touch-action: none;

  &:hover {
    background: rgba(0, 0, 0, 0.05);
  }
`;

export const Header = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  background: #f8f9fa;
  gap: 12px;
`;

export const Title = styled.h3`
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: #222;
`;

export const HeaderActions = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
`;

export const CloseButton = styled.button`
  appearance: none;
  border: none;
  background: transparent;
  color: #666;
  padding: 4px;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;

  &:hover {
    background: rgba(0, 0, 0, 0.05);
  }
`;

export const Content = styled.div`
  flex: 1;
  overflow-y: auto;
`;

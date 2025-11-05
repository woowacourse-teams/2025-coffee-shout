import styled from '@emotion/styled';

export const Container = styled.div`
  position: relative;
`;

export const ToggleBar = styled.div`
  position: fixed;
  top: 8px;
  right: 12px;
  z-index: 1001;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  padding: 0;
  background: transparent;
`;

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

export const PlayButton = styled.button`
  appearance: none;
  border: 1px solid rgba(0, 0, 0, 0.12);
  background: #4caf50;
  color: #ffffff;
  padding: 6px 10px;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.15s ease;

  &:hover:not(:disabled) {
    background: #45a049;
  }

  &:disabled {
    background: #cccccc;
    cursor: not-allowed;
    opacity: 0.6;
  }
`;

export const StopButton = styled.button`
  appearance: none;
  border: 1px solid rgba(0, 0, 0, 0.12);
  background: #f44336;
  color: #ffffff;
  padding: 6px 10px;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.15s ease;

  &:hover {
    background: #d32f2f;
  }

  &:active {
    background: #b71c1c;
  }
`;

export const IframePanel = styled.div`
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 12px;
  background: #ffffff;
  overflow: auto;
  align-items: flex-start;
  justify-content: center;
  height: 100%;
`;

type IframeWrapperProps = {
  $height?: string;
  $useMinHeight?: boolean;
};

export const IframeWrapper = styled.div<IframeWrapperProps>`
  position: relative;
  width: 320px;
  height: ${(props) => props.$height || '100%'};
  ${(props) => props.$useMinHeight && `height: 680px;`};
  flex-shrink: 0;

  &:hover button[data-delete-button] {
    opacity: 1;
  }
`;

export const PreviewIframe = styled.iframe`
  width: 100%;
  height: 100%;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  background: #fff;
`;

export const IframeLabel = styled.div`
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 1;
  padding: 4px 8px;
  background: rgba(0, 0, 0, 0.7);
  color: #ffffff;
  font-size: 11px;
  font-weight: 500;
  border-radius: 4px;
  pointer-events: none;
  font-family:
    system-ui,
    -apple-system,
    sans-serif;
  max-width: calc(100% - 16px);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
`;

export const DeleteButton = styled.button`
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 2;
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.7);
  color: #ffffff;
  font-size: 18px;
  font-weight: 600;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  opacity: 0;
  transition:
    opacity 0.15s ease,
    background 0.15s ease;

  &:hover {
    background: rgba(220, 38, 38, 0.9);
  }

  &:active {
    background: rgba(185, 28, 28, 0.9);
  }
`;

export const AddIframeButton = styled.button`
  width: 100%;
  height: 100%;
  border: 2px dashed rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  background: #f8f8f8;
  color: #666;
  font-size: 48px;
  font-weight: 300;
  cursor: pointer;
  transition: all 0.15s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;

  &:hover {
    background: #e8e8e8;
    border-color: rgba(0, 0, 0, 0.3);
    color: #333;
  }

  &:active {
    background: #d8d8d8;
  }
`;

export const GameSelectionContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px;
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 8px;
  min-width: 150px;
`;

export const GameSelectionLabel = styled.div`
  font-size: 12px;
  font-weight: 500;
  color: #666;
  margin-bottom: 4px;
`;

export const GameSelectionButtons = styled.div`
  display: flex;
  flex-direction: column;
  gap: 4px;
`;

type GameSelectionButtonProps = {
  $selected: boolean;
};

export const GameSelectionButton = styled.button<GameSelectionButtonProps>`
  appearance: none;
  border: 1px solid ${(props) => (props.$selected ? '#4caf50' : 'rgba(0, 0, 0, 0.12)')};
  background: ${(props) => (props.$selected ? '#4caf50' : '#ffffff')};
  color: ${(props) => (props.$selected ? '#ffffff' : '#222')};
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s ease;
  text-align: left;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  width: 100%;
  min-width: 120px;
  box-sizing: border-box;

  &:hover {
    background: ${(props) => (props.$selected ? '#45a049' : '#f6f6f6')};
    border-color: ${(props) => (props.$selected ? '#45a049' : 'rgba(0, 0, 0, 0.2)')};
  }

  &:active {
    background: ${(props) => (props.$selected ? '#3d8b40' : '#e8e8e8')};
  }
`;

type GameOrderBadgeProps = {
  $visible: boolean;
};

export const GameOrderBadge = styled.span<GameOrderBadgeProps>`
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  width: 20px;
  height: 20px;
  background: ${(props) => (props.$visible ? 'rgba(255, 255, 255, 0.3)' : 'transparent')};
  color: ${(props) => (props.$visible ? '#ffffff' : 'transparent')};
  font-size: 11px;
  font-weight: 600;
  border-radius: 50%;
  padding: 0;
  margin-left: auto;
  flex-shrink: 0;
`;

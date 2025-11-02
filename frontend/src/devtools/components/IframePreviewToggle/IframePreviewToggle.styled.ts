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
  justify-content: flex-end;
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
  margin-right: 8px;

  &:hover:not(:disabled) {
    background: #45a049;
  }

  &:disabled {
    background: #cccccc;
    cursor: not-allowed;
    opacity: 0.6;
  }
`;

export const IframePanel = styled.div`
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  gap: 12px;
  padding: 0;
  background: #ffffff;
  overflow: auto;
  align-items: flex-start;
  justify-content: center;
  height: 100%;
`;

export const IframeWrapper = styled.div`
  position: relative;
  width: 320px;
  height: 100%;
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

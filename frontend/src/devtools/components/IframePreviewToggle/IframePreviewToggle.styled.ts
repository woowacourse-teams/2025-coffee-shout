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
`;

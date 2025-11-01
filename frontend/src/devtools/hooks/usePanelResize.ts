import { useState, useRef, useCallback, useEffect, MouseEvent } from 'react';

/**
 * 패널의 수직 리사이즈를 관리하는 커스텀 훅입니다.
 *
 * @param initialHeight - 초기 패널 높이 (px)
 * @param minHeight - 최소 패널 높이 (px)
 * @returns 패널 높이, 리사이즈 시작 핸들러, 리사이즈 상태
 */
export const usePanelResize = (initialHeight: number, minHeight: number = 400) => {
  const [panelHeight, setPanelHeight] = useState(initialHeight);
  const [isResizing, setIsResizing] = useState(false);
  const resizeStartYRef = useRef<number | null>(null);
  const resizeStartHeightRef = useRef<number | null>(null);

  /**
   * 리사이즈 시작 핸들러입니다.
   */
  const handleResizeStart = useCallback(
    (e: MouseEvent) => {
      e.preventDefault();
      setIsResizing(true);
      resizeStartYRef.current = e.clientY;
      resizeStartHeightRef.current = panelHeight;
    },
    [panelHeight]
  );

  /**
   * 리사이즈 중 핸들러입니다.
   */
  const handleResizeMove = useCallback(
    (e: globalThis.MouseEvent) => {
      if (
        !isResizing ||
        resizeStartYRef.current === null ||
        resizeStartHeightRef.current === null
      ) {
        return;
      }

      const deltaY = resizeStartYRef.current - e.clientY;
      const newHeight = resizeStartHeightRef.current + deltaY;
      const maxHeight = window.innerHeight;

      if (newHeight >= minHeight && newHeight <= maxHeight) {
        setPanelHeight(newHeight);
      } else if (newHeight < minHeight) {
        setPanelHeight(minHeight);
      } else if (newHeight > maxHeight) {
        setPanelHeight(maxHeight);
      }
    },
    [isResizing, minHeight]
  );

  /**
   * 리사이즈 종료 핸들러입니다.
   */
  const handleResizeEnd = useCallback(() => {
    setIsResizing(false);
    resizeStartYRef.current = null;
    resizeStartHeightRef.current = null;
  }, []);

  useEffect(() => {
    if (isResizing) {
      document.addEventListener('mousemove', handleResizeMove);
      document.addEventListener('mouseup', handleResizeEnd);
      document.body.style.userSelect = 'none';
      document.body.style.cursor = 'ns-resize';

      return () => {
        document.removeEventListener('mousemove', handleResizeMove);
        document.removeEventListener('mouseup', handleResizeEnd);
        document.body.style.userSelect = '';
        document.body.style.cursor = '';
      };
    }
  }, [isResizing, handleResizeMove, handleResizeEnd]);

  return {
    panelHeight,
    handleResizeStart,
    isResizing,
  };
};

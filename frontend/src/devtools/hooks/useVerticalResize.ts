import React, { useState, useRef, useCallback, useEffect } from 'react';

/**
 * 패널의 수평(세로) 리사이즈를 관리하는 커스텀 훅입니다.
 */
export const useVerticalResize = (
  initialWidthPercent: number = 50,
  minWidthPercent: number = 20,
  maxWidthPercent: number = 80
) => {
  const [detailWidthPercent, setDetailWidthPercent] = useState(initialWidthPercent);
  const [isResizingVertical, setIsResizingVertical] = useState(false);
  const resizeStartXRef = useRef<number | null>(null);
  const resizeStartWidthPercentRef = useRef<number | null>(null);
  const contentRef = useRef<HTMLDivElement | null>(null);

  /**
   * 리사이즈 시작 핸들러 (React용)
   */
  const handleVerticalResizeStart = useCallback(
    (e: React.MouseEvent<HTMLDivElement>) => {
      e.preventDefault();
      e.stopPropagation();
      setIsResizingVertical(true);
      resizeStartXRef.current = e.clientX;
      resizeStartWidthPercentRef.current = detailWidthPercent;
    },
    [detailWidthPercent]
  );

  /**
   * 리사이즈 중 (브라우저 DOM 이벤트용)
   */
  const handleVerticalResizeMove = useCallback(
    (e: globalThis.MouseEvent) => {
      if (
        !isResizingVertical ||
        resizeStartXRef.current === null ||
        resizeStartWidthPercentRef.current === null ||
        !contentRef.current
      )
        return;

      const contentWidth = contentRef.current.offsetWidth;
      const deltaX = e.clientX - resizeStartXRef.current;
      const deltaPercent = (deltaX / contentWidth) * 100;
      const newWidthPercent = resizeStartWidthPercentRef.current - deltaPercent;
      const minWidth = Math.max((300 / contentWidth) * 100, minWidthPercent);

      if (newWidthPercent >= minWidth && newWidthPercent <= maxWidthPercent) {
        setDetailWidthPercent(newWidthPercent);
      } else if (newWidthPercent < minWidth) {
        setDetailWidthPercent(minWidth);
      } else {
        setDetailWidthPercent(maxWidthPercent);
      }
    },
    [isResizingVertical, minWidthPercent, maxWidthPercent]
  );

  /**
   * 리사이즈 종료 핸들러 (브라우저 DOM 이벤트용)
   */
  const handleVerticalResizeEnd = useCallback(() => {
    setIsResizingVertical(false);
    resizeStartXRef.current = null;
    resizeStartWidthPercentRef.current = null;
  }, []);

  /**
   * mousemove, mouseup 리스너 등록
   */
  useEffect(() => {
    if (!isResizingVertical) return;

    const onMove = (e: globalThis.MouseEvent) => handleVerticalResizeMove(e);
    const onUp = () => handleVerticalResizeEnd();

    document.addEventListener('mousemove', onMove);
    document.addEventListener('mouseup', onUp);
    document.body.style.userSelect = 'none';
    document.body.style.cursor = 'ew-resize';

    return () => {
      document.removeEventListener('mousemove', onMove);
      document.removeEventListener('mouseup', onUp);
      document.body.style.userSelect = '';
      document.body.style.cursor = '';
    };
  }, [isResizingVertical, handleVerticalResizeMove, handleVerticalResizeEnd]);

  return {
    detailWidthPercent,
    handleVerticalResizeStart,
    isResizingVertical,
    contentRef,
  };
};

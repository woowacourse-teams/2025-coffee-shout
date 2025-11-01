import { useState, useRef, useCallback, useEffect } from 'react';

/**
 * 패널의 수평(세로) 리사이즈를 관리하는 커스텀 훅입니다.
 *
 * @param initialWidthPercent - 초기 상세 패널 너비 비율 (%)
 * @param minWidthPercent - 최소 상세 패널 너비 비율 (%)
 * @param maxWidthPercent - 최대 상세 패널 너비 비율 (%)
 * @returns 상세 패널 너비 비율, 리사이즈 시작 핸들러, 리사이즈 상태, 컨텐츠 ref
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
   * 수평 리사이즈 시작 핸들러입니다.
   */
  const handleVerticalResizeStart = useCallback(
    (e: React.MouseEvent) => {
      e.preventDefault();
      e.stopPropagation();
      setIsResizingVertical(true);
      resizeStartXRef.current = e.clientX;
      resizeStartWidthPercentRef.current = detailWidthPercent;
    },
    [detailWidthPercent]
  );

  /**
   * 수평 리사이즈 중 핸들러입니다.
   */
  const handleVerticalResizeMove = useCallback(
    (e: MouseEvent) => {
      if (
        !isResizingVertical ||
        resizeStartXRef.current === null ||
        resizeStartWidthPercentRef.current === null ||
        !contentRef.current
      ) {
        return;
      }

      const contentWidth = contentRef.current.offsetWidth;
      const deltaX = e.clientX - resizeStartXRef.current;
      const deltaPercent = (deltaX / contentWidth) * 100;
      const newWidthPercent = resizeStartWidthPercentRef.current - deltaPercent;

      const minWidth = Math.max((300 / contentWidth) * 100, minWidthPercent);

      if (newWidthPercent >= minWidth && newWidthPercent <= maxWidthPercent) {
        setDetailWidthPercent(newWidthPercent);
      } else if (newWidthPercent < minWidth) {
        setDetailWidthPercent(minWidth);
      } else if (newWidthPercent > maxWidthPercent) {
        setDetailWidthPercent(maxWidthPercent);
      }
    },
    [isResizingVertical, minWidthPercent, maxWidthPercent]
  );

  /**
   * 수평 리사이즈 종료 핸들러입니다.
   */
  const handleVerticalResizeEnd = useCallback(() => {
    setIsResizingVertical(false);
    resizeStartXRef.current = null;
    resizeStartWidthPercentRef.current = null;
  }, []);

  useEffect(() => {
    if (isResizingVertical) {
      document.addEventListener('mousemove', handleVerticalResizeMove);
      document.addEventListener('mouseup', handleVerticalResizeEnd);
      document.body.style.userSelect = 'none';
      document.body.style.cursor = 'ew-resize';

      return () => {
        document.removeEventListener('mousemove', handleVerticalResizeMove);
        document.removeEventListener('mouseup', handleVerticalResizeEnd);
        document.body.style.userSelect = '';
        document.body.style.cursor = '';
      };
    }
  }, [isResizingVertical, handleVerticalResizeMove, handleVerticalResizeEnd]);

  return {
    detailWidthPercent,
    handleVerticalResizeStart,
    isResizingVertical,
    contentRef,
  };
};

import { useEffect, useMemo, useState } from 'react';
import { useLocation } from 'react-router-dom';
import * as S from './IframePreviewToggle.styled';

const IframePreviewToggle = () => {
  const location = useLocation();
  const [open, setOpen] = useState<boolean>(false);

  const isTopWindow = useMemo(() => {
    if (typeof window === 'undefined') return false;
    try {
      return window.self === window.top;
    } catch {
      return false;
    }
  }, []);

  const isRootPath = location.pathname === '/';

  useEffect(() => {
    // 경로가 바뀌면 닫아준다 (예상치 못한 잔상 방지)
    setOpen(false);
  }, [location.pathname]);

  if (!isTopWindow || !isRootPath) return null;

  return (
    <S.Container>
      <S.ToggleBar>
        <S.ToggleButton type="button" onClick={() => setOpen((v) => !v)}>
          {open ? 'Hide iframes' : 'Show iframes'}
        </S.ToggleButton>
      </S.ToggleBar>
      {open && (
        <S.IframePanel>
          <S.PreviewIframe title="preview-left" src="/" />
          <S.PreviewIframe title="preview-right" src="/" />
        </S.IframePanel>
      )}
    </S.Container>
  );
};

export default IframePreviewToggle;

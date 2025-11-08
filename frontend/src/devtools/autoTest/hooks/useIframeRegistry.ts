import { useCallback, useMemo, useRef, useState } from 'react';

type IframeRefMap = Record<string, HTMLIFrameElement | null>;

const DEFAULT_IFRAME_NAMES = ['host', 'guest1'] as const;
const MAX_IFRAME_COUNT = 9;
const MAX_GUEST_INDEX = 8;
const FULL_HEIGHT_THRESHOLD = 4;

export type UseIframeRegistryResult = {
  iframeNames: string[];
  iframeRefs: React.MutableRefObject<IframeRefMap>;
  iframeHeight: string;
  useMinHeight: boolean;
  canAddMore: boolean;
  handleAddIframe: () => void;
  handleDeleteIframe: (name: string) => void;
  setIframeRef: (name: string, iframe: HTMLIFrameElement | null) => void;
};

export const useIframeRegistry = (
  initialNames: string[] = [...DEFAULT_IFRAME_NAMES]
): UseIframeRegistryResult => {
  const [iframeNames, setIframeNames] = useState<string[]>(initialNames);
  const iframeRefs = useRef<IframeRefMap>({});

  const setIframeRef = useCallback((name: string, iframe: HTMLIFrameElement | null) => {
    if (iframe) {
      iframeRefs.current[name] = iframe;
    } else {
      delete iframeRefs.current[name];
    }
  }, []);

  const handleAddIframe = useCallback(() => {
    if (iframeNames.length >= MAX_IFRAME_COUNT) return;

    const guestNames = iframeNames.filter((name) => name.startsWith('guest'));
    const usedNumbers = new Set(
      guestNames
        .map((name) => {
          const match = name.match(/^guest(\d+)$/);
          return match ? parseInt(match[1], 10) : null;
        })
        .filter((value): value is number => value !== null)
    );

    let nextGuestNumber: number | null = null;
    for (let i = 1; i <= MAX_GUEST_INDEX; i++) {
      if (!usedNumbers.has(i)) {
        nextGuestNumber = i;
        break;
      }
    }

    if (nextGuestNumber === null) return;

    const newGuestName = `guest${nextGuestNumber}`;
    setIframeNames((prev) => [...prev, newGuestName]);
  }, [iframeNames]);

  const handleDeleteIframe = useCallback(
    (name: string) => {
      if (name === 'host' || name === 'guest1') return;

      const lastIndex = iframeNames.length - 1;
      const lastIframeName = iframeNames[lastIndex];
      if (name !== lastIframeName) return;

      setIframeNames((prev) => prev.filter((iframeName) => iframeName !== name));
      delete iframeRefs.current[name];
    },
    [iframeNames]
  );

  const iframeHeight = useMemo(() => {
    return iframeNames.length <= FULL_HEIGHT_THRESHOLD ? '100%' : 'auto';
  }, [iframeNames.length]);

  const useMinHeight = useMemo(() => {
    return iframeNames.length > FULL_HEIGHT_THRESHOLD;
  }, [iframeNames.length]);

  const canAddMore = useMemo(() => {
    return iframeNames.length < MAX_IFRAME_COUNT;
  }, [iframeNames.length]);

  return {
    iframeNames,
    iframeRefs,
    iframeHeight,
    useMinHeight,
    canAddMore,
    handleAddIframe,
    handleDeleteIframe,
    setIframeRef,
  };
};



import { useEffect, useState, useCallback, useRef } from 'react';
import { AutoTestLog, AutoTestLogger } from '../types/autoTest';
import { AutoTestLoggerInstance } from '../utils/autoTestLogger';
import { isTopWindow } from '@/devtools/common/utils/isTopWindow';

declare global {
  interface Window {
    __autoTestLogger__?: AutoTestLoggerInstance;
  }
}

// 모든 logger 수집 (메인 + iframe들)
const getAllLoggers = (): AutoTestLogger[] => {
  if (typeof window === 'undefined' || typeof document === 'undefined') return [];

  const loggers: AutoTestLogger[] = [];

  // 메인 윈도우의 logger
  try {
    if (isTopWindow() && window.__autoTestLogger__) {
      loggers.push(window.__autoTestLogger__);
    }
  } catch {
    // cross-origin 등의 경우 무시
  }

  // 모든 iframe의 logger 수집
  try {
    const iframes = document.querySelectorAll('iframe');
    iframes.forEach((iframe) => {
      try {
        const iframeWindow = iframe.contentWindow;
        if (iframeWindow && iframeWindow.__autoTestLogger__) {
          loggers.push(iframeWindow.__autoTestLogger__);
        }
      } catch {
        // cross-origin 등의 경우 무시
      }
    });
  } catch {
    // iframe 접근 불가능한 경우 무시
  }

  // window.frames도 시도 (fallback)
  try {
    const frames = window.frames;
    if (frames && frames.length) {
      for (let i = 0; i < frames.length; i++) {
        try {
          const frame = frames[i] as Window;
          if (frame && frame.__autoTestLogger__) {
            if (!loggers.includes(frame.__autoTestLogger__)) {
              loggers.push(frame.__autoTestLogger__);
            }
          }
        } catch {
          // cross-origin 등의 경우 무시
        }
      }
    }
  } catch {
    // frames 접근 불가능한 경우 무시
  }

  return loggers;
};

export const useAutoTestLogger = () => {
  const [logs, setLogs] = useState<AutoTestLog[]>([]);
  const loggersRef = useRef<AutoTestLogger[]>([]);
  const unsubscribeFunctionsRef = useRef<(() => void)[]>([]);
  const initialLogIdsRef = useRef<Set<string>>(new Set());

  // 메인 윈도우에서만 동작
  const topWindow = isTopWindow();

  // logger 구독 설정
  const setupLoggers = useCallback((loggers: AutoTestLogger[]) => {
    // 기존 구독 해제
    unsubscribeFunctionsRef.current.forEach((unsubscribe) => {
      try {
        unsubscribe();
      } catch {
        // 무시
      }
    });
    unsubscribeFunctionsRef.current = [];

    if (loggers.length === 0) return;

    // 모든 logger의 초기 로그 수집
    const allInitialLogs: AutoTestLog[] = [];
    loggers.forEach((logger) => {
      try {
        const loggerLogs = logger.getLogs();
        allInitialLogs.push(...loggerLogs);
      } catch {
        // 무시
      }
    });

    // 타임스탬프 순으로 정렬 (오래된 순, 최신이 아래로)
    allInitialLogs.sort((a, b) => a.timestamp - b.timestamp);
    setLogs(allInitialLogs);

    // 초기 로그의 ID 추적
    initialLogIdsRef.current = new Set(allInitialLogs.map((r) => r.id));

    // 모든 logger 구독
    loggers.forEach((logger) => {
      try {
        const unsubscribe = logger.subscribe((log) => {
          setLogs((prev) => {
            // 이미 있는 로그면 무시
            if (prev.some((r) => r.id === log.id)) {
              return prev;
            }
            // 초기 로그 목록에 있던 건 무시 (이미 추가됨)
            if (initialLogIdsRef.current.has(log.id)) {
              return prev;
            }
            // 새로운 로그를 뒤에 추가 (최신이 아래로)
            return [...prev, log];
          });
        });
        unsubscribeFunctionsRef.current.push(unsubscribe);
      } catch {
        // 무시
      }
    });

    loggersRef.current = loggers;
  }, []);

  useEffect(() => {
    if (!topWindow) return;

    // 초기 logger 수집 및 구독
    const initialLoggers = getAllLoggers();
    setupLoggers(initialLoggers);

    // 주기적으로 iframe 확인 (동적으로 추가되는 iframe 대응)
    const intervalId = setInterval(() => {
      const currentLoggers = getAllLoggers();
      const existingLoggers = loggersRef.current;

      // 새로운 logger가 있는지 확인 (객체 참조 비교)
      const hasNewLogger =
        currentLoggers.length !== existingLoggers.length ||
        currentLoggers.some((newLogger) => !existingLoggers.includes(newLogger));

      if (hasNewLogger) {
        setupLoggers(currentLoggers);
      }
    }, 1000); // 1초마다 확인

    return () => {
      unsubscribeFunctionsRef.current.forEach((unsubscribe) => {
        try {
          unsubscribe();
        } catch {
          // 무시
        }
      });
      clearInterval(intervalId);
    };
  }, [topWindow, setupLoggers]);

  const refreshLogs = useCallback(() => {
    if (!topWindow) return;

    const allLoggers = getAllLoggers();
    const allLogs: AutoTestLog[] = [];

    allLoggers.forEach((logger) => {
      try {
        const loggerLogs = logger.getLogs();
        allLogs.push(...loggerLogs);
      } catch {
        // 무시
      }
    });

    // 타임스탬프 순으로 정렬 (오래된 순, 최신이 아래로)
    allLogs.sort((a, b) => a.timestamp - b.timestamp);
    setLogs(allLogs);

    // 초기 로그 ID 업데이트
    initialLogIdsRef.current = new Set(allLogs.map((r) => r.id));

    // logger 재설정 (새로운 logger가 있을 수 있음)
    setupLoggers(allLoggers);
  }, [topWindow, setupLoggers]);

  return {
    logs,
    refreshLogs,
    loggers: loggersRef.current,
  };
};

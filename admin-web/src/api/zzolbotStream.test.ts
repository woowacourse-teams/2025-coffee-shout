import { afterEach, describe, expect, it, vi } from 'vitest';
import { askZzolBot } from '@/api/zzolbotStream';

/**
 * SSE 프레이밍을 직접 파싱하는 코드라 청크가 어떻게 쪼개져 오느냐에 따라 깨진다.
 * 실제로 네트워크는 이벤트 경계를 지켜서 끊어 주지 않는다.
 */
function streamOf(...chunks: string[]): Response {
  const encoder = new TextEncoder();
  return {
    ok: true,
    status: 200,
    body: new ReadableStream<Uint8Array>({
      start(controller) {
        for (const chunk of chunks) {
          controller.enqueue(encoder.encode(chunk));
        }
        controller.close();
      },
    }),
  } as unknown as Response;
}

function collect() {
  const progress: string[] = [];
  let sessionId: number | null = null;
  let answer: string | null = null;
  return {
    progress,
    get sessionId() {
      return sessionId;
    },
    get answer() {
      return answer;
    },
    handlers: {
      onProgress: (tool: string) => progress.push(tool),
      onSessionId: (id: number) => {
        sessionId = id;
      },
      onResult: (text: string) => {
        answer = text;
      },
    },
  };
}

afterEach(() => {
  vi.unstubAllGlobals();
});

describe('askZzolBot', () => {
  it('이벤트 이름별로 콜백을 나눠 부른다', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        streamOf(
          'event: progress\ndata: countRooms\n\n',
          'event: sessionId\ndata: 42\n\n',
          'event: result\ndata: 어제 방은 12개였습니다\n\n',
        ),
      ),
    );

    const sink = collect();
    await askZzolBot('어제 방 몇 개?', sink.handlers);

    expect(sink.progress).toEqual(['countRooms']);
    expect(sink.sessionId).toBe(42);
    expect(sink.answer).toBe('어제 방은 12개였습니다');
  });

  it('data 가 여러 줄이면 개행으로 이어 붙인다', async () => {
    // 한 줄로 가정하면 줄바꿈 있는 답변이 첫 줄만 나온다.
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(streamOf('event: result\ndata: 첫 줄\ndata: 둘째 줄\n\n')),
    );

    const sink = collect();
    await askZzolBot('q', sink.handlers);

    expect(sink.answer).toBe('첫 줄\n둘째 줄');
  });

  it('청크가 이벤트 한가운데서 끊겨도 이어 붙인다', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(streamOf('event: res', 'ult\ndata: 나눠', '져서 왔다\n\n')),
    );

    const sink = collect();
    await askZzolBot('q', sink.handlers);

    expect(sink.answer).toBe('나눠져서 왔다');
  });

  it('마지막 이벤트가 빈 줄로 끝나지 않아도 흘린다', async () => {
    // 서버가 complete() 하면서 종료 개행 없이 스트림을 닫는 경우가 있다.
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(streamOf('event: result\ndata: 끝맺음 없음')),
    );

    const sink = collect();
    await askZzolBot('q', sink.handlers);

    expect(sink.answer).toBe('끝맺음 없음');
  });

  it('data 뒤 공백은 하나만 걷어낸다', async () => {
    // 규격상 제거 대상은 공백 하나다. 두 개를 지우면 들여쓴 답변이 망가진다.
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(streamOf('event: result\ndata:   들여쓴 줄\n\n')),
    );

    const sink = collect();
    await askZzolBot('q', sink.handlers);

    expect(sink.answer).toBe('  들여쓴 줄');
  });

  it('CRLF 로 와도 이벤트 이름에 캐리지리턴이 붙지 않는다', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(streamOf('event: progress\r\ndata: countRooms\r\n\r\n')),
    );

    const sink = collect();
    await askZzolBot('q', sink.handlers);

    expect(sink.progress).toEqual(['countRooms']);
  });

  it('실패 응답이면 던진다', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({ ok: false, status: 503, body: null } as Response),
    );

    await expect(askZzolBot('q', collect().handlers)).rejects.toThrow('503');
  });
});

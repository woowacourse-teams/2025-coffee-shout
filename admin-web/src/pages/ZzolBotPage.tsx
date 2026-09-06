import { useState } from 'react';
import { PageHeader } from '@/components/ui/PageHeader';
import { Tabs } from '@/components/ui/Tabs';
import { ZzolBotChatPanel } from '@/pages/zzolbot/ZzolBotChatPanel';
import { ZzolBotEvalPanel } from '@/pages/zzolbot/ZzolBotEvalPanel';
import { ZzolBotMonitorPanel } from '@/pages/zzolbot/ZzolBotMonitorPanel';

type Panel = 'chat' | 'monitor' | 'eval';

const TABS: { value: Panel; label: string }[] = [
  { value: 'chat', label: '진단' },
  { value: 'monitor', label: '모니터링' },
  { value: 'eval', label: '평가' },
];

const DESCRIPTION: Record<Panel, string> = {
  chat: '운영 DB 를 읽어 답합니다. Grafana 를 열기 전에 먼저 물어보는 자리입니다.',
  monitor: '알림을 받아 스스로 분석한 기록입니다. 무엇이 튀었나가 아니라 그게 무슨 뜻인가가 남습니다.',
  eval: '프롬프트나 모델을 바꿨을 때 좋아졌는지를 숫자로 봅니다.',
};

/**
 * ZzolBot. 레거시 백오피스의 세 탭을 그대로 옮겼다.
 *
 * <p>탭을 화면 셋으로 쪼개지 않았다. 셋 다 같은 봇을 다른 각도에서 보는 것이고,
 * 실제로 진단하다가 "이 질문을 골든셋에 넣자"로 넘어가는 흐름이 잦다.
 *
 * <p>탭 상태를 URL 에 넣지 않는다. 이 화면을 링크로 주고받는 일이 없고, 넣으면
 * 라우팅 규칙이 한 겹 늘어난다. 링크가 필요해지면 그때 쿼리 파라미터로 올린다.
 */
export function ZzolBotPage() {
  const [panel, setPanel] = useState<Panel>('chat');

  return (
    <div className="mx-auto flex w-full max-w-[1500px] flex-col gap-6">
      <PageHeader
        title="ZzolBot"
        description={DESCRIPTION[panel]}
        actions={<Tabs tabs={TABS} value={panel} onChange={setPanel} />}
      />

      {panel === 'chat' && <ZzolBotChatPanel />}
      {panel === 'monitor' && <ZzolBotMonitorPanel />}
      {panel === 'eval' && <ZzolBotEvalPanel />}
    </div>
  );
}

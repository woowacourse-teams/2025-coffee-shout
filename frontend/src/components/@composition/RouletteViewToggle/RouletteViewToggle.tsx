import TextButton from '@/components/@common/TextButton/TextButton';
import { RouletteView } from '@/types/roulette';

type Props = {
  currentView: RouletteView;
  onViewChange: () => void;
};

const TOGGLE_TEXT_MAP = {
  roulette: '확률 보기',
  statistics: '룰렛 보기',
} as const;

const RouletteViewToggle = ({ currentView, onViewChange }: Props) => {
  const buttonText = TOGGLE_TEXT_MAP[currentView];
  const ariaLabel =
    currentView === 'roulette'
      ? '확률 보기 버튼. 클릭하면 각 참여자의 확률을 목록으로 볼 수 있습니다.'
      : '룰렛 보기 버튼. 클릭하면 룰렛 화면으로 전환됩니다.';

  return <TextButton text={buttonText} onClick={onViewChange} aria-label={ariaLabel} />;
};

export default RouletteViewToggle;

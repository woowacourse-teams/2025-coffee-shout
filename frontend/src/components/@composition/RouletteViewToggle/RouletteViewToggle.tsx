import { RouletteView } from '@/types/roulette';
import RouletteIcon from '@/assets/roulette-icon.svg';
import StatisticsIcon from '@/assets/statistics-icon.svg';
import IconButton from '@/components/@common/IconButton/IconButton';

type Props = {
  currentView: RouletteView;
  onViewChange: () => void;
};

const TOGGLE_ICONS = {
  roulette: StatisticsIcon,
  statistics: RouletteIcon,
} as const;

const RouletteViewToggle = ({ currentView, onViewChange }: Props) => {
  return <IconButton iconSrc={TOGGLE_ICONS[currentView]} onClick={onViewChange} />;
};

export default RouletteViewToggle;

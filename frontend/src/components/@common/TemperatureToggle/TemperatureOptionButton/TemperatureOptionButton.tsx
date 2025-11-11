import { TemperatureOption } from '@/types/menu';
import * as S from './TemperatureToggleButton.styled';

type Props = {
  option: TemperatureOption;
  position: 'left' | 'right';
  selected: boolean;
  onClick: () => void;
};

const TemperatureOptionButton = ({ option, position, selected, onClick }: Props) => {
  return (
    <S.Container
      $position={position}
      $selected={selected}
      $option={option}
      onClick={onClick}
      aria-label={option}
      data-testid={`temperature-option-${option}`}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          onClick();
        }
      }}
    >
      {option}
    </S.Container>
  );
};

export default TemperatureOptionButton;

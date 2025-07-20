import { ComponentProps } from 'react';
import * as S from './SwitchButton.styled';

const ICONS = {
  statistics: '/images/statistics-icon.svg' as const,
  roulette: '/images/roulette-icon.svg' as const,
};

type Props = {
  targetView: 'statistics' | 'roulette';
  onClick: () => void;
} & Omit<ComponentProps<'button'>, 'onClick'>;

const SwitchButton = ({ targetView, onClick, ...rest }: Props) => {
  const currentIcon = ICONS[targetView];
  const viewName = targetView === 'statistics' ? '통계' : '룰렛';

  return (
    <S.Container onClick={onClick} {...rest}>
      <S.Icon src={currentIcon} alt={`${viewName} 보기`} />
    </S.Container>
  );
};

export default SwitchButton;

import { ComponentProps } from 'react';
import * as S from './SwitchButton.styled';

const ICONS = {
  statistics: '/images/statistics-icon.svg' as const,
  roulette: '/images/roulette-icon.svg' as const,
  detail: '/images/detail-icon.svg' as const,
};

type Props = {
  currentView: 'statistics' | 'roulette' | 'detail';
  onClick: () => void;
} & Omit<ComponentProps<'button'>, 'onClick'>;

const SwitchButton = ({ currentView, onClick, ...rest }: Props) => {
  const currentIcon = ICONS[currentView];

  return (
    <S.Container onClick={onClick} {...rest}>
      <S.Icon src={currentIcon} alt={currentView} />
    </S.Container>
  );
};

export default SwitchButton;

import { ComponentProps } from 'react';
import * as S from './SwitchButton.styled';

type Props = {
  currentView: 'statistics' | 'roulette';
  onClick: () => void;
} & Omit<ComponentProps<'button'>, 'onClick'>;

const SwitchButton = ({ currentView, onClick, ...props }: Props) => {
  const currentIcon =
    currentView === 'statistics' ? '/images/statistics-icon.svg' : '/images/roulette-icon.svg';

  return (
    <S.Container onClick={onClick} {...props}>
      <S.Icon src={currentIcon} alt={currentView} />
    </S.Container>
  );
};

export default SwitchButton;

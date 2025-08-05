import Button from '@/components/@common/Button/Button';
import type { ComponentProps, MouseEvent, TouchEvent } from 'react';

type Props = {
  isReady: boolean;
  onClick?: (e: MouseEvent<HTMLButtonElement> | TouchEvent<HTMLButtonElement>) => void;
} & Omit<ComponentProps<typeof Button>, 'onClick'>;

const GameReadyButton = ({ onClick, isReady = false, ...rest }: Props) => {
  return (
    <Button variant={isReady ? 'ready' : 'primary'} onClick={onClick} {...rest}>
      {isReady ? '준비 완료!' : '준비하기'}
    </Button>
  );
};

export default GameReadyButton;

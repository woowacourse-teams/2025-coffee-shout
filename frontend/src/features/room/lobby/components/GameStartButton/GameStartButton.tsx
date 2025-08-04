import Button from '@/components/@common/Button/Button';
import type { ComponentProps, MouseEvent, TouchEvent } from 'react';

type Props = {
  onClick?: (e: MouseEvent<HTMLButtonElement> | TouchEvent<HTMLButtonElement>) => void;
  canStart?: boolean;
  participantCount?: number;
} & Omit<ComponentProps<typeof Button>, 'onClick'>;

const GameStartButton = ({ onClick, ...rest }: Props) => {
  return (
    <Button variant="primary" onClick={onClick} {...rest}>
      게임 시작
    </Button>
  );
};

export default GameStartButton;

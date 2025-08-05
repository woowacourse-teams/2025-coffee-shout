import Button from '@/components/@common/Button/Button';
import type { ComponentProps } from 'react';

const GameStartButton = ({ ...rest }: ComponentProps<typeof Button>) => {
  return (
    <Button variant="primary" {...rest}>
      게임 시작
    </Button>
  );
};

export default GameStartButton;

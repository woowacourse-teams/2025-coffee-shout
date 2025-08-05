import Button from '@/components/@common/Button/Button';
import type { ComponentProps } from 'react';

type Props = {
  isReady: boolean;
} & ComponentProps<typeof Button>;

const GameReadyButton = ({ isReady, ...rest }: Props) => {
  return (
    <Button variant={isReady ? 'ready' : 'primary'} {...rest}>
      {isReady ? '준비 완료!' : '준비하기'}
    </Button>
  );
};

export default GameReadyButton;

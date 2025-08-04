import Button from '@/components/@common/Button/Button';
import type { ComponentProps } from 'react';

type Props = {
  isReadyCount?: number;
  totalParticipantCount?: number;
} & Omit<ComponentProps<typeof Button>, 'onClick'>;

const HostWaitingButton = ({ isReadyCount = 0, totalParticipantCount = 0, ...rest }: Props) => {
  return (
    <Button variant="ready" {...rest}>
      게임 대기중... {isReadyCount}/{totalParticipantCount}
    </Button>
  );
};

export default HostWaitingButton;

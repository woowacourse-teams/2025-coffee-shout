import BackIcon from '@/assets/back-icon.svg';
import { ComponentProps } from 'react';

type Props = {
  onClick: () => void;
} & ComponentProps<'button'>;

const BackButton = ({ onClick, ...rest }: Props) => {
  return (
    <button onClick={onClick} {...rest}>
      <img src={BackIcon} alt="뒤로가기" />
    </button>
  );
};

export default BackButton;

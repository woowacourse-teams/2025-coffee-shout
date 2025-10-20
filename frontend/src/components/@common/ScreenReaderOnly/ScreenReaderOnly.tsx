import { ScreenReaderContainer } from './ScreenReaderOnly.styled';

type Props = {
  children: string;
  'aria-live'?: 'polite' | 'assertive' | 'off';
  'aria-atomic'?: boolean;
};

const ScreenReaderOnly = ({
  children,
  'aria-live': ariaLive = 'polite',
  'aria-atomic': ariaAtomic = true,
}: Props) => {
  return (
    <ScreenReaderContainer aria-live={ariaLive} aria-atomic={ariaAtomic}>
      {children}
    </ScreenReaderContainer>
  );
};

export default ScreenReaderOnly;

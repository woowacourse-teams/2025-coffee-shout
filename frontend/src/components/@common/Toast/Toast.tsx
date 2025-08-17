import Description from '../Description/Description';
import Portal from '../Portal/Portal';
import * as S from './Toast.styled';
import { ToastOptions } from './types';

const Toast = ({ message, type }: Omit<ToastOptions, 'duration'>) => {
  return (
    <Portal containerId="toast-root">
      <S.Container $type={type}>
        <Description>{message}</Description>
      </S.Container>
    </Portal>
  );
};

export default Toast;

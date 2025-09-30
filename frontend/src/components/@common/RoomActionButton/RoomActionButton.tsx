import NextStepIcon from '@/assets/next-step-icon.svg';
import { type ComponentProps, type MouseEvent, type TouchEvent } from 'react';
import Description from '../Description/Description';
import Headline3 from '../Headline3/Headline3';
import * as S from './RoomActionButton.styled';
import { checkIsTouchDevice } from '@/utils/checkIsTouchDevice';
import { useTouchInteraction } from '@/hooks/useTouchInteraction';

type Props = {
  title: string;
  descriptions: string[];
  onClick?: (e: MouseEvent<HTMLButtonElement> | TouchEvent<HTMLButtonElement>) => void;
} & Omit<ComponentProps<'button'>, 'onClick'>;

const RoomActionButton = ({ title, descriptions, onClick, ...rest }: Props) => {
  const { touchState, handleTouchStart, handleTouchEnd } = useTouchInteraction({
    onClick,
  });
  const isTouchDevice = checkIsTouchDevice();

  const handleClick = (e: MouseEvent<HTMLButtonElement>) => {
    if (isTouchDevice) return;

    onClick?.(e);
  };

  return (
    <S.Container
      onClick={handleClick}
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
      $touchState={touchState}
      {...rest}
    >
      <Headline3>{title}</Headline3>
      <div>
        {descriptions.map((description, index) => (
          <S.DescriptionBox key={index}>
            <Description color="gray-400">{description}</Description>
          </S.DescriptionBox>
        ))}
      </div>
      <S.NextStepIcon src={NextStepIcon} alt="next-step-icon" />
    </S.Container>
  );
};

export default RoomActionButton;

import NextStepIcon from '@/assets/next-step-icon.svg';
import type { ComponentProps, MouseEvent, TouchEvent } from 'react';
import Description from '../Description/Description';
import Headline3 from '../Headline3/Headline3';
import * as S from './RoomActionButton.styled';
import { isTouchDevice } from '@/utils/isTouchDevice';

type Props = {
  title: string;
  descriptions: string[];
  onClick?: (e: MouseEvent<HTMLButtonElement> | TouchEvent<HTMLButtonElement>) => void;
} & Omit<ComponentProps<'button'>, 'onClick'>;

const RoomActionButton = ({ title, descriptions, onClick, ...rest }: Props) => {
  const isTouch = isTouchDevice();

  const handleClick = (e: MouseEvent<HTMLButtonElement>) => {
    if (isTouch) return;

    onClick?.(e);
  };

  const handleTouchEnd = (e: TouchEvent<HTMLButtonElement>) => {
    if (!isTouch) return;

    e.preventDefault();

    onClick?.(e);
  };

  return (
    <S.Container onClick={handleClick} onTouchEnd={handleTouchEnd} {...rest}>
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

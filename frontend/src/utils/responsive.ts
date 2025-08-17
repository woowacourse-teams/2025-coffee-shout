const MIN_WIDTH = 320;
const MAX_WIDTH = 430;
const MIN_HEIGHT = 568;
const MAX_HEIGHT = 932;

export const clampByWidth = (min: number, max: number): string => {
  const vw = ((max - min) / (MAX_WIDTH - MIN_WIDTH)) * 100;
  const offset = min - (MIN_WIDTH * vw) / 100;
  return `clamp(${min}px, ${vw}vw + ${offset}px, ${max}px)`;
};

export const clampByHeight = (min: number, max: number): string => {
  const slope = (max - min) / (MAX_HEIGHT - MIN_HEIGHT);
  const vhValue = slope * 100;
  const offset = min - slope * MIN_HEIGHT;

  const offsetStr = offset >= 0 ? `+ ${offset}px` : `- ${Math.abs(offset)}px`;
  return `clamp(${min}px, ${vhValue}vh ${offsetStr}, ${max}px)`;
};

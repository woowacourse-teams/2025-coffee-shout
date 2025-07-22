import ProfileRedIcon from '@/assets/images/profile-red.svg';
// import ProfileBlueIcon from '@/assets/images/profile-blue.svg';
// import ProfileGreenIcon from '@/assets/images/profile-green.svg';
// import ProfileYellowIcon from '@/assets/images/profile-yellow.svg';

export type IconColor = 'red';

const ICON_MAP = {
  red: ProfileRedIcon,
  // blue: ProfileBlueIcon,
  // green: ProfileGreenIcon,
  // yellow: ProfileYellowIcon,
} as const;

export const getPlayerIcon = (color: IconColor): string => {
  return ICON_MAP[color] || ICON_MAP.red;
};

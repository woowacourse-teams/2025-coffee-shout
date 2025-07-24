import ProfileRedIcon from '@/assets/profile-red.svg';
import { IconColor } from '@/types/player';
// import ProfileBlueIcon from '@/assets/profile-blue.svg';
// import ProfileGreenIcon from '@/assets/profile-green.svg';
// import ProfileYellowIcon from '@/assets/profile-yellow.svg';

const ICON_MAP = {
  red: ProfileRedIcon,
  // blue: ProfileBlueIcon,
  // green: ProfileGreenIcon,
  // yellow: ProfileYellowIcon,
} as const;

export const getPlayerIcon = (color: IconColor): string => {
  return ICON_MAP[color] || ICON_MAP.red;
};

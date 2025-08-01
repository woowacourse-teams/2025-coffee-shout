import ProfileRedIcon from '@/assets/profile-red.svg';
import { ColorList } from '@/constants/color';

const ICON_MAP: Record<ColorList, string> = {
  // TODO: ProfileRedIcon 아이콘 svg 추가하여 매핑
  '#FF6B6B': ProfileRedIcon,
  '#80d6d0': ProfileRedIcon,
  '#45B7D1': ProfileRedIcon,
  '#96CEB4': ProfileRedIcon,
  '#FFEAA7': ProfileRedIcon,
  '#DDA0DD': ProfileRedIcon,
  '#98D8C8': ProfileRedIcon,
  '#F7DC6F': ProfileRedIcon,
  '#BB8FCE': ProfileRedIcon,
};

export const getPlayerIcon = (color: ColorList): string => {
  return ICON_MAP[color] || ProfileRedIcon;
};

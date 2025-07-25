import { SVGProps } from 'react';

interface Props extends SVGProps<SVGSVGElement> {
  stroke?: string;
  strokeWidth?: number;
}

const CloseIcon = ({ stroke = '#99A1AF', strokeWidth = 2, ...rest }: Props) => {
  return (
    <svg
      width="19"
      height="19"
      viewBox="0 0 19 19"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      {...rest}
    >
      <path
        d="M14.8438 4.15625L4.15625 14.8438"
        stroke={stroke}
        strokeWidth={strokeWidth}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M14.8438 14.8438L4.15625 4.15625"
        stroke={stroke}
        strokeWidth={strokeWidth}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
};

export default CloseIcon;

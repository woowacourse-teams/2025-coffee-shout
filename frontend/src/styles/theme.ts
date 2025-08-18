const color = {
  point: {
    50: '#FEF2F2',
    100: '#FFE1E1',
    200: '#FFC8C9',
    300: '#FF8789',
    400: '#FD6C6E',
    500: '#F53E41',
  },

  gray: {
    50: '#F9FAFB',
    100: '#F3F4F6',
    200: '#E5E7EB',
    300: '#D1D5DC',
    400: '#99A1AF',
    500: '#6A7282',
    600: '#4A5565',
    700: '#364153',
    800: '#1E2939',
    900: '#101828',
    950: '#030712',
  },

  white: '#FFFFFF',
  black: '#000000',
} as const;

const typography = {
  h1: {
    fontSize: '30px',
    fontWeight: 700,
    fontFamily:
      "'Pretendard Variable', Pretendard, -apple-system, BlinkMacSystemFont, system-ui, sans-serif",
  },
  h2: {
    fontSize: '24px',
    fontWeight: 600,
    fontFamily:
      "'Pretendard Variable', Pretendard, -apple-system, BlinkMacSystemFont, system-ui, sans-serif",
  },
  h3: {
    fontSize: '20px',
    fontWeight: 600,
    fontFamily:
      "'Pretendard Variable', Pretendard, -apple-system, BlinkMacSystemFont, system-ui, sans-serif",
  },
  h4: {
    fontSize: '16px',
    fontWeight: 600,
    fontFamily:
      "'Pretendard Variable', Pretendard, -apple-system, BlinkMacSystemFont, system-ui, sans-serif",
  },

  paragraph: {
    fontSize: '16px',
    fontWeight: 500,
    fontFamily:
      "'Pretendard Variable', Pretendard, -apple-system, BlinkMacSystemFont, system-ui, sans-serif",
  },

  small: {
    fontSize: '14px',
    fontWeight: 400,
    fontFamily:
      "'Pretendard Variable', Pretendard, -apple-system, BlinkMacSystemFont, system-ui, sans-serif",
  },
} as const;

export const theme = {
  color,
  typography,
} as const;

export const getWebSocketUrl = (): string => {
  const API_URL = process.env.REACT_APP_API_URL || 'https://api.dev.coffee-shout.com';

  if (!API_URL) {
    throw new Error('REACT_APP_API_URL가 정의되지 않았습니다');
  }

  return API_URL + '/ws';
};

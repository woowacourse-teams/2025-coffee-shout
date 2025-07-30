export const getWebSocketUrl = (): string => {
  const apiUrl = process.env.REACT_APP_API_URL;
  if (!apiUrl) {
    throw new Error('REACT_APP_API_URL가 정의되지 않았습니다');
  }

  return apiUrl + '/ws';
};

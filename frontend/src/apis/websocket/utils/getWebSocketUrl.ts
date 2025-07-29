export const getWebSocketUrl = (): string => {
  const apiUrl = process.env.REACT_APP_API_URL;
  if (!apiUrl) {
    throw new Error('REACT_APP_API_URL is not defined');
  }

  return apiUrl + '/ws';
};

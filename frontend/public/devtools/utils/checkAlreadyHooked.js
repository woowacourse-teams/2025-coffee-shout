/* eslint-env browser */

export const checkAlreadyHooked = (win, key, marker) => {
  if (!win?.[key]) {
    return false;
  }
  return !!win[marker];
};

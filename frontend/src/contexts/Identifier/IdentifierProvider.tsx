import { PropsWithChildren, useCallback, useEffect, useState } from 'react';
import { IdentifierContext } from './IdentifierContext';

const STORAGE_KEYS = {
  JOIN_CODE: 'coffee-shout-join-code',
  MY_NAME: 'coffee-shout-my-name',
  QR_CODE_URL: 'coffee-shout-qr-code-url',
} as const;

export const IdentifierProvider = ({ children }: PropsWithChildren) => {
  const [joinCode, setJoinCode] = useState<string>(() => {
    return sessionStorage.getItem(STORAGE_KEYS.JOIN_CODE) || '';
  });
  const [myName, setMyName] = useState<string>(() => {
    return sessionStorage.getItem(STORAGE_KEYS.MY_NAME) || '';
  });
  const [qrCodeUrl, setQrCodeUrl] = useState<string>(() => {
    return sessionStorage.getItem(STORAGE_KEYS.QR_CODE_URL) || '';
  });

  useEffect(() => {
    if (joinCode) {
      sessionStorage.setItem(STORAGE_KEYS.JOIN_CODE, joinCode);
    } else {
      sessionStorage.removeItem(STORAGE_KEYS.JOIN_CODE);
    }
  }, [joinCode]);

  useEffect(() => {
    if (myName) {
      sessionStorage.setItem(STORAGE_KEYS.MY_NAME, myName);
    } else {
      sessionStorage.removeItem(STORAGE_KEYS.MY_NAME);
    }
  }, [myName]);

  useEffect(() => {
    if (qrCodeUrl) {
      sessionStorage.setItem(STORAGE_KEYS.QR_CODE_URL, qrCodeUrl);
    } else {
      sessionStorage.removeItem(STORAGE_KEYS.QR_CODE_URL);
    }
  }, [qrCodeUrl]);

  const clearJoinCode = useCallback(() => {
    setJoinCode('');
  }, []);

  const clearMyName = useCallback(() => {
    setMyName('');
  }, []);

  const clearQrCodeUrl = useCallback(() => {
    setQrCodeUrl('');
  }, []);

  const clearIdentifier = useCallback(() => {
    clearJoinCode();
    clearMyName();
    clearQrCodeUrl();
  }, [clearJoinCode, clearMyName, clearQrCodeUrl]);

  return (
    <IdentifierContext.Provider
      value={{
        joinCode,
        setJoinCode,
        clearJoinCode,
        myName,
        setMyName,
        clearMyName,
        qrCodeUrl,
        setQrCodeUrl,
        clearQrCodeUrl,
        clearIdentifier,
      }}
    >
      {children}
    </IdentifierContext.Provider>
  );
};

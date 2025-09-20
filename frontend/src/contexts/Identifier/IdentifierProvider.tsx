import { PropsWithChildren, useCallback, useEffect, useState } from 'react';
import { storageManager, STORAGE_KEYS, STORAGE_TYPES } from '@/utils/StorageManager';
import { IdentifierContext } from './IdentifierContext';

export const IdentifierProvider = ({ children }: PropsWithChildren) => {
  const [joinCode, setJoinCode] = useState<string>(() => {
    return storageManager.getItem(STORAGE_KEYS.JOIN_CODE, STORAGE_TYPES.SESSION) || '';
  });
  const [myName, setMyName] = useState<string>(() => {
    return storageManager.getItem(STORAGE_KEYS.MY_NAME, STORAGE_TYPES.SESSION) || '';
  });
  const [qrCodeUrl, setQrCodeUrl] = useState<string>(() => {
    return storageManager.getItem(STORAGE_KEYS.QR_CODE_URL, STORAGE_TYPES.SESSION) || '';
  });

  useEffect(() => {
    if (joinCode) {
      storageManager.setItem(STORAGE_KEYS.JOIN_CODE, joinCode, STORAGE_TYPES.SESSION);
    } else {
      storageManager.removeItem(STORAGE_KEYS.JOIN_CODE, STORAGE_TYPES.SESSION);
    }
  }, [joinCode]);

  useEffect(() => {
    if (myName) {
      storageManager.setItem(STORAGE_KEYS.MY_NAME, myName, STORAGE_TYPES.SESSION);
    } else {
      storageManager.removeItem(STORAGE_KEYS.MY_NAME, STORAGE_TYPES.SESSION);
    }
  }, [myName]);

  useEffect(() => {
    if (qrCodeUrl) {
      storageManager.setItem(STORAGE_KEYS.QR_CODE_URL, qrCodeUrl, STORAGE_TYPES.SESSION);
    } else {
      storageManager.removeItem(STORAGE_KEYS.QR_CODE_URL, STORAGE_TYPES.SESSION);
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

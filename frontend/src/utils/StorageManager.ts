export const STORAGE_KEYS = {
  COFFEE_SHOUT_VISITED: 'coffee-shout-visited',
  COFFEE_SHOUT_FIRST_TIME_USER: 'coffee-shout-first-time-user',
  JOIN_CODE: 'coffee-shout-join-code',
  MY_NAME: 'coffee-shout-my-name',
  QR_CODE_URL: 'coffee-shout-qr-code-url',
  PLAYER_TYPE: 'coffee-shout-player-type',
} as const;

export const STORAGE_TYPES = {
  LOCAL: 'localStorage',
  SESSION: 'sessionStorage',
} as const;

type StorageType = (typeof STORAGE_TYPES)[keyof typeof STORAGE_TYPES];

export class StorageManager {
  private static instance: StorageManager;

  public static getInstance(): StorageManager {
    if (!StorageManager.instance) {
      StorageManager.instance = new StorageManager();
    }
    return StorageManager.instance;
  }

  private constructor() {}

  private getStorage(type: StorageType): Storage | null {
    try {
      if (typeof window === 'undefined') {
        console.warn('스토리지에 접근할 수 없음');
        return null;
      }
      return window[type];
    } catch (error) {
      console.error(`${type} 스토리지에 접근할 수 없음 :`, error);
      return null;
    }
  }

  public setItem(key: string, value: string, type: StorageType = STORAGE_TYPES.LOCAL): boolean {
    try {
      const storage = this.getStorage(type);
      if (!storage) return false;

      storage.setItem(key, value);
      return true;
    } catch (error) {
      console.error(`값 넣기 실패 ${type}:`, error);
      return false;
    }
  }

  public getItem(key: string, type: StorageType = STORAGE_TYPES.LOCAL): string | null {
    try {
      const storage = this.getStorage(type);
      if (!storage) return null;

      return storage.getItem(key);
    } catch (error) {
      console.error(`값 가져오기 실패 ${type}:`, error);
      return null;
    }
  }

  public removeItem(key: string, type: StorageType = STORAGE_TYPES.LOCAL): boolean {
    try {
      const storage = this.getStorage(type);
      if (!storage) return false;

      storage.removeItem(key);
      return true;
    } catch (error) {
      console.error(`삭제 실패 ${type}:`, error);
      return false;
    }
  }

  public hasItem(key: string, type: StorageType = STORAGE_TYPES.LOCAL): boolean {
    return this.getItem(key, type) !== null;
  }

  public clear(type: StorageType = STORAGE_TYPES.LOCAL): boolean {
    try {
      const storage = this.getStorage(type);
      if (!storage) return false;

      storage.clear();
      return true;
    } catch (error) {
      console.error(`초기화 실패 ${type}:`, error);
      return false;
    }
  }

  public setObject<T>(key: string, value: T, type: StorageType = STORAGE_TYPES.LOCAL): boolean {
    try {
      const jsonString = JSON.stringify(value);
      return this.setItem(key, jsonString, type);
    } catch (error) {
      console.error('json 변환 실패 :', error);
      return false;
    }
  }

  public getObject<T>(key: string, type: StorageType = STORAGE_TYPES.LOCAL): T | null {
    try {
      const jsonString = this.getItem(key, type);
      if (!jsonString) return null;

      return JSON.parse(jsonString) as T;
    } catch (error) {
      console.error('json 파싱 실패 :', error);
      return null;
    }
  }
}

export const storageManager = StorageManager.getInstance();

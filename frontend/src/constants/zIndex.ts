export const Z_INDEX = {
  MODAL: 1000,
} as const;

export type ZIndexLevel = (typeof Z_INDEX)[keyof typeof Z_INDEX];

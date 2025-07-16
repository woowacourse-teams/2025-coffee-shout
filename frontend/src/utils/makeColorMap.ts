export const makeColorMap = (prefix: string, obj: Record<string, string>) =>
  Object.fromEntries(Object.entries(obj).map(([k, v]) => [`${prefix}-${k}`, v]));

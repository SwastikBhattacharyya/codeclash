export type ApiError = {
  code: string;
  title: string;
  message: string;
  timestamp: string;
  properties?: Record<string, unknown>;
};

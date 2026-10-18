// Portfolio demo build (ng build --configuration demo): no backend, the API is answered in the browser
// by src/app/demo/demo-backend.ts. apiBaseUrl is only a prefix the interceptor recognises; nothing is requested from it.
export const environment = {
  production: true,
  demo: true,
  apiBaseUrl: 'https://amira.demo',
  lowStockThreshold: 5,
  expiryWarningDays: 90
};

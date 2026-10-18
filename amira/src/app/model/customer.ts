export interface Customer {
  id: number;
  name: string;
  email: string;
  phone?: string;
  shippingAddress?: string;
  role: 'CUSTOMER' | 'OWNER';
  /** sent to the server only, never returned */
  password?: string;
}

export interface LoginResponse {
  token: string;
  customer: Customer;
}

export type PaymentMethod = 'GCash' | 'Card' | 'Bank Transfer' | 'COD';

export const PAYMENT_METHODS: { value: PaymentMethod; label: string; note: string }[] = [
  { value: 'GCash', label: 'GCash', note: 'Paid instantly (demo gateway)' },
  { value: 'Card', label: 'Credit / debit card', note: 'Paid instantly (demo gateway)' },
  { value: 'Bank Transfer', label: 'Bank transfer', note: 'Confirmed by the shop after your transfer' },
  { value: 'COD', label: 'Cash on delivery', note: 'Pay the rider; confirmed by the shop' }
];

export interface Payment {
  id: number;
  orderId: number;
  method: PaymentMethod;
  amount: number;
  paymentDate: string;
  status: 'Pending' | 'Completed' | 'Failed' | 'Refunded';
}

import { OrderItem } from './order-item';

export type OrderStatus = 'Pending' | 'Paid' | 'Shipped' | 'Delivered' | 'Cancelled';
export type PaymentStatus = 'Unpaid' | 'Paid' | 'Refunded';

export interface Order {
  id: number;
  description?: string;
  customerId: number;
  customerName: string;
  shippingAddress: string;
  dateOrdered: string;
  totalAmount: number;
  statusName: OrderStatus;
  paymentStatus: PaymentStatus;
  items: OrderItem[];
}

export interface OrderItem {
  id?: number;
  orderId?: number;
  variantId: number;
  productName?: string;
  size?: string;
  quantity: number;
  priceAtPurchase?: number;
  subtotal?: number;
}

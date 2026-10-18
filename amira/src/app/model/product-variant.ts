export type VariantStatus = 'Active' | 'LowStock' | 'Expired' | 'Discontinued';

export interface ProductVariant {
  id: number;
  productId: number;
  productName?: string;
  size: string;
  sku?: string;
  price: number;
  /** only sent to the owner */
  costPrice?: number | null;
  stockQuantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  batchNumber?: string;
  manufactureDate?: string | null;
  expiryDate?: string | null;
  status: VariantStatus;
}

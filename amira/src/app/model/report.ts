export interface StockReportLine {
  variantId: number;
  productName: string;
  brand: string;
  category: string;
  size: string;
  sku: string;
  batchNumber?: string;
  expiryDate?: string | null;
  stockQuantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  status: string;
  costValue: number;
}

export interface BestSeller {
  variantId: number;
  productName: string;
  size: string;
  unitsSold: number;
  revenue: number;
}

export interface CategoryRevenue {
  category: string;
  revenue: number;
}

export interface InventoryValuation {
  totalUnits: number;
  costValue: number;
  retailValue: number;
}

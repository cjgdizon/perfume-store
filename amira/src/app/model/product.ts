import { ProductVariant } from './product-variant';

export interface Product {
  id: number;
  name: string;
  brand: string;
  description: string;
  category: string;
  imageUrl?: string | null;
  dateAdded?: string;
  variants: ProductVariant[];
}

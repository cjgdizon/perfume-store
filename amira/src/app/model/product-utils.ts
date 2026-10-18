import { Product } from './product';
import { ProductVariant } from './product-variant';

/** A variant can be bought when it is not expired or discontinued and has stock left. */
export function isSellable(v: ProductVariant): boolean {
  return (v.status === 'Active' || v.status === 'LowStock') && v.availableQuantity > 0;
}

export function sellableVariants(p: Product): ProductVariant[] {
  return p.variants.filter(isSellable);
}

/** Lowest price among the variants that can be bought, or null when none can. */
export function fromPrice(p: Product): number | null {
  const prices = sellableVariants(p).map(v => v.price);
  return prices.length ? Math.min(...prices) : null;
}

export const CATEGORIES = ['Men', 'Women', 'Unisex'];

/** "2027-03-01" -> "Mar 2027" */
export function monthYear(date?: string | null): string {
  if (!date) {
    return 'not stated';
  }
  return new Date(date + 'T00:00:00').toLocaleDateString('en-PH', { month: 'short', year: 'numeric' });
}

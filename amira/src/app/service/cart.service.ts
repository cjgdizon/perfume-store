import { Injectable } from '@angular/core';
import { BehaviorSubject, map } from 'rxjs';
import { Product } from '../model/product';
import { ProductVariant } from '../model/product-variant';

export interface CartLine {
  variantId: number;
  productId: number;
  productName: string;
  brand: string;
  category: string;
  imageUrl?: string | null;
  size: string;
  price: number;
  quantity: number;
  /** stock available when the line was added; the server re-checks at checkout */
  maxQuantity: number;
}

const STORAGE_KEY = 'amira.cart';

/** The cart lives in the browser only; the server sees it when the order is placed. */
@Injectable({ providedIn: 'root' })
export class CartService {
  private lines = new BehaviorSubject<CartLine[]>(this.restore());
  readonly lines$ = this.lines.asObservable();
  readonly count$ = this.lines$.pipe(map(l => l.reduce((n, line) => n + line.quantity, 0)));
  readonly total$ = this.lines$.pipe(map(l => l.reduce((sum, line) => sum + line.price * line.quantity, 0)));

  get snapshot(): CartLine[] {
    return this.lines.value;
  }

  public add(product: Product, variant: ProductVariant, quantity: number): void {
    const lines = this.snapshot.map(l => ({ ...l }));
    const existing = lines.find(l => l.variantId === variant.id);
    if (existing) {
      existing.quantity = Math.min(existing.quantity + quantity, variant.availableQuantity);
      existing.maxQuantity = variant.availableQuantity;
      existing.price = variant.price;
    } else {
      lines.push({
        variantId: variant.id,
        productId: product.id,
        productName: product.name,
        brand: product.brand,
        category: product.category,
        imageUrl: product.imageUrl,
        size: variant.size,
        price: variant.price,
        quantity: Math.min(quantity, variant.availableQuantity),
        maxQuantity: variant.availableQuantity
      });
    }
    this.save(lines);
  }

  public setQuantity(variantId: number, quantity: number): void {
    const lines = this.snapshot
      .map(l => (l.variantId === variantId ? { ...l, quantity: Math.max(1, Math.min(quantity, l.maxQuantity)) } : l));
    this.save(lines);
  }

  public remove(variantId: number): void {
    this.save(this.snapshot.filter(l => l.variantId !== variantId));
  }

  public clear(): void {
    this.save([]);
  }

  private save(lines: CartLine[]): void {
    this.lines.next(lines);
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(lines));
    } catch {
      /* the cart still works for this tab */
    }
  }

  private restore(): CartLine[] {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as CartLine[]) : [];
    } catch {
      return [];
    }
  }
}

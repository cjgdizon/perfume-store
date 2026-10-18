import { Pipe, PipeTransform } from '@angular/core';
import { ProductVariant } from '../model/product-variant';

@Pipe({ name: 'stockSum' })
export class StockSumPipe implements PipeTransform {
  transform(variants: ProductVariant[]): number {
    return variants.reduce((sum, v) => sum + v.stockQuantity, 0);
  }
}

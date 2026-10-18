import { Component, Input } from '@angular/core';
import { Product } from '../model/product';
import { fromPrice, sellableVariants } from '../model/product-utils';

@Component({
  selector: 'app-product-card',
  templateUrl: './product-card.component.html',
  styleUrls: ['./product-card.component.css']
})
export class ProductCardComponent {
  @Input() product!: Product;

  get from(): number | null {
    return fromPrice(this.product);
  }
  get sizes(): string {
    return sellableVariants(this.product).map(v => v.size).join(' · ');
  }
  get lowOnly(): boolean {
    const v = sellableVariants(this.product);
    return v.length > 0 && v.every(x => x.status === 'LowStock');
  }
}

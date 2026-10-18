import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '../model/product';
import { ProductVariant } from '../model/product-variant';
import { isSellable, monthYear } from '../model/product-utils';
import { CartService } from '../service/cart.service';
import { ProductService } from '../service/product.service';
import { ToastService } from '../service/toast.service';

@Component({
  selector: 'app-product-detail',
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.css']
})
export class ProductDetailComponent implements OnInit {
  product?: Product;
  selected?: ProductVariant;
  quantity = 1;
  loading = true;
  notFound = false;
  failed = false;
  monthYear = monthYear;
  isSellable = isSellable;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cart: CartService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.productService.getProduct(id).subscribe({
      next: p => {
        this.product = p;
        this.selected = p.variants.find(isSellable);
        this.loading = false;
      },
      error: err => {
        this.notFound = err.status === 404;
        this.failed = !this.notFound;
        this.loading = false;
      }
    });
  }

  /** status shown on a size: a sellable-looking variant with nothing left reads "Sold out" */
  label(v: ProductVariant): string {
    return (v.status === 'Active' || v.status === 'LowStock') && v.availableQuantity <= 0 ? 'SoldOut' : v.status;
  }

  choose(variant: ProductVariant): void {
    if (!isSellable(variant)) {
      return;
    }
    this.selected = variant;
    this.quantity = Math.min(this.quantity, variant.availableQuantity);
  }

  step(by: number): void {
    if (this.selected) {
      this.quantity = Math.max(1, Math.min(this.quantity + by, this.selected.availableQuantity));
    }
  }

  addToCart(goToCart: boolean): void {
    if (!this.product || !this.selected) {
      return;
    }
    this.cart.add(this.product, this.selected, this.quantity);
    this.toast.success(`${this.product.name} ${this.selected.size} added to your cart`);
    if (goToCart) {
      this.router.navigate(['/cart']);
    }
  }
}

import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, Subscription, debounceTime, distinctUntilChanged, switchMap, tap } from 'rxjs';
import { Product } from '../model/product';
import { CATEGORIES } from '../model/product-utils';
import { ProductService } from '../service/product.service';

@Component({
  selector: 'app-products',
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.css']
})
export class ProductsComponent implements OnInit, OnDestroy {
  products: Product[] = [];
  categories = CATEGORIES;
  keyword = '';
  category = '';
  loading = true;
  failed = false;

  private search = new Subject<void>();
  private sub = new Subscription();

  constructor(
    private productService: ProductService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const q = this.route.snapshot.queryParamMap;
    this.keyword = q.get('q') ?? '';
    this.category = q.get('category') ?? '';

    // typing is debounced; a newer search cancels the one in flight
    this.sub.add(
      this.search
        .pipe(
          debounceTime(250),
          tap(() => {
            this.loading = true;
            this.failed = false;
            this.router.navigate([], {
              queryParams: { q: this.keyword || null, category: this.category || null },
              replaceUrl: true
            });
          }),
          switchMap(() => this.productService.getProducts(this.keyword, this.category))
        )
        .subscribe({
          next: p => this.show(p),
          error: () => this.fail()
        })
    );
    this.productService.getProducts(this.keyword, this.category).subscribe({
      next: p => this.show(p),
      error: () => this.fail()
    });
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  onKeyword(value: string): void {
    this.keyword = value;
    this.search.next();
  }

  pick(category: string): void {
    this.category = this.category === category ? '' : category;
    this.search.next();
  }

  private show(products: Product[]): void {
    this.products = products;
    this.loading = false;
  }

  private fail(): void {
    this.failed = true;
    this.loading = false;
  }
}

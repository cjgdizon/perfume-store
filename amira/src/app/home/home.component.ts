import { Component, OnInit } from '@angular/core';
import { Product } from '../model/product';
import { sellableVariants } from '../model/product-utils';
import { ProductService } from '../service/product.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  /**
   * Hero scene, drawn on a 1000 x 640 grid: x is the bottle's centre, base is where it stands
   * (y from the top), h its height. z puts bottles on plinths in front of the tall box.
   */
  readonly bottles = [
    { file: 'jadore', x: 480, base: 215, h: 200, z: 2 },
    { file: 'miss-dior', x: 372, base: 400, h: 150, z: 4 },
    { file: 'acqua-di-gio', x: 618, base: 380, h: 170, z: 4 },
    { file: 'black-opium', x: 112, base: 580, h: 175, z: 4 },
    { file: 'sauvage', x: 214, base: 580, h: 190, z: 4 },
    { file: 'eros', x: 815, base: 580, h: 165, z: 4 },
    { file: 'good-girl', x: 930, base: 580, h: 255, z: 4 }
  ];

  featured: Product[] = [];
  loading = true;
  failed = false;

  constructor(private productService: ProductService) {}

  ngOnInit(): void {
    this.productService.getProducts().subscribe({
      next: products => {
        // show perfumes that can actually be bought right now
        this.featured = products.filter(p => sellableVariants(p).length > 0).slice(0, 4);
        this.loading = false;
      },
      error: () => {
        this.failed = true;
        this.loading = false;
      }
    });
  }
}

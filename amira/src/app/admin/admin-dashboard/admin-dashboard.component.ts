import { Component, OnInit } from '@angular/core';
import { forkJoin } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Order } from '../../model/order';
import { ProductVariant } from '../../model/product-variant';
import { InventoryValuation } from '../../model/report';
import { monthYear } from '../../model/product-utils';
import { OrderService } from '../../service/order.service';
import { ProductService } from '../../service/product.service';
import { ReportService } from '../../service/report.service';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  valuation?: InventoryValuation;
  expiring: ProductVariant[] = [];
  lowStock: ProductVariant[] = [];
  toConfirm = 0;
  toShip = 0;
  loading = true;
  failed = false;
  days = environment.expiryWarningDays;
  threshold = environment.lowStockThreshold;
  monthYear = monthYear;

  constructor(
    private reports: ReportService,
    private products: ProductService,
    private orders: OrderService
  ) {}

  ngOnInit(): void {
    forkJoin({
      valuation: this.reports.getValuation(),
      expiring: this.products.getExpiringVariants(this.days),
      lowStock: this.products.getLowStockVariants(this.threshold),
      orders: this.orders.getOrders()
    }).subscribe({
      next: r => {
        this.valuation = r.valuation;
        this.expiring = r.expiring;
        this.lowStock = r.lowStock;
        this.toConfirm = r.orders.filter((o: Order) => o.statusName === 'Pending').length;
        this.toShip = r.orders.filter((o: Order) => o.statusName === 'Paid').length;
        this.loading = false;
      },
      error: () => {
        this.failed = true;
        this.loading = false;
      }
    });
  }
}

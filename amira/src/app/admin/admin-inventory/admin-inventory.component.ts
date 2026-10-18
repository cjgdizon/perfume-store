import { Component, OnInit } from '@angular/core';
import { StockReportLine } from '../../model/report';
import { monthYear } from '../../model/product-utils';
import { ProductService } from '../../service/product.service';
import { ReportService } from '../../service/report.service';
import { ToastService } from '../../service/toast.service';

@Component({
  selector: 'app-admin-inventory',
  templateUrl: './admin-inventory.component.html',
  styleUrls: ['./admin-inventory.component.css']
})
export class AdminInventoryComponent implements OnInit {
  lines: StockReportLine[] = [];
  search = '';
  status = '';
  statuses = ['Active', 'LowStock', 'Expired', 'Discontinued'];
  loading = true;
  failed = false;
  monthYear = monthYear;

  /** variant whose count is being changed, and the number typed so far */
  counting: number | null = null;
  count = 0;
  saving = false;

  constructor(
    private reports: ReportService,
    private products: ProductService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.reports.getStockReport().subscribe({
      next: lines => {
        this.lines = lines;
        this.loading = false;
        this.failed = false;
      },
      error: () => {
        this.loading = false;
        this.failed = true;
      }
    });
  }

  get shown(): StockReportLine[] {
    const q = this.search.trim().toLowerCase();
    return this.lines.filter(
      l =>
        (!this.status || l.status === this.status) &&
        (!q || `${l.productName} ${l.brand} ${l.sku} ${l.batchNumber ?? ''}`.toLowerCase().includes(q))
    );
  }

  get totalCost(): number {
    return this.shown.reduce((sum, l) => sum + l.costValue, 0);
  }

  startCount(l: StockReportLine): void {
    this.counting = l.variantId;
    this.count = l.stockQuantity;
  }

  saveCount(l: StockReportLine): void {
    if (!Number.isInteger(this.count) || this.count < 0) {
      this.toast.error('Enter a whole number of units, zero or more.');
      return;
    }
    this.saving = true;
    this.products.updateStock(l.variantId, this.count).subscribe({
      next: () => {
        this.toast.success(`${l.productName} ${l.size} is now ${this.count} on hand`);
        this.saving = false;
        this.counting = null;
        this.load();
      },
      error: err => {
        this.saving = false;
        this.toast.error(ToastService.messageOf(err));
      }
    });
  }
}

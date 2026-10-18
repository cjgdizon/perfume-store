import { Component, OnInit } from '@angular/core';
import { BestSeller, CategoryRevenue, InventoryValuation, StockReportLine } from '../../model/report';
import { monthYear } from '../../model/product-utils';
import { ReportService } from '../../service/report.service';

function isoDate(d: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

@Component({
  selector: 'app-admin-reports',
  templateUrl: './admin-reports.component.html',
  styleUrls: ['./admin-reports.component.css']
})
export class AdminReportsComponent implements OnInit {
  from = isoDate(new Date(Date.now() - 30 * 86400000));
  to = isoDate(new Date());
  months = 6;

  best: BestSeller[] = [];
  slow: StockReportLine[] = [];
  revenue: CategoryRevenue[] = [];
  valuation?: InventoryValuation;
  failed = false;
  monthYear = monthYear;

  constructor(private reports: ReportService) {}

  ngOnInit(): void {
    this.loadBest();
    this.loadSlow();
    this.reports.getRevenueByCategory().subscribe({ next: r => (this.revenue = r), error: () => (this.failed = true) });
    this.reports.getValuation().subscribe({ next: v => (this.valuation = v), error: () => (this.failed = true) });
  }

  loadBest(): void {
    this.reports.getBestSellers(this.from, this.to).subscribe({ next: b => (this.best = b), error: () => (this.failed = true) });
  }

  loadSlow(): void {
    this.reports.getSlowMoving(this.months).subscribe({ next: s => (this.slow = s), error: () => (this.failed = true) });
  }

  get maxRevenue(): number {
    return Math.max(1, ...this.revenue.map(r => r.revenue));
  }

  get tiedUp(): number {
    return this.slow.reduce((sum, l) => sum + l.costValue, 0);
  }
}

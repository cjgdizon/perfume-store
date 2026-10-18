import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { BestSeller, CategoryRevenue, InventoryValuation, StockReportLine } from '../model/report';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private api = `${environment.apiBaseUrl}/api/report`;

  constructor(private http: HttpClient) {}

  public getStockReport(): Observable<StockReportLine[]> {
    return this.http.get<StockReportLine[]>(`${this.api}/stock`);
  }
  public getBestSellers(from?: string, to?: string): Observable<BestSeller[]> {
    let params = new HttpParams();
    if (from) { params = params.set('from', from); }
    if (to) { params = params.set('to', to); }
    return this.http.get<BestSeller[]>(`${this.api}/best-sellers`, { params });
  }
  public getSlowMoving(months: number): Observable<StockReportLine[]> {
    return this.http.get<StockReportLine[]>(`${this.api}/slow-moving`, { params: { months } });
  }
  public getRevenueByCategory(): Observable<CategoryRevenue[]> {
    return this.http.get<CategoryRevenue[]>(`${this.api}/revenue-by-category`);
  }
  public getValuation(): Observable<InventoryValuation> {
    return this.http.get<InventoryValuation>(`${this.api}/valuation`);
  }
}

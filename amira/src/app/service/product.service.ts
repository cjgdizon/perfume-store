import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Product } from '../model/product';
import { ProductVariant } from '../model/product-variant';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private api = `${environment.apiBaseUrl}/api`;

  constructor(private http: HttpClient) {}

  public getProducts(keyword = '', category = '', brand = ''): Observable<Product[]> {
    const params = new HttpParams().set('keyword', keyword).set('category', category).set('brand', brand);
    return this.http.get<Product[]>(`${this.api}/product`, { params });
  }
  public getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.api}/product/${id}`);
  }
  public createProduct(product: Partial<Product>): Observable<Product> {
    return this.http.post<Product>(`${this.api}/product`, product);
  }
  public updateProduct(product: Partial<Product>): Observable<Product> {
    return this.http.put<Product>(`${this.api}/product`, product);
  }
  public deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/product/${id}`);
  }

  public addVariant(variant: Partial<ProductVariant>): Observable<ProductVariant> {
    return this.http.post<ProductVariant>(`${this.api}/variant`, variant);
  }
  public updateVariant(variant: Partial<ProductVariant>): Observable<ProductVariant> {
    return this.http.put<ProductVariant>(`${this.api}/variant`, variant);
  }
  public updateStock(variantId: number, quantity: number): Observable<ProductVariant> {
    return this.http.put<ProductVariant>(`${this.api}/variant/${variantId}/stock`, { quantity });
  }
  public deactivateVariant(variantId: number): Observable<ProductVariant> {
    return this.http.put<ProductVariant>(`${this.api}/variant/${variantId}/deactivate`, {});
  }
  public deleteVariant(variantId: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/variant/${variantId}`);
  }
  public getExpiringVariants(days: number = environment.expiryWarningDays): Observable<ProductVariant[]> {
    return this.http.get<ProductVariant[]>(`${this.api}/variant/expiring`, { params: { days } });
  }
  public getLowStockVariants(threshold: number = environment.lowStockThreshold): Observable<ProductVariant[]> {
    return this.http.get<ProductVariant[]>(`${this.api}/variant/low-stock`, { params: { threshold } });
  }
}

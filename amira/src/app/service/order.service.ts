import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Order } from '../model/order';
import { OrderItem } from '../model/order-item';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private api = `${environment.apiBaseUrl}/api/order`;

  constructor(private http: HttpClient) {}

  /** owner: every order */
  public getOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(this.api);
  }
  public getOrderHistory(customerId: number): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.api}/customer/${customerId}`);
  }
  public getOrder(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.api}/${id}`);
  }
  public createOrder(items: OrderItem[], shippingAddress: string): Observable<Order> {
    return this.http.post<Order>(this.api, { items, shippingAddress });
  }
  public updateStatus(id: number, status: 'Shipped' | 'Delivered' | 'Cancelled'): Observable<Order> {
    return this.http.put<Order>(`${this.api}/${id}/status`, { status });
  }
  public cancelOrder(id: number): Observable<Order> {
    return this.http.post<Order>(`${this.api}/${id}/cancel`, {});
  }
}

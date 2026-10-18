import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Payment, PaymentMethod } from '../model/payment';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private api = `${environment.apiBaseUrl}/api/payment`;

  constructor(private http: HttpClient) {}

  public processPayment(orderId: number, method: PaymentMethod): Observable<Payment> {
    return this.http.post<Payment>(this.api, { orderId, method });
  }
  public getPayments(orderId: number): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.api}/order/${orderId}`);
  }
  public confirmPayment(paymentId: number): Observable<Payment> {
    return this.http.put<Payment>(`${this.api}/${paymentId}/confirm`, {});
  }
  public refundPayment(paymentId: number): Observable<Payment> {
    return this.http.post<Payment>(`${this.api}/${paymentId}/refund`, {});
  }
}

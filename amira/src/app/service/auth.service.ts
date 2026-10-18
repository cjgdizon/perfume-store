import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, map, tap } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Customer, LoginResponse } from '../model/customer';

const STORAGE_KEY = 'amira.auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private session = new BehaviorSubject<LoginResponse | null>(this.restore());
  /** the signed-in customer, or null */
  readonly customer$: Observable<Customer | null> = this.session.pipe(map(s => s?.customer ?? null));

  constructor(private http: HttpClient) {}

  get token(): string | null {
    return this.session.value?.token ?? null;
  }
  get customer(): Customer | null {
    return this.session.value?.customer ?? null;
  }
  get isLoggedIn(): boolean {
    return this.session.value !== null;
  }
  get isOwner(): boolean {
    return this.customer?.role === 'OWNER';
  }

  public login(email: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.apiBaseUrl}/api/customer/login`, { email, password })
      .pipe(tap(r => this.start(r)));
  }

  public register(customer: Customer): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.apiBaseUrl}/api/customer/register`, customer)
      .pipe(tap(r => this.start(r)));
  }

  public updateProfile(customer: Customer): Observable<Customer> {
    return this.http.put<Customer>(`${environment.apiBaseUrl}/api/customer`, customer).pipe(
      tap(updated => {
        const s = this.session.value;
        if (s && s.customer.id === updated.id) {
          this.start({ token: s.token, customer: updated });
        }
      })
    );
  }

  /** Always succeeds for a well-formed request: the server does not reveal whether the email exists. */
  public forgotPassword(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${environment.apiBaseUrl}/api/customer/forgot-password`, { email });
  }

  public resetPassword(token: string, password: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${environment.apiBaseUrl}/api/customer/reset-password`, { token, password });
  }

  public logout(): void {
    if (this.isLoggedIn) {
      // best effort: the token also expires on its own
      this.http.post(`${environment.apiBaseUrl}/api/customer/logout`, {}).subscribe({ error: () => {} });
    }
    this.clear();
  }

  /** called by the interceptor when the server says the token is no longer valid */
  public clear(): void {
    this.session.next(null);
    try {
      localStorage.removeItem(STORAGE_KEY);
    } catch {
      /* storage unavailable: nothing to clear */
    }
  }

  private start(response: LoginResponse): void {
    this.session.next(response);
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(response));
    } catch {
      /* private mode: the session simply lasts until the tab closes */
    }
  }

  private restore(): LoginResponse | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as LoginResponse) : null;
    } catch {
      return null;
    }
  }
}

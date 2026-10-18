import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from 'src/environments/environment';
import { AuthService } from './auth.service';

/** Adds the bearer token to API calls and signs the user out when the server rejects it. */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private auth: AuthService, private router: Router) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.auth.token;
    if (token && request.url.startsWith(environment.apiBaseUrl)) {
      request = request.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
    }
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        const isLogin = request.url.endsWith('/api/customer/login');
        if (error.status === 401 && token && !isLogin) {
          this.auth.clear();
          this.router.navigate(['/login'], { queryParams: { expired: 1 } });
        }
        return throwError(() => error);
      })
    );
  }
}

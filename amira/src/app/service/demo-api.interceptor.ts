import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, delay, mergeMap, of, throwError, timer } from 'rxjs';
import { environment } from 'src/environments/environment';
import { handleDemoRequest } from '../demo/demo-backend';

/**
 * Answers API calls from the in-browser demo backend instead of a server. Only active in the "demo" build
 * (environment.demo); in every other build it passes requests straight through, so Docker/dev are unchanged.
 * Registered after AuthInterceptor so the bearer token and the 401 sign-out handling still apply.
 */
@Injectable()
export class DemoApiInterceptor implements HttpInterceptor {
  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (!environment.demo || !request.url.startsWith(environment.apiBaseUrl)) {
      return next.handle(request);
    }
    const auth = request.headers.get('Authorization');
    const result = handleDemoRequest({
      method: request.method,
      path: request.url.slice(environment.apiBaseUrl.length),
      query: name => request.params.get(name),
      body: request.body,
      token: auth?.startsWith('Bearer ') ? auth.slice(7) : null
    });
    // a short delay so spinners and loading states behave like they would against a real server
    if (result.status >= 400) {
      const error = new HttpErrorResponse({ status: result.status, error: result.body, url: request.url });
      return timer(200).pipe(mergeMap(() => throwError(() => error)));
    }
    return of(new HttpResponse({ status: result.status, body: result.body, url: request.url })).pipe(delay(200));
  }
}

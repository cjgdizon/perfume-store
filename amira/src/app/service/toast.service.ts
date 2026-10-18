import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Toast {
  id: number;
  kind: 'success' | 'error';
  message: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private next = 1;
  private items = new BehaviorSubject<Toast[]>([]);
  readonly toasts$ = this.items.asObservable();

  public success(message: string): void {
    this.push('success', message, 4000);
  }

  public error(message: string): void {
    this.push('error', message, 7000);
  }

  public dismiss(id: number): void {
    this.items.next(this.items.value.filter(t => t.id !== id));
  }

  /** The server sends {"message": "..."} for every failure; fall back to a plain sentence. */
  public static messageOf(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'Cannot reach the server. Check that the backend is running.';
      }
      const body = error.error as { message?: string } | null;
      if (body && typeof body.message === 'string' && body.message) {
        return body.message;
      }
    }
    return 'Something went wrong. Please try again.';
  }

  private push(kind: Toast['kind'], message: string, ms: number): void {
    const id = this.next++;
    this.items.next([...this.items.value, { id, kind, message }]);
    setTimeout(() => this.dismiss(id), ms);
  }
}

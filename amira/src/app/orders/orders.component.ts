import { Component, OnInit } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { Order } from '../model/order';
import { PAYMENT_METHODS, Payment, PaymentMethod } from '../model/payment';
import { AuthService } from '../service/auth.service';
import { OrderService } from '../service/order.service';
import { PaymentService } from '../service/payment.service';
import { ToastService } from '../service/toast.service';

@Component({
  selector: 'app-orders',
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.css']
})
export class OrdersComponent implements OnInit {
  orders: Order[] = [];
  /** payments waiting on the shop (bank transfer / COD), by order id */
  waiting: Record<number, Payment> = {};
  methods = PAYMENT_METHODS;
  payWith: Record<number, PaymentMethod> = {};
  busy: number | null = null;
  loading = true;
  failed = false;

  constructor(
    private auth: AuthService,
    private orderService: OrderService,
    private paymentService: PaymentService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    const me = this.auth.customer;
    if (!me) {
      return;
    }
    this.orderService.getOrderHistory(me.id).subscribe({
      next: orders => {
        this.orders = orders;
        this.loading = false;
        this.failed = false;
        const open = orders.filter(o => o.statusName === 'Pending');
        forkJoin(open.length ? open.map(o => this.paymentService.getPayments(o.id)) : [of([] as Payment[])]).subscribe(
          lists => {
            this.waiting = {};
            lists.flat().filter(p => p.status === 'Pending').forEach(p => (this.waiting[p.orderId] = p));
          }
        );
      },
      error: () => {
        this.loading = false;
        this.failed = true;
      }
    });
  }

  canPay(o: Order): boolean {
    return o.statusName === 'Pending' && o.paymentStatus === 'Unpaid' && !this.waiting[o.id];
  }

  canCancel(o: Order): boolean {
    return o.statusName === 'Pending' || o.statusName === 'Paid';
  }

  pay(o: Order): void {
    this.busy = o.id;
    this.paymentService.processPayment(o.id, this.payWith[o.id] ?? 'GCash').subscribe({
      next: p => {
        this.toast.success(p.status === 'Completed' ? `Order #${o.id} is paid` : `Payment recorded. The shop will confirm it shortly.`);
        this.busy = null;
        this.load();
      },
      error: err => {
        this.toast.error(ToastService.messageOf(err));
        this.busy = null;
      }
    });
  }

  cancel(o: Order): void {
    const refund = o.paymentStatus === 'Paid' ? ' Your payment will be refunded.' : '';
    if (!confirm(`Cancel order #${o.id}?${refund}`)) {
      return;
    }
    this.busy = o.id;
    this.orderService.cancelOrder(o.id).subscribe({
      next: () => {
        this.toast.success(`Order #${o.id} was cancelled`);
        this.busy = null;
        this.load();
      },
      error: err => {
        this.toast.error(ToastService.messageOf(err));
        this.busy = null;
      }
    });
  }
}

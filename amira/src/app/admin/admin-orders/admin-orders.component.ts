import { Component, OnInit } from '@angular/core';
import { Order } from '../../model/order';
import { Payment } from '../../model/payment';
import { OrderService } from '../../service/order.service';
import { PaymentService } from '../../service/payment.service';
import { ToastService } from '../../service/toast.service';

@Component({
  selector: 'app-admin-orders',
  templateUrl: './admin-orders.component.html',
  styleUrls: ['./admin-orders.component.css']
})
export class AdminOrdersComponent implements OnInit {
  orders: Order[] = [];
  filter = '';
  statuses = ['Pending', 'Paid', 'Shipped', 'Delivered', 'Cancelled'];
  loading = true;
  failed = false;
  open: number | null = null;
  payments: Payment[] = [];
  busy = false;

  constructor(
    private orderService: OrderService,
    private paymentService: PaymentService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.orderService.getOrders().subscribe({
      next: o => {
        this.orders = o;
        this.loading = false;
        this.failed = false;
        if (this.open !== null) {
          this.loadPayments(this.open);
        }
      },
      error: () => {
        this.loading = false;
        this.failed = true;
      }
    });
  }

  get shown(): Order[] {
    return this.filter ? this.orders.filter(o => o.statusName === this.filter) : this.orders;
  }

  toggle(o: Order): void {
    if (this.open === o.id) {
      this.open = null;
      return;
    }
    this.open = o.id;
    this.loadPayments(o.id);
  }

  private loadPayments(orderId: number): void {
    this.payments = [];
    this.paymentService.getPayments(orderId).subscribe(p => (this.payments = p));
  }

  pendingPayment(): Payment | undefined {
    return this.payments.find(p => p.status === 'Pending');
  }

  completedPayment(): Payment | undefined {
    return this.payments.find(p => p.status === 'Completed');
  }

  refund(p: Payment): void {
    if (!confirm(`Refund ${p.amount.toFixed(2)} for order #${p.orderId}? The returned bottles are not put back on sale automatically; re-count them in Inventory.`)) {
      return;
    }
    this.run(this.paymentService.refundPayment(p.id), `Payment for order #${p.orderId} refunded`);
  }

  confirm(p: Payment): void {
    this.run(this.paymentService.confirmPayment(p.id), `Payment for order #${p.orderId} confirmed. Stock was deducted.`);
  }

  advance(o: Order, status: 'Shipped' | 'Delivered'): void {
    this.run(this.orderService.updateStatus(o.id, status), `Order #${o.id} marked ${status.toLowerCase()}`);
  }

  cancel(o: Order): void {
    const refund = o.paymentStatus === 'Paid' ? ' The payment is refunded and the stock goes back on hand.' : ' The held stock is released.';
    if (!confirm(`Cancel order #${o.id}?${refund}`)) {
      return;
    }
    this.run(this.orderService.cancelOrder(o.id), `Order #${o.id} cancelled`);
  }

  private run(call: import('rxjs').Observable<unknown>, success: string): void {
    this.busy = true;
    call.subscribe({
      next: () => {
        this.busy = false;
        this.toast.success(success);
        this.load();
      },
      error: err => {
        this.busy = false;
        this.toast.error(ToastService.messageOf(err));
      }
    });
  }
}

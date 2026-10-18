import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { PAYMENT_METHODS } from '../model/payment';
import { AuthService } from '../service/auth.service';
import { CartService } from '../service/cart.service';
import { OrderService } from '../service/order.service';
import { PaymentService } from '../service/payment.service';
import { ToastService } from '../service/toast.service';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})
export class CheckoutComponent implements OnInit {
  methods = PAYMENT_METHODS;
  submitting = false;
  error = '';

  form = this.fb.nonNullable.group({
    shippingAddress: ['', [Validators.required, Validators.minLength(10)]],
    method: ['GCash', Validators.required]
  });

  constructor(
    private fb: FormBuilder,
    public cart: CartService,
    private auth: AuthService,
    private orders: OrderService,
    private payments: PaymentService,
    private toast: ToastService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.cart.snapshot.length) {
      this.router.navigate(['/cart']);
      return;
    }
    // reuse what we already know instead of asking again
    this.form.patchValue({ shippingAddress: this.auth.customer?.shippingAddress ?? '' });
  }

  get address() {
    return this.form.controls.shippingAddress;
  }

  place(): void {
    this.error = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    const items = this.cart.snapshot.map(l => ({ variantId: l.variantId, quantity: l.quantity }));
    const { shippingAddress, method } = this.form.getRawValue();

    this.orders.createOrder(items, shippingAddress).subscribe({
      next: order => {
        // the order exists and stock is held; now pay for it
        this.payments.processPayment(order.id, method as never).subscribe({
          next: payment => {
            this.cart.clear();
            this.toast.success(
              payment.status === 'Completed'
                ? `Order #${order.id} is paid. Thank you!`
                : `Order #${order.id} placed. We will confirm your ${method} payment shortly.`
            );
            this.router.navigate(['/orders']);
          },
          error: err => {
            // keep the order: the customer can retry payment from the orders page
            this.cart.clear();
            this.toast.error(`Order #${order.id} was placed but payment failed: ${ToastService.messageOf(err)}`);
            this.router.navigate(['/orders']);
          }
        });
      },
      error: err => {
        this.submitting = false;
        this.error = ToastService.messageOf(err);
      }
    });
  }
}

import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../service/auth.service';
import { ToastService } from '../service/toast.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  submitting = false;
  error = '';
  showPassword = false;

  form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    phone: [''],
    shippingAddress: [''],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private toast: ToastService
  ) {}

  submit(): void {
    this.error = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    this.auth.register({ ...this.form.getRawValue(), id: 0, role: 'CUSTOMER' }).subscribe({
      next: res => {
        this.toast.success(`Welcome, ${res.customer.name}!`);
        this.router.navigate(['/products']);
      },
      error: err => {
        this.submitting = false;
        this.error = ToastService.messageOf(err);
      }
    });
  }

  invalid(name: 'name' | 'email' | 'password'): boolean {
    const c = this.form.controls[name];
    return c.invalid && c.touched;
  }
}

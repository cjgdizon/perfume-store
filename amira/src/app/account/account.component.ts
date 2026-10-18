import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../service/auth.service';
import { ToastService } from '../service/toast.service';

@Component({
  selector: 'app-account',
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.css']
})
export class AccountComponent implements OnInit {
  saving = false;
  error = '';

  form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    phone: [''],
    shippingAddress: [''],
    password: ['', Validators.minLength(6)]
  });

  constructor(private fb: FormBuilder, public auth: AuthService, private toast: ToastService) {}

  ngOnInit(): void {
    const me = this.auth.customer;
    if (me) {
      this.form.patchValue({ name: me.name, phone: me.phone ?? '', shippingAddress: me.shippingAddress ?? '' });
    }
  }

  save(): void {
    const me = this.auth.customer;
    if (!me) {
      return;
    }
    this.error = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    const v = this.form.getRawValue();
    this.auth.updateProfile({ ...me, name: v.name, phone: v.phone, shippingAddress: v.shippingAddress, password: v.password }).subscribe({
      next: () => {
        this.saving = false;
        this.form.controls.password.reset('');
        this.toast.success('Your details are saved');
      },
      error: err => {
        this.saving = false;
        this.error = ToastService.messageOf(err);
      }
    });
  }
}

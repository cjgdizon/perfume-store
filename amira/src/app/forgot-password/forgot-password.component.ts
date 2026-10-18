import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../service/auth.service';
import { ToastService } from '../service/toast.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  submitting = false;
  sent = false;
  error = '';

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]]
  });

  constructor(private fb: FormBuilder, private auth: AuthService) {}

  submit(): void {
    this.error = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    this.auth.forgotPassword(this.form.getRawValue().email).subscribe({
      // the server gives the same answer for every email, so this page does too
      next: () => {
        this.submitting = false;
        this.sent = true;
      },
      error: err => {
        this.submitting = false;
        this.error = ToastService.messageOf(err);
      }
    });
  }
}

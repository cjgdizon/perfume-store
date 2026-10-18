import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../service/auth.service';
import { ToastService } from '../service/toast.service';

function sameAsPassword(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirm = group.get('confirm')?.value;
  return confirm && password !== confirm ? { mismatch: true } : null;
}

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent {
  token = this.route.snapshot.queryParamMap.get('token') ?? '';
  submitting = false;
  showPassword = false;
  /** set when the server rejects the link, so we can point to a new request */
  linkProblem = false;
  error = '';

  form = this.fb.nonNullable.group(
    {
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirm: ['', Validators.required]
    },
    { validators: sameAsPassword }
  );

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private route: ActivatedRoute,
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
    this.auth.resetPassword(this.token, this.form.getRawValue().password).subscribe({
      next: () => {
        this.toast.success('Your password was changed. Please sign in.');
        this.router.navigate(['/login']);
      },
      error: err => {
        this.submitting = false;
        this.error = ToastService.messageOf(err);
        // a 400 about the link (not the password length) means it is used up or expired
        this.linkProblem = err.status === 400 && /link/i.test(this.error);
      }
    });
  }
}

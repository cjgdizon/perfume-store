import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../service/auth.service';
import { ToastService } from '../service/toast.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  submitting = false;
  error = '';
  showPassword = false;
  expired = this.route.snapshot.queryParamMap.has('expired');

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  submit(): void {
    this.error = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    const { email, password } = this.form.getRawValue();
    this.auth.login(email, password).subscribe({
      next: res => {
        const back = this.route.snapshot.queryParamMap.get('returnUrl');
        // only follow in-app return paths
        const safe = back && back.startsWith('/') && !back.startsWith('//') ? back : null;
        this.router.navigateByUrl(safe ?? (res.customer.role === 'OWNER' ? '/admin' : '/products'));
      },
      error: err => {
        this.submitting = false;
        this.error = ToastService.messageOf(err);
      }
    });
  }
}

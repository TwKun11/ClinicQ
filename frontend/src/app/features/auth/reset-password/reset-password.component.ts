import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule],
  template: `
    <form [formGroup]="form" (ngSubmit)="onSubmit()">
      <mat-form-field class="full-width">
        <mat-label>New password</mat-label>
        <input matInput type="password" formControlName="newPassword">
      </mat-form-field>

      <button mat-raised-button color="primary" type="submit" class="full-width" [disabled]="form.invalid || loading">
        {{ loading ? 'Resetting...' : 'Reset password' }}
      </button>

      <p style="text-align: center; margin-top: 16px;">
        <a routerLink="/login">Back to login</a>
      </p>
    </form>
  `
})
export class ResetPasswordComponent {
  loading = false;
  form = this.fb.group({
    newPassword: ['', [Validators.required, Validators.minLength(6)]]
  });

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private notification: NotificationService
  ) {}

  onSubmit(): void {
    if (this.form.invalid) return;
    const token = this.route.snapshot.queryParamMap.get('token') || '';
    if (!token) {
      this.notification.error('Reset token is missing');
      return;
    }
    this.loading = true;
    this.authService.resetPassword(token, this.form.getRawValue().newPassword!).subscribe({
      next: () => {
        this.loading = false;
        this.notification.success('Password reset successful. Please log in.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.loading = false;
        this.notification.error(err.error?.message || 'Password reset failed');
      }
    });
  }
}

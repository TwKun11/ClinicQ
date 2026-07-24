import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule],
  template: `
    <form [formGroup]="form" (ngSubmit)="onSubmit()">
      <mat-form-field class="full-width">
        <mat-label>Email</mat-label>
        <input matInput type="email" formControlName="email">
      </mat-form-field>

      <button mat-raised-button color="primary" type="submit" class="full-width" [disabled]="form.invalid || loading">
        {{ loading ? 'Sending...' : 'Send reset link' }}
      </button>

      <p style="text-align: center; margin-top: 16px;">
        <a routerLink="/login">Back to login</a>
      </p>
    </form>
  `
})
export class ForgotPasswordComponent {
  loading = false;
  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private notification: NotificationService
  ) {}

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    const email = this.form.getRawValue().email!;
    this.authService.forgotPassword(email).subscribe({
      next: () => {
        this.loading = false;
        this.notification.success('If the email exists, a reset link has been sent.');
      },
      error: (err) => {
        this.loading = false;
        this.notification.error(err.error?.message || 'Could not send reset link');
      }
    });
  }
}

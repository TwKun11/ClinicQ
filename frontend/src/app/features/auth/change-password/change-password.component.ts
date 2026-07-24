import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  template: `
    <h1>Change password</h1>
    <form [formGroup]="form" (ngSubmit)="onSubmit()" style="max-width: 420px;">
      <mat-form-field class="full-width">
        <mat-label>Current password</mat-label>
        <input matInput type="password" formControlName="currentPassword">
      </mat-form-field>

      <mat-form-field class="full-width">
        <mat-label>New password</mat-label>
        <input matInput type="password" formControlName="newPassword">
      </mat-form-field>

      <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid || loading">
        {{ loading ? 'Saving...' : 'Save password' }}
      </button>
    </form>
  `
})
export class ChangePasswordComponent {
  loading = false;
  form = this.fb.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(6)]]
  });

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private notification: NotificationService
  ) {}

  onSubmit(): void {
    if (this.form.invalid) return;
    const raw = this.form.getRawValue();
    this.loading = true;
    this.authService.changePassword(raw.currentPassword!, raw.newPassword!).subscribe({
      next: () => {
        this.loading = false;
        this.form.reset();
        this.notification.success('Password changed successfully');
      },
      error: (err) => {
        this.loading = false;
        this.notification.error(err.error?.message || 'Could not change password');
      }
    });
  }
}

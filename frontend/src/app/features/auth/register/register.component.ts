import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';

function passwordsMatch(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  return password && confirmPassword && password !== confirmPassword ? { passwordsMismatch: true } : null;
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  template: `
    <form [formGroup]="form" (ngSubmit)="onSubmit()">
      <mat-form-field class="full-width">
        <mat-label>Username</mat-label>
        <input matInput formControlName="username">
      </mat-form-field>

      <mat-form-field class="full-width">
        <mat-label>Email</mat-label>
        <input matInput type="email" formControlName="email">
      </mat-form-field>

      <mat-form-field class="full-width">
        <mat-label>Full Name</mat-label>
        <input matInput formControlName="fullName">
      </mat-form-field>

      <mat-form-field class="full-width">
        <mat-label>Password</mat-label>
        <input matInput type="password" formControlName="password">
      </mat-form-field>

      <div class="password-rules" aria-live="polite">
        <div class="password-rule" [class.valid]="hasMinLength" [class.invalid]="!hasMinLength">
          <mat-icon>{{ hasMinLength ? 'check_circle' : 'cancel' }}</mat-icon>
          <span>At least 8 characters</span>
        </div>
        <div class="password-rule" [class.valid]="hasLetter" [class.invalid]="!hasLetter">
          <mat-icon>{{ hasLetter ? 'check_circle' : 'cancel' }}</mat-icon>
          <span>Contains at least one letter</span>
        </div>
        <div class="password-rule" [class.valid]="hasNumber" [class.invalid]="!hasNumber">
          <mat-icon>{{ hasNumber ? 'check_circle' : 'cancel' }}</mat-icon>
          <span>Contains at least one number</span>
        </div>
      </div>

      <mat-form-field class="full-width">
        <mat-label>Confirm Password</mat-label>
        <input matInput type="password" formControlName="confirmPassword">
        @if (form.hasError('passwordsMismatch') && form.get('confirmPassword')?.touched) {
          <mat-error>Password confirmation does not match</mat-error>
        }
      </mat-form-field>

      <button mat-raised-button color="primary" type="submit" class="full-width" [disabled]="form.invalid || loading">
        {{ loading ? 'Registering...' : 'Register' }}
      </button>

      <p style="text-align: center; margin-top: 16px;">
        Already have an account? <a routerLink="/login">Login</a>
      </p>
    </form>
  `,
  styles: [`
    .password-rules {
      display: grid;
      gap: 6px;
      margin: -8px 0 14px;
      font-size: 13px;
    }

    .password-rule {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .password-rule mat-icon {
      width: 18px;
      height: 18px;
      font-size: 18px;
    }

    .password-rule.valid {
      color: #2e7d32;
    }

    .password-rule.invalid {
      color: #c62828;
    }
  `]
})
export class RegisterComponent {
  loading = false;
  form = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    fullName: ['', Validators.required],
    password: ['', [
      Validators.required,
      Validators.minLength(8),
      Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d).+$/)
    ]],
    confirmPassword: ['', Validators.required]
  }, { validators: passwordsMatch });

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private notification: NotificationService
  ) {}

  get password(): string {
    return this.form.get('password')?.value || '';
  }

  get hasMinLength(): boolean {
    return this.password.length >= 8;
  }

  get hasLetter(): boolean {
    return /[A-Za-z]/.test(this.password);
  }

  get hasNumber(): boolean {
    return /\d/.test(this.password);
  }

  onSubmit(): void {
    if (this.form.valid) {
      this.loading = true;
      const { confirmPassword, ...payload } = this.form.getRawValue();
      this.authService.register(payload as { username: string; email: string; password: string; fullName: string }).subscribe({
        next: () => {
          this.notification.success('Registration pending. Check your email to verify your account.');
          this.router.navigate(['/login']);
        },
        error: (err) => {
          this.loading = false;
          this.notification.error(err.error?.message || 'Registration failed');
        }
      });
    }
  }
}

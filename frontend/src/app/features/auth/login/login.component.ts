import { AfterViewInit, Component, NgZone } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { environment } from '../../../../environments/environment';

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (config: { client_id: string; callback: (response: { credential?: string }) => void }) => void;
          renderButton: (element: HTMLElement, options: { theme: string; size: string; width?: number }) => void;
        };
      };
    };
  }
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  template: `
    <form [formGroup]="form" (ngSubmit)="onSubmit()">
      <mat-form-field class="full-width">
        <mat-label>Email</mat-label>
        <input matInput type="email" formControlName="email">
      </mat-form-field>

      <mat-form-field class="full-width">
        <mat-label>Password</mat-label>
        <input matInput type="password" formControlName="password">
      </mat-form-field>

      <button mat-raised-button color="primary" type="submit" class="full-width" [disabled]="form.invalid || loading">
        {{ loading ? 'Logging in...' : 'Login' }}
      </button>

      <div class="divider">or</div>

      <div id="google-login-button" class="google-button"></div>

      <p style="text-align: center; margin-top: 16px;">
        <a routerLink="/forgot-password">Forgot password?</a>
      </p>

      <p style="text-align: center; margin-top: 16px;">
        Don't have an account? <a routerLink="/register">Register</a>
      </p>
    </form>
  `,
  styles: [`
    .divider {
      color: #777;
      margin: 18px 0;
      text-align: center;
    }

    .google-button {
      display: flex;
      justify-content: center;
      min-height: 44px;
    }
  `]
})
export class LoginComponent implements AfterViewInit {
  loading = false;
  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private notification: NotificationService,
    private zone: NgZone
  ) {}

  ngAfterViewInit(): void {
    this.loadGoogleScript().then(() => this.renderGoogleButton());
  }

  onSubmit(): void {
    if (this.form.valid) {
      this.loading = true;
      this.authService.login(this.form.getRawValue() as { email: string; password: string }).subscribe({
        next: (res) => {
          this.notification.success('Login successful');
          this.router.navigate([this.authService.landingRouteForRole(res.data?.role)]);
        },
        error: (err) => {
          this.loading = false;
          this.notification.error(err.error?.message || 'Login failed');
        }
      });
    }
  }

  private renderGoogleButton(): void {
    const target = document.getElementById('google-login-button');
    if (!target || !window.google) {
      return;
    }

    window.google.accounts.id.initialize({
      client_id: environment.googleClientId,
      callback: (response) => this.zone.run(() => this.onGoogleCredential(response.credential))
    });
    window.google.accounts.id.renderButton(target, {
      theme: 'outline',
      size: 'large',
      width: 320
    });
  }

  private onGoogleCredential(credential?: string): void {
    if (!credential) {
      this.notification.error('Google login failed');
      return;
    }

    this.loading = true;
    this.authService.googleLogin(credential).subscribe({
      next: (res) => {
        this.notification.success('Login successful');
        this.router.navigate([this.authService.landingRouteForRole(res.data?.role)]);
      },
      error: (err) => {
        this.loading = false;
        this.notification.error(err.error?.message || 'Google login failed');
      }
    });
  }

  private loadGoogleScript(): Promise<void> {
    if (window.google) {
      return Promise.resolve();
    }

    return new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = 'https://accounts.google.com/gsi/client';
      script.async = true;
      script.defer = true;
      script.onload = () => resolve();
      script.onerror = () => reject();
      document.head.appendChild(script);
    });
  }
}

import { AfterViewInit, Component, NgZone } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
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
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatCheckboxModule, MatIconModule],
  template: `
    <main class="login-page">
      <header class="site-header">
        <a class="brand" routerLink="/login" aria-label="YouMed">
          <span class="brand-mark">Y</span>
          <span class="brand-you">You</span><span class="brand-med">Med</span>
        </a>

        <nav class="main-nav" aria-label="Main navigation">
          <a href="#">Đặt khám <span class="chevron">⌄</span></a>
          <a href="#">Tư vấn trực tuyến</a>
          <a href="#">Tin Y tế</a>
          <a href="#">Trợ lý y khoa</a>
        </nav>

        <a class="outline-login" routerLink="/login">Đăng nhập</a>
      </header>

      <section class="login-content" aria-label="Đăng nhập YouMed">
        <div class="promo-panel">
          <img src="assets/images/login-illustration.png" alt="QR code tải ứng dụng YouMed">
        </div>

        <form class="login-card" [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="auth-tabs" role="tablist" aria-label="Đăng nhập hoặc đăng ký">
            <button class="auth-tab active" type="button" role="tab" aria-selected="true">Đăng nhập</button>
            <a class="auth-tab" routerLink="/register" href="/register" (click)="goToRegister($event)" role="tab" aria-selected="false">Đăng ký</a>
          </div>

          <label class="field-label" for="email">Email</label>
          <input
            id="email"
            class="text-input"
            type="email"
            formControlName="email"
            autocomplete="email"
            placeholder="Nhập email">

          <label class="field-label" for="password">Mật khẩu</label>
          <div class="password-field">
            <input
              id="password"
              class="text-input"
              [type]="showPassword ? 'text' : 'password'"
              formControlName="password"
              autocomplete="current-password"
              placeholder="Nhập mật khẩu">
            <button class="visibility-button" type="button" (click)="showPassword = !showPassword" [attr.aria-label]="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'">
              <mat-icon>{{ showPassword ? 'visibility' : 'visibility_off' }}</mat-icon>
            </button>
          </div>

          <div class="form-tools">
            <mat-checkbox formControlName="rememberMe">Ghi nhớ mật khẩu</mat-checkbox>
            <a routerLink="/forgot-password">Quên mật khẩu?</a>
          </div>

          <button mat-flat-button class="submit-button" type="submit" [disabled]="form.invalid || loading">
            {{ loading ? 'Đang đăng nhập...' : 'Đăng nhập' }}
          </button>

          <div class="social-divider">hoặc</div>
          <div id="google-login-button" class="google-button"></div>

          <div class="card-divider"></div>

          <p class="register-prompt">
            Chưa có tài khoản? <a routerLink="/register" href="/register" (click)="goToRegister($event)">Đăng ký ngay</a>
          </p>
        </form>
      </section>
    </main>
  `,
  styles: [`
    :host {
      display: block;
      min-height: 100vh;
      background: #f6f6f6;
      color: #171717;
    }

    .login-page {
      min-height: 100vh;
    }

    .site-header {
      display: flex;
      align-items: center;
      gap: 32px;
      height: 82px;
      padding: 0 30px;
      background: #ffffff;
      border-bottom: 1px solid #eceff3;
    }

    .brand {
      display: inline-flex;
      align-items: center;
      text-decoration: none;
      font-size: 36px;
      font-weight: 800;
      line-height: 1;
      letter-spacing: 0;
    }

    .brand-mark {
      display: grid;
      place-items: center;
      width: 32px;
      height: 32px;
      margin-right: 2px;
      border-radius: 9px;
      background: #155bd4;
      color: #ffffff;
      font-size: 25px;
      font-weight: 900;
    }

    .brand-you {
      color: #155bd4;
    }

    .brand-med {
      color: #11b66a;
    }

    .main-nav {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      gap: 56px;
      flex: 1;
      font-size: 18px;
      font-weight: 700;
    }

    .main-nav a,
    .outline-login,
    .form-tools a,
    .register-prompt a,
    .auth-tab {
      color: #096fe7;
      text-decoration: none;
    }

    .main-nav a {
      color: #151515;
    }

    .chevron {
      color: #9aa4b2;
      font-size: 18px;
    }

    .outline-login {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 142px;
      height: 46px;
      border: 1px solid #096fe7;
      border-radius: 4px;
      font-size: 17px;
      font-weight: 700;
    }

    .login-content {
      display: grid;
      grid-template-columns: minmax(360px, 680px) minmax(360px, 560px);
      align-items: center;
      justify-content: center;
      gap: 86px;
      min-height: calc(100vh - 82px);
      padding: 64px 48px;
      box-sizing: border-box;
    }

    .promo-panel {
      display: flex;
      justify-content: center;
    }

    .promo-panel img {
      display: block;
      width: min(100%, 650px);
      height: auto;
    }

    .login-card {
      width: min(100%, 560px);
      padding: 36px 30px 60px;
      background: #ffffff;
      border: 1px solid #edf0f4;
      border-radius: 8px;
      box-sizing: border-box;
    }

    .auth-tabs {
      display: grid;
      grid-template-columns: 1fr 1fr;
      border-bottom: 1px solid #e9e9e9;
      margin-bottom: 22px;
    }

    .auth-tab {
      display: flex;
      align-items: center;
      justify-content: center;
      border: 0;
      background: transparent;
      min-height: 48px;
      font: inherit;
      font-size: 18px;
      font-weight: 700;
      text-align: center;
      cursor: pointer;
      color: #151515;
    }

    .auth-tab.active {
      color: #096fe7;
      border-bottom: 2px solid #096fe7;
    }

    .field-label {
      display: block;
      margin: 0 0 7px;
      font-size: 18px;
      font-weight: 500;
    }

    .text-input {
      width: 100%;
      height: 52px;
      padding: 0 16px;
      border: 1px solid #e0e4ea;
      border-radius: 7px;
      box-sizing: border-box;
      font: inherit;
      font-size: 18px;
      outline: none;
      background: #ffffff;
      transition: border-color 160ms ease, box-shadow 160ms ease;
    }

    .text-input:focus {
      border-color: #096fe7;
      box-shadow: 0 0 0 3px rgba(9, 111, 231, 0.12);
    }

    .password-field {
      position: relative;
      margin-bottom: 24px;
    }

    .password-field .text-input {
      padding-right: 52px;
    }

    .visibility-button {
      position: absolute;
      top: 50%;
      right: 10px;
      display: grid;
      place-items: center;
      width: 36px;
      height: 36px;
      border: 0;
      background: transparent;
      color: #98a1ad;
      transform: translateY(-50%);
      cursor: pointer;
    }

    .visibility-button mat-icon {
      width: 22px;
      height: 22px;
      font-size: 22px;
    }

    .form-tools {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      margin-bottom: 32px;
      font-size: 17px;
    }

    .submit-button {
      width: 100%;
      height: 58px;
      border-radius: 5px;
      font-size: 18px;
      font-weight: 800;
      background: #096fe7;
      color: #ffffff;
    }

    .submit-button:disabled {
      background: #cccccc;
      color: #ffffff;
    }

    .social-divider {
      color: #7a8491;
      margin: 22px 0 16px;
      text-align: center;
      font-size: 16px;
    }

    .google-button {
      display: flex;
      justify-content: center;
      min-height: 44px;
    }

    .card-divider {
      height: 1px;
      margin: 40px 0 30px;
      background: #e9e9e9;
    }

    .register-prompt {
      margin: 0;
      text-align: center;
      font-size: 18px;
    }

    @media (max-width: 1080px) {
      .main-nav {
        gap: 24px;
        font-size: 16px;
      }

      .login-content {
        gap: 40px;
      }
    }

    @media (max-width: 860px) {
      .site-header {
        height: auto;
        min-height: 72px;
        padding: 16px 20px;
        flex-wrap: wrap;
      }

      .main-nav {
        order: 3;
        width: 100%;
        justify-content: flex-start;
        overflow-x: auto;
        white-space: nowrap;
      }

      .outline-login {
        margin-left: auto;
      }

      .login-content {
        grid-template-columns: 1fr;
        min-height: auto;
        padding: 36px 20px;
      }

      .promo-panel img {
        max-width: 420px;
      }
    }

    @media (max-width: 520px) {
      .brand {
        font-size: 29px;
      }

      .brand-mark {
        width: 28px;
        height: 28px;
        font-size: 22px;
      }

      .outline-login {
        min-width: 112px;
        height: 40px;
        font-size: 15px;
      }

      .login-card {
        padding: 26px 18px 36px;
      }

      .form-tools {
        align-items: flex-start;
        flex-direction: column;
      }
    }
  `]
})
export class LoginComponent implements AfterViewInit {
  loading = false;
  showPassword = false;
  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
    rememberMe: [false]
  });

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private notification: NotificationService,
    private zone: NgZone
  ) {}

  ngAfterViewInit(): void {
    this.loadGoogleScript()
      .then(() => this.renderGoogleButton())
      .catch(() => this.notification.error('Không tải được Google login'));
  }

  goToRegister(event: Event): void {
    event.preventDefault();
    this.router.navigate(['/register']);
  }

  onSubmit(): void {
    if (this.form.valid) {
      this.loading = true;
      const { email, password } = this.form.getRawValue();
      this.authService.login({ email: email || '', password: password || '' }).subscribe({
        next: (res) => {
          this.notification.success('Đăng nhập thành công');
          this.router.navigate([this.authService.landingRouteForRole(res.data?.role)]);
        },
        error: (err) => {
          this.loading = false;
          this.notification.error(err.error?.message || 'Đăng nhập thất bại');
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
      this.notification.error('Google login thất bại');
      return;
    }

    this.loading = true;
    this.authService.googleLogin(credential).subscribe({
      next: (res) => {
        this.notification.success('Đăng nhập thành công');
        this.router.navigate([this.authService.landingRouteForRole(res.data?.role)]);
      },
      error: (err) => {
        this.loading = false;
        this.notification.error(err.error?.message || 'Google login thất bại');
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

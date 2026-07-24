import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatCheckboxModule, MatIconModule],
  template: `
    <main class="register-page">
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

      <section class="register-content" aria-label="Đăng ký YouMed">
        <div class="promo-panel">
          <img src="assets/images/login-illustration.png" alt="QR code tải ứng dụng YouMed">
        </div>

        <form class="register-card" [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="auth-tabs" role="tablist" aria-label="Đăng nhập hoặc đăng ký">
            <a class="auth-tab" routerLink="/login" role="tab" aria-selected="false">Đăng nhập</a>
            <button class="auth-tab active" type="button" role="tab" aria-selected="true">Đăng ký</button>
          </div>

          <label class="field-label" for="fullName">Họ và tên</label>
          <input id="fullName" class="text-input" type="text" formControlName="fullName" autocomplete="name" placeholder="Nhập họ và tên">

          <label class="field-label" for="username">Số điện thoại</label>
          <input id="username" class="text-input" type="tel" formControlName="username" autocomplete="username" inputmode="tel" placeholder="Số điện thoại">

          <label class="field-label" for="email">Email</label>
          <input id="email" class="text-input" type="email" formControlName="email" autocomplete="email" placeholder="Nhập email">

          <label class="field-label" for="password">Mật khẩu</label>
          <div class="password-field">
            <input id="password" class="text-input" [type]="showPassword ? 'text' : 'password'" formControlName="password" autocomplete="new-password" placeholder="Nhập mật khẩu">
            <button class="visibility-button" type="button" (click)="showPassword = !showPassword" [attr.aria-label]="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'">
              <mat-icon>{{ showPassword ? 'visibility' : 'visibility_off' }}</mat-icon>
            </button>
          </div>

          <label class="field-label" for="confirmPassword">Nhập lại mật khẩu</label>
          <div class="password-field compact">
            <input id="confirmPassword" class="text-input" [type]="showConfirmPassword ? 'text' : 'password'" formControlName="confirmPassword" autocomplete="new-password" placeholder="Nhập lại mật khẩu">
            <button class="visibility-button" type="button" (click)="showConfirmPassword = !showConfirmPassword" [attr.aria-label]="showConfirmPassword ? 'Ẩn mật khẩu xác nhận' : 'Hiện mật khẩu xác nhận'">
              <mat-icon>{{ showConfirmPassword ? 'visibility' : 'visibility_off' }}</mat-icon>
            </button>
          </div>

          @if (passwordsMismatch) {
            <p class="field-error">Mật khẩu nhập lại chưa khớp.</p>
          }

          <mat-checkbox class="terms-check" formControlName="acceptedTerms">Tôi đồng ý với điều khoản sử dụng</mat-checkbox>

          <button mat-flat-button class="submit-button" type="submit" [disabled]="form.invalid || passwordsMismatch || loading">
            {{ loading ? 'Đang đăng ký...' : 'Đăng ký' }}
          </button>

          <div class="card-divider"></div>
          <p class="login-prompt">Đã có tài khoản? <a routerLink="/login">Đăng nhập ngay</a></p>
        </form>
      </section>
    </main>
  `,
  styles: [`
    :host { display: block; min-height: 100vh; background: #f6f6f6; color: #171717; }
    .register-page { min-height: 100vh; }
    .site-header { display: flex; align-items: center; gap: 32px; height: 82px; padding: 0 30px; background: #ffffff; border-bottom: 1px solid #eceff3; }
    .brand { display: inline-flex; align-items: center; text-decoration: none; font-size: 36px; font-weight: 800; line-height: 1; letter-spacing: 0; }
    .brand-mark { display: grid; place-items: center; width: 32px; height: 32px; margin-right: 2px; border-radius: 9px; background: #155bd4; color: #ffffff; font-size: 25px; font-weight: 900; }
    .brand-you { color: #155bd4; }
    .brand-med { color: #11b66a; }
    .main-nav { display: flex; align-items: center; justify-content: flex-end; gap: 56px; flex: 1; font-size: 18px; font-weight: 700; }
    .main-nav a, .outline-login, .login-prompt a, .auth-tab { color: #096fe7; text-decoration: none; }
    .main-nav a { color: #151515; }
    .chevron { color: #9aa4b2; font-size: 18px; }
    .outline-login { display: inline-flex; align-items: center; justify-content: center; min-width: 142px; height: 46px; border: 1px solid #096fe7; border-radius: 4px; font-size: 17px; font-weight: 700; }
    .register-content { display: grid; grid-template-columns: minmax(360px, 680px) minmax(380px, 560px); align-items: center; justify-content: center; gap: 86px; min-height: calc(100vh - 82px); padding: 48px; box-sizing: border-box; }
    .promo-panel { display: flex; justify-content: center; }
    .promo-panel img { display: block; width: min(100%, 650px); height: auto; }
    .register-card { width: min(100%, 560px); padding: 32px 30px 40px; background: #ffffff; border: 1px solid #edf0f4; border-radius: 8px; box-sizing: border-box; }
    .auth-tabs { display: grid; grid-template-columns: 1fr 1fr; border-bottom: 1px solid #e9e9e9; margin-bottom: 20px; }
    .auth-tab { border: 0; background: transparent; min-height: 48px; font: inherit; font-size: 18px; font-weight: 700; text-align: center; cursor: pointer; color: #151515; }
    .auth-tab.active { color: #096fe7; border-bottom: 2px solid #096fe7; }
    .field-label { display: block; margin: 0 0 7px; font-size: 17px; font-weight: 500; }
    .text-input { width: 100%; height: 50px; margin-bottom: 14px; padding: 0 16px; border: 1px solid #e0e4ea; border-radius: 7px; box-sizing: border-box; font: inherit; font-size: 17px; outline: none; background: #ffffff; transition: border-color 160ms ease, box-shadow 160ms ease; }
    .text-input:focus { border-color: #096fe7; box-shadow: 0 0 0 3px rgba(9, 111, 231, 0.12); }
    .password-field { position: relative; }
    .password-field.compact .text-input { margin-bottom: 8px; }
    .password-field .text-input { padding-right: 52px; }
    .visibility-button { position: absolute; top: 25px; right: 10px; display: grid; place-items: center; width: 36px; height: 36px; border: 0; background: transparent; color: #98a1ad; transform: translateY(-50%); cursor: pointer; }
    .visibility-button mat-icon { width: 22px; height: 22px; font-size: 22px; }
    .field-error { margin: 0 0 10px; color: #d32f2f; font-size: 14px; }
    .terms-check { display: block; margin-bottom: 24px; font-size: 16px; }
    .submit-button { width: 100%; height: 56px; border-radius: 5px; font-size: 18px; font-weight: 800; background: #096fe7; color: #ffffff; }
    .submit-button:disabled { background: #cccccc; color: #ffffff; }
    .card-divider { height: 1px; margin: 30px 0 24px; background: #e9e9e9; }
    .login-prompt { margin: 0; text-align: center; font-size: 18px; }
    @media (max-width: 1080px) { .main-nav { gap: 24px; font-size: 16px; } .register-content { gap: 40px; } }
    @media (max-width: 860px) { .site-header { height: auto; min-height: 72px; padding: 16px 20px; flex-wrap: wrap; } .main-nav { order: 3; width: 100%; justify-content: flex-start; overflow-x: auto; white-space: nowrap; } .outline-login { margin-left: auto; } .register-content { grid-template-columns: 1fr; min-height: auto; padding: 32px 20px; } .promo-panel img { max-width: 420px; } }
    @media (max-width: 520px) { .brand { font-size: 29px; } .brand-mark { width: 28px; height: 28px; font-size: 22px; } .outline-login { min-width: 112px; height: 40px; font-size: 15px; } .register-card { padding: 26px 18px 34px; } }
  `]
})
export class RegisterComponent {
  loading = false;
  showPassword = false;
  showConfirmPassword = false;
  form = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    fullName: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required, Validators.minLength(6)]],
    acceptedTerms: [false, Validators.requiredTrue]
  });

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private notification: NotificationService
  ) {}

  get passwordsMismatch(): boolean {
    const password = this.form.controls.password.value;
    const confirmPassword = this.form.controls.confirmPassword.value;
    return !!confirmPassword && password !== confirmPassword;
  }

  onSubmit(): void {
    if (this.form.valid && !this.passwordsMismatch) {
      this.loading = true;
      const { username, email, password, fullName } = this.form.getRawValue();
      this.authService.register({
        username: username || '',
        email: email || '',
        password: password || '',
        fullName: fullName || ''
      }).subscribe({
        next: () => {
          this.notification.success('Đăng ký thành công');
          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          this.loading = false;
          this.notification.error(err.error?.message || 'Đăng ký thất bại');
        }
      });
    }
  }
}

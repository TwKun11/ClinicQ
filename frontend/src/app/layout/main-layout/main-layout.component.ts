import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatSidenavModule, MatToolbarModule, MatListModule, MatIconModule, MatButtonModule],
  template: `
    <mat-sidenav-container class="sidenav-container">
      <mat-sidenav mode="side" opened class="sidenav">
        <div class="sidenav-header">
          <h3>ClinicQ</h3>
          <p>Hệ thống quản trị</p>
        </div>
        <mat-nav-list>
          @if (!authService.isAdmin()) {
            <a mat-list-item routerLink="/home" routerLinkActive="active">
              <mat-icon matListItemIcon>home</mat-icon>
              <span matListItemTitle>Trang chủ</span>
            </a>
          }
          @if (authService.isAdmin()) {
            <a mat-list-item routerLink="/dashboard" routerLinkActive="active">
              <mat-icon matListItemIcon>dashboard</mat-icon>
              <span matListItemTitle>Tổng quan</span>
            </a>
            <a mat-list-item routerLink="/users" routerLinkActive="active">
              <mat-icon matListItemIcon>people</mat-icon>
              <span matListItemTitle>Người dùng</span>
            </a>
            <a mat-list-item routerLink="/doctors" routerLinkActive="active">
              <mat-icon matListItemIcon>medical_services</mat-icon>
              <span matListItemTitle>Quản lý bác sĩ</span>
            </a>
            <a mat-list-item routerLink="/schedules" routerLinkActive="active">
              <mat-icon matListItemIcon>calendar_month</mat-icon>
              <span matListItemTitle>Quản lý lịch khám</span>
            </a>
            <a mat-list-item routerLink="/queues" routerLinkActive="active">
              <mat-icon matListItemIcon>queue</mat-icon>
              <span matListItemTitle>Theo dõi hàng đợi</span>
            </a>
          }
          @if (authService.canUseChangePassword()) {
            <a mat-list-item routerLink="/change-password" routerLinkActive="active">
              <mat-icon matListItemIcon>lock</mat-icon>
              <span matListItemTitle>Đổi mật khẩu</span>
            </a>
          }
        </mat-nav-list>
      </mat-sidenav>

      <mat-sidenav-content>
        <mat-toolbar color="primary">
          <span class="spacer"></span>
          <span class="account-label">{{ authService.getEmail() || authService.getUsername() }}</span>
          <button mat-icon-button (click)="authService.logout()">
            <mat-icon>logout</mat-icon>
          </button>
        </mat-toolbar>
        <div class="content">
          <router-outlet></router-outlet>
        </div>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .sidenav-container { height: 100vh; }
    .sidenav { width: 240px; }
    .sidenav-header { padding: 18px 16px; text-align: center; border-bottom: 1px solid #e0e0e0; }
    .sidenav-header h3 { margin: 0; color: #004e9f; font-size: 24px; font-weight: 800; }
    .sidenav-header p { margin: 4px 0 0; color: #64748b; font-size: 12px; font-weight: 600; }
    .content { padding: 24px; }
    .spacer { flex: 1 1 auto; }
    .account-label { margin-right: 16px; }
    .active { background-color: rgba(0, 0, 0, 0.04); }
  `]
})
export class MainLayoutComponent {
  constructor(public authService: AuthService) {}
}

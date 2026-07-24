import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatSidenavModule, MatToolbarModule, MatListModule, MatIconModule, MatButtonModule],
  template: `
    <mat-sidenav-container class="sidenav-container">
      <mat-sidenav mode="side" opened class="sidenav">
        <div class="sidenav-header">
          <h3>Training Starter</h3>
        </div>
        <mat-nav-list>
          @if (!authService.isAdmin()) {
            <a mat-list-item routerLink="/home" routerLinkActive="active">
              <mat-icon matListItemIcon>home</mat-icon>
              <span matListItemTitle>Home</span>
            </a>
          }
          @if (authService.isAdmin()) {
            <a mat-list-item routerLink="/dashboard" routerLinkActive="active">
              <mat-icon matListItemIcon>dashboard</mat-icon>
              <span matListItemTitle>Dashboard</span>
            </a>
            <a mat-list-item routerLink="/users" routerLinkActive="active">
              <mat-icon matListItemIcon>people</mat-icon>
              <span matListItemTitle>Users</span>
            </a>
          }
          @if (authService.canUseChangePassword()) {
            <a mat-list-item routerLink="/change-password" routerLinkActive="active">
              <mat-icon matListItemIcon>lock</mat-icon>
              <span matListItemTitle>Change password</span>
            </a>
          }
        </mat-nav-list>
      </mat-sidenav>

      <mat-sidenav-content>
        <mat-toolbar color="primary">
          <span class="spacer"></span>
          <span style="margin-right: 16px;">{{ authService.getEmail() || authService.getUsername() }}</span>
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
    .sidenav-header { padding: 16px; text-align: center; border-bottom: 1px solid #e0e0e0; }
    .sidenav-header h3 { margin: 0; }
    .content { padding: 24px; }
    .spacer { flex: 1 1 auto; }
    .active { background-color: rgba(0, 0, 0, 0.04); }
  `]
})
export class MainLayoutComponent {
  constructor(public authService: AuthService) {}
}

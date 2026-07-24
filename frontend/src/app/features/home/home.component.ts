import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [MatCardModule],
  template: `
    <mat-card>
      <mat-card-header>
        <mat-card-title>Home</mat-card-title>
        <mat-card-subtitle>{{ authService.getEmail() || authService.getUsername() }}</mat-card-subtitle>
      </mat-card-header>
      <mat-card-content>
        <p>Welcome to ClinicQ.</p>
      </mat-card-content>
    </mat-card>
  `
})
export class HomeComponent {
  constructor(public authService: AuthService) {}
}

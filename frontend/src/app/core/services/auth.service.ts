import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';

export interface AuthResponse {
  accessToken: string;
  username: string;
  email: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient, private router: Router) {}

  login(credentials: { email: string; password: string }): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/login`, credentials, { withCredentials: true }).pipe(
      tap(res => {
        if (res.success && res.data) {
          this.storeTokens(res.data);
        }
      })
    );
  }

  googleLogin(credential: string): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/google`, { credential }, { withCredentials: true }).pipe(
      tap(res => {
        if (res.success && res.data) {
          this.storeTokens(res.data);
        }
      })
    );
  }

  register(data: { username: string; email: string; password: string; fullName: string }): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/register`, data, { withCredentials: true });
  }

  me(): Observable<ApiResponse<{ id: number; username: string; email: string; fullName: string; role: string; status: string; active: boolean }>> {
    return this.http.get<ApiResponse<{ id: number; username: string; email: string; fullName: string; role: string; status: string; active: boolean }>>(`${this.apiUrl}/me`);
  }

  forgotPassword(email: string): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/reset-password`, { token, newPassword });
  }

  changePassword(currentPassword: string, newPassword: string): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/change-password`, { currentPassword, newPassword });
  }

  logout(): void {
    this.http.post(`${this.apiUrl}/logout`, {}, { withCredentials: true }).subscribe();
    this.clearLocalAuth();
    this.router.navigate(['/login']);
  }

  refreshToken(): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/refresh`, {}, { withCredentials: true }).pipe(
      tap(res => {
        if (res.success && res.data) {
          this.storeTokens(res.data);
        }
      })
    );
  }

  clearLocalAuth(): void {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
    localStorage.removeItem('role');
  }

  getToken(): string | null {
    return localStorage.getItem('access_token');
  }

  getUsername(): string | null {
    return localStorage.getItem('username');
  }

  getEmail(): string | null {
    return localStorage.getItem('email');
  }

  getRole(): string | null {
    return localStorage.getItem('role');
  }

  landingRouteForRole(role = this.getRole()): string {
    return role === 'ADMIN' ? '/dashboard' : '/home';
  }

  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  canUseChangePassword(): boolean {
    const role = this.getRole();
    return role === 'USER' || role === 'STAFF';
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  private storeTokens(auth: AuthResponse): void {
    localStorage.setItem('access_token', auth.accessToken);
    localStorage.setItem('username', auth.username);
    localStorage.setItem('email', auth.email);
    localStorage.setItem('role', auth.role);
  }
}

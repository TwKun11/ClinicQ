import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const token = localStorage.getItem('access_token');
  const isAuthRequest = req.url.includes('/auth/login')
    || req.url.includes('/auth/google')
    || req.url.includes('/auth/register')
    || req.url.includes('/auth/forgot-password')
    || req.url.includes('/auth/reset-password')
    || req.url.includes('/auth/refresh')
    || req.url.includes('/auth/logout');

  if (token) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  if (token && !isAuthRequest && isExpiringSoon(token)) {
    return authService.refreshToken().pipe(
      switchMap((res) => {
        const refreshedRequest = req.clone({
          setHeaders: { Authorization: `Bearer ${res.data?.accessToken}` }
        });
        return next(refreshedRequest);
      }),
      catchError((error) => {
        authService.clearLocalAuth();
        router.navigate(['/login']);
        return throwError(() => error);
      })
    );
  }

  return next(req).pipe(
    catchError((error) => {
      if (error.status === 401 && !isAuthRequest) {
        return authService.refreshToken().pipe(
          switchMap((res) => {
            const retriedRequest = req.clone({
              setHeaders: { Authorization: `Bearer ${res.data?.accessToken}` }
            });
            return next(retriedRequest);
          }),
          catchError((refreshError) => {
            authService.clearLocalAuth();
            router.navigate(['/login']);
            return throwError(() => refreshError);
          })
        );
      }
      return throwError(() => error);
    })
  );
};

function isExpiringSoon(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.exp * 1000 - Date.now() < 60_000;
  } catch {
    return true;
  }
}
